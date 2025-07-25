package com.kaiming.xiaohongshu.count.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.domain.dataobject.UserCountDO;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.UserCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.service.UserCountService;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: CountService
 * Package: com.kaiming.xiaohongshu.count.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 20:42
 * @Version 1.0
 */
@Service
public class UserCountServiceImpl implements UserCountService {

    @Resource
    private UserCountDOMapper userCountDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 查询用户相关计数
     *
     * @param findUserCountsByIdReqDTO
     * @return
     */
    @Override
    public Response<FindUserCountsByIdRespDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) {
        // 用户Id
        Long userId = findUserCountsByIdReqDTO.getUserId();

        FindUserCountsByIdRespDTO findUserCountsByIdRespDTO = FindUserCountsByIdRespDTO.builder()
                .userId(userId)
                .build();
        // 先查询Redis
        String userCountHashKey = RedisKeyConstants.buildCountUserKey(userId);

        List<Object> counts = redisTemplate.opsForHash()
                .multiGet(userCountHashKey, List.of(
                        RedisKeyConstants.FIELD_FANS_TOTAL,
                        RedisKeyConstants.FIELD_FOLLOWING_TOTAL,
                        RedisKeyConstants.FIELD_LIKE_TOTAL,
                        RedisKeyConstants.FIELD_NOTE_TOTAL,
                        RedisKeyConstants.FIELD_COLLECT_TOTAL
                ));
        // 若 Hash 中计数不为空，优先以其为主（实时性更高）
        Object fansTotal = counts.get(0);
        Object followingTotal = counts.get(1);
        Object likeTotal = counts.get(2);
        Object noteTotal = counts.get(3);
        Object collectTotal = counts.get(4);

        findUserCountsByIdRespDTO.setFansTotal(Objects.isNull(fansTotal) ? 0 : Long.parseLong(String.valueOf(fansTotal)));
        findUserCountsByIdRespDTO.setFollowingTotal(Objects.isNull(followingTotal) ? 0 : Long.parseLong(String.valueOf(followingTotal)));
        findUserCountsByIdRespDTO.setLikeTotal(Objects.isNull(likeTotal) ? 0 : Long.parseLong(String.valueOf(likeTotal)));
        findUserCountsByIdRespDTO.setNoteTotal(Objects.isNull(noteTotal) ? 0 : Long.parseLong(String.valueOf(noteTotal)));
        findUserCountsByIdRespDTO.setCollectTotal(Objects.isNull(collectTotal) ? 0 : Long.parseLong(String.valueOf(collectTotal)));
        // 若 Hash 中有任何一个计数为空
        boolean isAnyNull = counts.stream().anyMatch(Objects::isNull);
        if (isAnyNull) {
            // 从数据库查询该用户的计数
            UserCountDO userCountDO = userCountDOMapper.selectByUserId(userId);
            // 判断 Redis 中对应计数，若为空，则使用 DO 中的计数
            if (Objects.nonNull(userCountDO) && Objects.isNull(fansTotal)) {
                findUserCountsByIdRespDTO.setFansTotal(userCountDO.getFansTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(followingTotal)) {
                findUserCountsByIdRespDTO.setFollowingTotal(userCountDO.getFollowingTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(likeTotal)) {
                findUserCountsByIdRespDTO.setLikeTotal(userCountDO.getLikeTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(noteTotal)) {
                findUserCountsByIdRespDTO.setNoteTotal(userCountDO.getNoteTotal());
            }
            if (Objects.nonNull(userCountDO) && Objects.isNull(collectTotal)) {
                findUserCountsByIdRespDTO.setCollectTotal(userCountDO.getCollectTotal());
            }
            // 异步同步到 Redis 缓存中, 以便下次查询能够命中缓存
            asyncHashCount2Redis(userCountHashKey, userCountDO, fansTotal, followingTotal, likeTotal, noteTotal, collectTotal);
        }
        return Response.success(findUserCountsByIdRespDTO);
    }

    /**
     * 异步同步 Redis
     *
     * @param userCountHashKey
     * @param userCountDO
     * @param fansTotal
     * @param followingTotal
     * @param likeTotal
     * @param noteTotal
     * @param collectTotal
     */
    private void asyncHashCount2Redis(String userCountHashKey, UserCountDO userCountDO, Object fansTotal, Object followingTotal, Object likeTotal, Object noteTotal, Object collectTotal) {
        if (Objects.nonNull(userCountDO)) {
            threadPoolTaskExecutor.execute(() -> {
                Map<String, Long> userCountMap = Maps.newHashMap();
                if (Objects.isNull(collectTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_COLLECT_TOTAL, Objects.isNull(userCountDO.getCollectTotal()) ? 0 : userCountDO.getCollectTotal());

                if (Objects.isNull(fansTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_FANS_TOTAL, Objects.isNull(userCountDO.getFansTotal()) ? 0 : userCountDO.getFansTotal());

                if (Objects.isNull(noteTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_NOTE_TOTAL, Objects.isNull(userCountDO.getNoteTotal()) ? 0 : userCountDO.getNoteTotal());

                if (Objects.isNull(followingTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_FOLLOWING_TOTAL, Objects.isNull(userCountDO.getFollowingTotal()) ? 0 : userCountDO.getFollowingTotal());

                if (Objects.isNull(likeTotal))
                    userCountMap.put(RedisKeyConstants.FIELD_LIKE_TOTAL, Objects.isNull(userCountDO.getLikeTotal()) ? 0 : userCountDO.getLikeTotal());

                redisTemplate.executePipelined(new SessionCallback<>() {

                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        // 批量添加 Hash 的计数 Field
                        operations.opsForHash().putAll(userCountHashKey, userCountMap);
                        // 设置随机过期时间 (2小时以内)
                        long expireTime = 60 * 60 + RandomUtil.randomInt(60 * 60);
                        operations.expire(userCountHashKey, expireTime, TimeUnit.SECONDS);
                        return null;
                    }
                });
            });
        }
    }
}
