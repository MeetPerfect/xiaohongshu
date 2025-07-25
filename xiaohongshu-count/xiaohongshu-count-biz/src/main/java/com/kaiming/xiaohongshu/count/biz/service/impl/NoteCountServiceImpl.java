package com.kaiming.xiaohongshu.count.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.domain.dataobject.NoteCountDO;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.NoteCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.service.NoteCountService;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: NoteCountServiceImpl
 * Package: com.kaiming.xiaohongshu.count.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 19:45
 * @Version 1.0
 */
@Service
@Slf4j
public class NoteCountServiceImpl implements NoteCountService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    /**
     * 批量查询笔记计数
     * @param findNoteCountByIdReqDTO
     * @return
     */
    @Override
    public Response<List<FindNoteCountByIdRespDTO>> findNoteCountData(FindNoteCountByIdReqDTO findNoteCountByIdReqDTO) {
        // 笔记集合
        List<Long> noteIds = findNoteCountByIdReqDTO.getNoteIds();

        // 1. 先查询 Redis 缓存
        List<String> hashKeys = noteIds.stream()
                .map(RedisKeyConstants::buildCountNoteKey)
                .toList();
        // 使用 Pipeline 通道，从 Redis 中批量查询笔记 Hash 计数
        List<Object> countHashes = getCountHashesByPipelineFromRedis(hashKeys);

        // 返参 DTO
        List<FindNoteCountByIdRespDTO>  findNoteCountsByIdRespDTOS = Lists.newArrayList();

        // 用于存储缓存中不存在，需要查数据库的笔记 ID
        List<Long> noteIdsNeedQuery = Lists.newArrayList();
        for (int i = 0; i < noteIds.size(); i++) {
            Long noteId = noteIds.get(i);
            List<Integer> currCountHash = (List<Integer>) countHashes.get(i);
            // 点赞数、收藏数、评论数
            Integer likeTotal = currCountHash.get(0);
            Integer collectTotal = currCountHash.get(1);
            Integer commentTotal = currCountHash.get(2);

            // Hash 中存在任意一个 Field 为 null, 都需要查询数据库
            if (Objects.isNull(likeTotal) || Objects.isNull(collectTotal) || Objects.isNull(commentTotal)) {
                noteIdsNeedQuery.add(noteId);
            }

            FindNoteCountByIdRespDTO findNoteCountsByIdRespDTO = FindNoteCountByIdRespDTO.builder()
                    .noteId(noteId)
                    .likeTotal(Objects.nonNull(likeTotal) ? Long.valueOf(likeTotal) : null)
                    .collectTotal(Objects.nonNull(collectTotal) ? Long.valueOf(collectTotal) : null)
                    .commentTotal(Objects.nonNull(commentTotal) ? Long.valueOf(commentTotal) : null)
                    .build();
            findNoteCountsByIdRespDTOS.add(findNoteCountsByIdRespDTO);
        }
        // 所有 Hash 计数都存在于 Redis 中，直接返参
        if (CollUtil.isEmpty(noteIdsNeedQuery)) {
            return Response.success(findNoteCountsByIdRespDTOS);
        }
        // 2. 若缓存中无，则查询数据库
        // 从数据库中批量查询过滤出的 noteIdsNeedQuery 笔记 ID
        List<NoteCountDO> noteCountDOS = noteCountDOMapper.selectByNoteIds(noteIdsNeedQuery);

        // 若数据库查询的记录不为空
        if (CollUtil.isNotEmpty(noteCountDOS)) {
            // DO 集合转 Map, 方便后续查询对应笔记 ID 的计数
            Map<Long, NoteCountDO> noteIdAndDOMap = noteCountDOS.stream()
                    .collect(Collectors.toMap(NoteCountDO::getNoteId, noteCountDO -> noteCountDO));

            // TODO: 将笔记 Hash 计数同步到 Redis 中
            syncNoteHash2Redis(findNoteCountsByIdRespDTOS, noteIdAndDOMap);
            
            // 针对 DTO 中为 null 的计数字段，循环设置从数据库中查询到的计数
            for (FindNoteCountByIdRespDTO findNoteCountsByIdRespDTO : findNoteCountsByIdRespDTOS) {
                Long noteId = findNoteCountsByIdRespDTO.getNoteId();
                Long likeTotal = findNoteCountsByIdRespDTO.getLikeTotal();
                Long collectTotal = findNoteCountsByIdRespDTO.getCollectTotal();
                Long commentTotal = findNoteCountsByIdRespDTO.getCommentTotal();

                if (Objects.isNull(likeTotal))
                    findNoteCountsByIdRespDTO.setLikeTotal(noteIdAndDOMap.get(noteId).getLikeTotal());
                if (Objects.isNull(collectTotal))
                    findNoteCountsByIdRespDTO.setCollectTotal(noteIdAndDOMap.get(noteId).getCollectTotal());
                if (Objects.isNull(commentTotal))
                    findNoteCountsByIdRespDTO.setCommentTotal(noteIdAndDOMap.get(noteId).getCommentTotal());
            }
            
        }
        
        return Response.success(findNoteCountsByIdRespDTOS);
    }

    /**
     * 将笔记 Hash 计数同步到 Redis 中
     * @param findNoteCountsByIdRespDTOS
     * @param noteIdAndDOMap
     */
    private void syncNoteHash2Redis(List<FindNoteCountByIdRespDTO> findNoteCountsByIdRespDTOS, Map<Long, NoteCountDO> noteIdAndDOMap) {
        // 将笔记计数同步到 Redis 中
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                // 循环已构建好的返参 DTO 集合
                for (FindNoteCountByIdRespDTO findNoteCountsByIdRespDTO : findNoteCountsByIdRespDTOS) {
                    Long likeTotal = findNoteCountsByIdRespDTO.getLikeTotal();
                    Long collectTotal = findNoteCountsByIdRespDTO.getCollectTotal();
                    Long commentTotal = findNoteCountsByIdRespDTO.getCommentTotal();
                    
                    // 若当前 DTO 的所有计数都不为空，则无需同步 Hash
                    if (Objects.nonNull(likeTotal) && Objects.nonNull(collectTotal) && Objects.nonNull(commentTotal)) {
                        continue;
                    }
                    // 否则，若有任意一个 Field 计数为空，则需要同步对应的 Field
                    Long noteId = findNoteCountsByIdRespDTO.getNoteId();
                    String noteCountHashKey = RedisKeyConstants.buildCountNoteKey(noteId);
                    // 设置 Field 计数
                    Map<String, Long> countMap = Maps.newHashMap();
                    NoteCountDO noteCountDO = noteIdAndDOMap.get(noteId);

                    if (Objects.isNull(likeTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_LIKE_TOTAL, noteCountDO.getLikeTotal());
                    }
                    if (Objects.isNull(collectTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_COLLECT_TOTAL, noteCountDO.getCollectTotal());
                    }
                    if (Objects.isNull(commentTotal)) {
                        countMap.put(RedisKeyConstants.FIELD_COMMENT_TOTAL, noteCountDO.getCommentTotal());
                    }
                    // 批量添加 Hash 的计数 Field
                    operations.opsForHash().putAll(noteCountHashKey, countMap);

                    // 设置随机过期时间 (1小时以内)
                    long expireTime = 60*30 + RandomUtil.randomInt(60 * 30);
                    operations.expire(noteCountHashKey, expireTime, TimeUnit.SECONDS);
                }

                return null;
            }
        });
    }

    /**
     * 从 Redis 中批量查询笔记 Hash 计数
     * @param hashKeys
     * @return
     */
    private List<Object> getCountHashesByPipelineFromRedis(List<String> hashKeys) {
        return redisTemplate.executePipelined(new SessionCallback<>() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (String hashKey : hashKeys) {
                    // 批量获取多个字段
                    operations.opsForHash().multiGet(hashKey, List.of(
                            RedisKeyConstants.FIELD_LIKE_TOTAL,
                            RedisKeyConstants.FIELD_COLLECT_TOTAL,
                            RedisKeyConstants.FIELD_COMMENT_TOTAL
                    ));
                }
                return null;
            }
        });
    }
}
