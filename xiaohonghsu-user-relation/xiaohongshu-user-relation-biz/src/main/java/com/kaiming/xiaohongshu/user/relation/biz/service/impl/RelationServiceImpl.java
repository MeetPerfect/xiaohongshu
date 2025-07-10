package com.kaiming.xiaohongshu.user.relation.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Page;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.kaiming.xiaohongshu.user.relation.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.user.relation.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.user.relation.biz.domain.dataobject.FansDO;
import com.kaiming.xiaohongshu.user.relation.biz.domain.dataobject.FollowingDO;
import com.kaiming.xiaohongshu.user.relation.biz.domain.mapper.FansDOMapper;
import com.kaiming.xiaohongshu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.kaiming.xiaohongshu.user.relation.biz.enums.LuaResultEnum;
import com.kaiming.xiaohongshu.user.relation.biz.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.FollowUserMqDTO;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.kaiming.xiaohongshu.user.relation.biz.model.vo.*;
import com.kaiming.xiaohongshu.user.relation.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.user.relation.biz.service.RelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * ClassName: RelationServiceImpl
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:13
 * @Version 1.0
 */
@Service
@Slf4j
public class RelationServiceImpl implements RelationService {
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private FansDOMapper fansDOMapper;

    /**
     * 关注用户接口
     *
     * @param followUserReqVO
     * @return
     */
    @Override
    public Response<?> follow(FollowUserReqVO followUserReqVO) {
        // 关注用户Id
        Long followUserId = followUserReqVO.getFollowUserId();
        // 当前登录用户Id
        Long userId = LoginUserContextHolder.getUserId();
        // 如果关注用户Id与当前用户Id相等，抛出自己不能关注自己异常
        if (Objects.equals(userId, followUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        // 校验关注的用户是否存在
        FindUserByIdRespDTO findUserByIdRespDTO = userRpcService.findById(followUserId);
        if (Objects.isNull(findUserByIdRespDTO)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }
        // 构建当前用户关注列表的 Redis Key
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 得到对应的时间戳
        long timestamp = DateUtils.localDateTime2Timestamp(now);

        // 执行Lua脚本
        Long result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);

        // 校验 Lua 脚本执行结果
        checkLuaScriptResult(result);
        // ZSET 不存在
        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            // 从数据库查询当前用户的关注关系记录
            List<FollowingDO> followingDOS = followingDOMapper.selectByUserId(userId);
            // 如果当前用户的关注关系记录不为空，则需要设置过期时间
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            // 若记录为空，直接ZADD关系数据，并设置过期时间
            if (CollUtil.isEmpty(followingDOS)) {
                DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_add_and_expire.lua")));
                script2.setResultType(Long.class);
                // TODO: 可以根据用户类型，设置不同的过期时间，若当前用户为大V, 则可以过期时间设置的长些或者不设置过期时间；如不是，则设置的短些
                // 如何判断呢？可以从计数服务获取用户的粉丝数，目前计数服务还没创建，则暂时采用统一的过期策略

                redisTemplate.execute(script2, Collections.singletonList(followingRedisKey), followUserId, timestamp, expireSeconds);
            } else {
                // 构建Lua参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行Lua脚本
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);
                // 再次调用上面的 Lua 脚本：follow_check_and_add.lua , 将最新的关注关系添加进去
                result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);
                checkLuaScriptResult(result);
            }
        }

        // 发送 MQ
        // 构建消息体 DTO
        FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(now)
                .build();
        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;

        log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);

        String hashKey = String.valueOf(userId);

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    /**
     * 取消关注用户接口
     *
     * @param unfollowUserReqVO
     * @return
     */
    @Override
    public Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO) {
        // 取关用户Id
        Long unfollowUserId = unfollowUserReqVO.getUnfollowUserId();
        // 当前用户Id
        Long userId = LoginUserContextHolder.getUserId();
        // 如果当前用户Id与取关用户Id相等，抛出自己不能取关自己的异常
        if (Objects.equals(userId, unfollowUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_UNFOLLOW_YOUR_SELF);
        }

        // 校验取关用户是否存在
        FindUserByIdRespDTO findUserByIdRespDTO = userRpcService.findById(unfollowUserId);
        if (Objects.isNull(findUserByIdRespDTO)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        // 必须是关注了该用户，才能取关
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_delete.lua")));
        script.setResultType(Long.class);

        // 执行 Lua 脚本
        Long result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);

        // 校验 Lua 脚本执行结果, 取关用户不在关注列表中
        if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
            throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
        }

        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            // 从数据库中查询当前用户的关注关系记录
            List<FollowingDO> followingDOS = followingDOMapper.selectByUserId(userId);

            // 随即过期时间
            // 保底1天 + 随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 若记录为空，则表示还未关注任何人，提示为关注对方
            if (CollUtil.isEmpty(followingDOS)) {
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            } else {    // 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间；
                // 构建 Lua 脚本参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本 , 批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // 再次调用上面的 Lua 脚本：unfollow_check_and_delete.lua , 将最新的关注关系删除
                result = redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);
                if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
                    throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
                }
            }
        }

        // 发送 MQ
        // 构建消息题 DTO
        UnfollowUserMqDTO unfollowUserMqDTO = UnfollowUserMqDTO.builder()
                .userId(userId)
                .unfollowUserId(unfollowUserId)
                .createTime(LocalDateTime.now())
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserMqDTO)).build();
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_UNFOLLOW;

        log.info("==> 开始发送取关操作 MQ, 消息体: {}", unfollowUserMqDTO);

        String hashKey = String.valueOf(userId);

        // 异步发送 MQ 消息， 提升接口的响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ 发送异常: ", throwable);
            }
        });
        return Response.success();
    }

    /**
     * 查询关注列表接口
     *
     * @param findFollowingListReqVO
     * @return
     */
    @Override
    public PageResponse<FindFollowingUserRespVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO) {
        // 用户 ID
        Long userId = findFollowingListReqVO.getUserId();
        // 页码
        Integer pageNo = findFollowingListReqVO.getPageNo();
        // Redis 中查询
        String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        // 查询目标用户关注列表 ZSET 的总大小
        long total = redisTemplate.opsForZSet().zCard(followingListRedisKey);

        // 返回参数
        List<FindFollowingUserRespVO> findFollowingUserRespVOS = null;
        // 每页展示 10 条数据
        long limit = 10;

        if (total > 0) {
            // 计算总页数
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数
            if (pageNo > totalPage) {
                return PageResponse.success(null, pageNo, total);
            }

            // 准备从 Redis 中查询 Zset 分页数据
            // 每页 展示 10 条数据
            long offset = (pageNo - 1) * limit;

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            // 注意：这里使用了 Double.POSITIVE_INFINITY 和 Double.NEGATIVE_INFINITY 作为分数范围
            // 因为关注列表最多有 1000 个元素，这样可以确保获取到所有的元素
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(followingListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 提取所有用户 ID 到集合
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC 批量查询用户信息
                List<FindUserByIdRespDTO> findUserByIdRespDTOS = userRpcService.findByIds(userIds);

                // 如果查询结果为空，直接返回空的关注列表
                if (CollUtil.isNotEmpty(findUserByIdRespDTOS)) {
                    findFollowingUserRespVOS = findUserByIdRespDTOS.stream()
                            .map(dto -> FindFollowingUserRespVO.builder()
                                    .userId(dto.getId())
                                    .avatar(dto.getAvatar())
                                    .nickname(dto.getNickName())
                                    .introduction(dto.getIntroduction())
                                    .build())
                            .toList();
                }
            }
        } else {
            // 若 Redis 中没有数据，则从数据库查询
            // 查询记录总量
            long count = followingDOMapper.selectCountByUserId(userId);

            // 计算总页数
            long totalPage = PageResponse.getTotalPage(count, limit);
            // 请求页码超出总页数
            if (pageNo > totalPage) {
                return PageResponse.success(null, pageNo, total);
            }

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);
            // 分页查询
            List<FollowingDO> followingDOS = followingDOMapper.selectPageListByUserId(userId, offset, limit);

            // 复制真实记录总数
            total = count;
            // 记录不为空
            if (CollUtil.isNotEmpty(followingDOS)) {
                // 提取所有关注用户 ID 到集合中
                List<Long> userIds = followingDOS.stream().map(FollowingDO::getFollowingUserId).toList();

                // RPC 调用用户服务, 并将 DTO 转为 VO
                findFollowingUserRespVOS = rpcUserServiceAndDTO2VO(userIds, findFollowingUserRespVOS);

                // 异步将关注列表全量同步到 Redis
                threadPoolTaskExecutor.submit(() -> syncFollowingList2Redis(userId));
            }
        }
        return PageResponse.success(findFollowingUserRespVOS, pageNo, total);
    }

    /**
     * 查询粉丝列表
     *
     * @param findFansListReqVO
     * @return
     */
    @Override
    public PageResponse<FindFansUserRespVO> findFansList(FindFansListReqVO findFansListReqVO) {
        // 查询的用户Id
        Long userId = findFansListReqVO.getUserId();

        // 页码
        Integer pageNo = findFansListReqVO.getPageNo();
        // 从 Redis 中查询粉丝列表
        String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);

        // 查询目标用户粉丝列表 ZSet 的总大小
        long total = redisTemplate.opsForZSet().zCard(fansListRedisKey);
        // 返回参数
        List<FindFansUserRespVO> findFansUserRespVOS = null;
        long limit = 10;
        if (total > 0) {
            // 总页数
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // Redis 查询 ZSet 分页数据
            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(fansListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 所有用户 Id 到集合
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC: 批量查询用户信息
                findFansUserRespVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRespVOS);

            }
        } else {
            // 查询数据库
            total = fansDOMapper.selectCountByUserId(userId);

            // 计算总页数
            long totalPage = PageResponse.getTotalPage(total, limit);
            // 请求页数超过总页数（只允许查询前500页）
            if (pageNo > 500 || pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            long offset = PageResponse.getOffset(pageNo, limit);

            List<FansDO> fansDOS = fansDOMapper.selectPageListByUserId(userId, offset, limit);

            if (CollUtil.isNotEmpty(fansDOS)) {
                // 将所有粉丝用户 ID 提取到集合中
                List<Long> userIds = fansDOS.stream().map(FansDO::getFansUserId).toList();

                // RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO
                findFansUserRespVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRespVOS);
                // 异步将粉丝列表全量同步到 Redis(最多5000条)
                threadPoolTaskExecutor.submit(() -> syncFansList2Redis(userId));
            }
        }
        return PageResponse.success(findFansUserRespVOS, pageNo, total);
    }

    /**
     * 粉丝列表同步到 Redis
     *
     * @param userId
     */
    private void syncFansList2Redis(Long userId) {
        // 查询粉丝列表 (最多 5000 位用户)
        List<FansDO> fansDOS = fansDOMapper.select5000FansByUserid(userId);

        if (CollUtil.isNotEmpty(fansDOS)) {
            // 用户粉丝列表 Redis Key
            String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);
            // 随机过期时间
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            // 构建 Lua 参数
            Object[] luaArgs = buildFansZSetLuaArgs(fansDOS, expireSeconds);

            // 执行 Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(fansListRedisKey), luaArgs);

        }
    }

    /**
     * 构建粉丝列表 ZSet 的 Lua 脚本参数
     *
     * @param fansDOS
     * @param expireSeconds
     */
    private Object[] buildFansZSetLuaArgs(List<FansDO> fansDOS, long expireSeconds) {
        int argsLength = fansDOS.size() * 2 + 1;   // 每个粉丝关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];
        
        int i = 0;
        for (FansDO fansDO : fansDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(fansDO.getCreateTime());
            luaArgs[i + 1] = fansDO.getFansUserId();
            i += 2;
        }
        luaArgs[argsLength - 1] = expireSeconds;
        return luaArgs;
    }

    /**
     * RPC 调用用户服务、计数服务，并将 DTO 转换为 VO
     *
     * @param userIds
     * @param findFansUserRespVOS
     * @return
     */
    private List<FindFansUserRespVO> rpcUserServiceAndCountServiceAndDTO2VO(List<Long> userIds, List<FindFansUserRespVO> findFansUserRespVOS) {
        List<FindUserByIdRespDTO> findUserByIdRespDTOS = userRpcService.findByIds(userIds);

        // TODO RPC: 批量查询用户的计数数据（笔记总数、粉丝总数）

        // 若不为空
        if (CollUtil.isNotEmpty(findUserByIdRespDTOS)) {
            findFansUserRespVOS = findUserByIdRespDTOS.stream()
                    .map(dto -> FindFansUserRespVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickName())
                            .noteTotal(0L)      // TODO: 这块的数据暂无，后续补充
                            .fansTotal(0L)      // TODO: 这块的数据暂无，后续补充
                            .build())
                    .toList();
        }
        return findFansUserRespVOS;
    }

    /**
     * 异步将关注列表全量同步到 Redis
     *
     * @param userId
     */
    private void syncFollowingList2Redis(Long userId) {
        // 查询全量关注用户列表(1000位用户)
        List<FollowingDO> followingDOS = followingDOMapper.selectAllByUserId(userId);
        if (CollUtil.isNotEmpty(followingDOS)) {
            // 用户关注列表 Redis Key
            String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
            // 随机过期时间
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 构建 Lua 脚本参数
            Object[] luaArg = buildLuaArgs(followingDOS, expireSeconds);
            // 执行 Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArg);
        }
    }

    private List<FindFollowingUserRespVO> rpcUserServiceAndDTO2VO(List<Long> userIds, List<FindFollowingUserRespVO> findFollowingUserRespVOS) {
        // RPC 批量查询用户信息
        List<FindUserByIdRespDTO> findUserByIdRespDTOS = userRpcService.findByIds(userIds);

        // 如果查询不为空，DTO 转为 VO
        if (CollUtil.isNotEmpty(findUserByIdRespDTOS)) {
            findFollowingUserRespVOS = findUserByIdRespDTOS.stream()
                    .map(dto -> FindFollowingUserRespVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickName())
                            .introduction(dto.getIntroduction())
                            .build()).toList();
        }
        return findFollowingUserRespVOS;
    }

    /**
     * 校验 Lua 脚本结果，根据状态码抛出对应的业务异常
     *
     * @param result
     */
    private void checkLuaScriptResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(result);

        if (Objects.isNull(luaResultEnum)) throw new RuntimeException("Lua返回结果错误");
        // 校验Lua脚本执行结果
        switch (luaResultEnum) {
            // 关注数已达到上限
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            // 已经关注了该用户
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * 构建lua脚本参数
     *
     * @param followingDOS
     * @param expireSeconds
     * @return
     */
    private Object[] buildLuaArgs(List<FollowingDO> followingDOS, long expireSeconds) {
        int argsLength = followingDOS.size() * 2 + 1;   // 每个关注关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (FollowingDO following : followingDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(following.getCreateTime());   // 关注时间作为 score
            luaArgs[i + 1] = following.getFollowingUserId();
            i += 2;
        }
        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }
}
