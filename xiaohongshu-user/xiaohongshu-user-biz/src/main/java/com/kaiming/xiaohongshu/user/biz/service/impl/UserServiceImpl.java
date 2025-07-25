package com.kaiming.xiaohongshu.user.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.enums.DeleteEnum;
import com.kaiming.framework.common.enums.StatusEnum;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.framework.common.util.NumberUtils;
import com.kaiming.framework.common.util.ParamUtils;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import com.kaiming.xiaohongshu.oss.api.FileFeignApi;
import com.kaiming.xiaohongshu.user.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.user.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.user.biz.constant.RoleConstants;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.RoleDO;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.UserDO;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.UserRoleDO;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.RoleDOMapper;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.UserDOMapper;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.UserRoleDOMapper;
import com.kaiming.xiaohongshu.user.biz.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.user.biz.enums.SexEnum;
import com.kaiming.xiaohongshu.user.biz.model.vo.FindUserProfileReqVO;
import com.kaiming.xiaohongshu.user.biz.model.vo.FindUserProfileRespVO;
import com.kaiming.xiaohongshu.user.biz.model.vo.UpdateUserInfoReqVO;
import com.kaiming.xiaohongshu.user.biz.rpc.CountRpcService;
import com.kaiming.xiaohongshu.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.kaiming.xiaohongshu.user.biz.rpc.OssRpcService;
import com.kaiming.xiaohongshu.user.biz.service.UserService;
import com.kaiming.xiaohongshu.user.dto.req.*;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: UserServiceImpl
 * Package: com.kaiming.xiaohongshu.user.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 21:54
 * @Version 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private FileFeignApi fileFeignApi;
    @Resource
    private OssRpcService ossRpcService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private CountRpcService countRpcService;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final Cache<Long, FindUserByIdRespDTO> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    // 用户简介本地缓存
    private static final Cache<Long, FindUserProfileRespVO> PROFILE_LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        // 被修改的用户Id
        Long userId = updateUserInfoReqVO.getUserId();
        // 当前登录用户
        Long currUserId = LoginUserContextHolder.getUserId();
        // 非号主本人，无法修改其个人信息
        if (!Objects.equals(userId, currUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_UPDATE_OTHER_USER_PROFILE);
        }

        UserDO userDO = new UserDO();
        // 设置用户Id
        userDO.setId(userId);
        // 标识位：是否需要更新
        boolean needUpdate = false;
        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();
        if (Objects.nonNull(avatarFile)) {
            // todo 调用对象存储服务上传文件
            String avatar = ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);

            if (StringUtils.isBlank(avatar)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            userDO.setAvatar(avatar);
            needUpdate = true;
        }

        // 昵称
        String nickName = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickName)) {
            Preconditions.checkArgument(ParamUtils.checkNickName(nickName), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickName);
            needUpdate = true;
        }

        // 小哈书号
        String xiaohashuId = updateUserInfoReqVO.getXiaohongshuId();
        if (StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohongshuId(xiaohashuId), ResponseCodeEnum.XIAOHONGSHU_ID_VALID_FAIL.getErrorMessage());
            userDO.setXiaohongshuId(xiaohashuId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundFile)) {
            String backgroundImg = ossRpcService.uploadFile(backgroundFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImg);

            // 若上传背景图失败，则抛出业务异常
            if (StringUtils.isBlank(backgroundImg)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }

            userDO.setBackgroundImg(backgroundImg);
            needUpdate = true;
        }

        if (needUpdate) {
            // 删除缓存
            deleteUserRedisCache(userId);

            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
            // 延时双删
            sendDelayDeleteUserRedisCacheMQ(userId);
        }

        return Response.success();
    }

    /**
     * 异步发送延迟消息
     *
     * @param userId
     */
    private void sendDelayDeleteUserRedisCacheMQ(Long userId) {
        Message<String> message = MessageBuilder.withPayload(String.valueOf(userId)).build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE, message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 用户缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("## 延时删除 Redis 用户缓存消息发送失败...", throwable);
                    }
                },
                3000,       // 超时时间
                1);                // 延迟级别，1 表示延时 1s
    }

    /**
     * 删除缓存
     *
     * @param userId
     */
    private void deleteUserRedisCache(Long userId) {
        // 构建 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);
        String userProfileKey = RedisKeyConstants.buildUserProfileKey(userId);
        // 批量删除
        redisTemplate.delete(Arrays.asList(userInfoRedisKey, userProfileKey));
    }

    @Override
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {

        String phone = registerUserReqDTO.getPhone();
        // 判断手机号是否已注册
        UserDO userDO1 = userDOMapper.selectByPhone(phone);
        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(userDO1)) {
            return Response.success(userDO1.getId());
        }
        // 若未注册，则创建新用户
//        Long xiaohongshuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHONGSHU_ID_GENERATOR_KEY);

        // RPC: 调用分布式 ID 生成服务生成小哈书 ID
        String xiaohongshuId = distributedIdGeneratorRpcService.getXiaohongshuId();
        // RPC: 调用分布式 ID 生成服务生成用户 ID
        String userIdStr = distributedIdGeneratorRpcService.getUserId();
        Long userId = Long.valueOf(userIdStr);

        UserDO userDO = UserDO.builder()
                .id(userId)
                .phone(phone)
                .xiaohongshuId(xiaohongshuId)
                .nickname("小红书" + xiaohongshuId)
                .status(StatusEnum.ENABLE.getValue())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeleteEnum.NO.getValue())
                .build();
        // 插入
        userDOMapper.insert(userDO);
        // 获取刚刚添加入库的用户 ID
//        Long userId = userDO.getId();

        // 给该用户分配一个默认角色
        UserRoleDO userRoleDO = UserRoleDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeleteEnum.NO.getValue())
                .build();
        // 用户角色数据库插入
        userRoleDOMapper.insert(userRoleDO);

        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
        // 将该用户的角色 ID 存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());

        String userRoleKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRoleKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneReqDTO
     * @return
     */
    @Override
    public Response<FindUserByPhoneRespDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {

        String phone = findUserByPhoneReqDTO.getPhone();
        // 根据手机号查询用户信息
        UserDO userDO = userDOMapper.selectByPhone(phone);
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        FindUserByPhoneRespDTO findUserByPhoneRespDTO = FindUserByPhoneRespDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();


        return Response.success(findUserByPhoneRespDTO);
    }

    /**
     * 更新用户密码
     *
     * @param updateUserPasswordReqDTO
     * @return
     */
    @Override
    public Response<?> updateUserPassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        Long userId = LoginUserContextHolder.getUserId();

        String encodePassword = updateUserPasswordReqDTO.getEncodePassword();
        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(encodePassword)
                .updateTime(LocalDateTime.now())
                .build();
        // 更新用户密码
        userDOMapper.updateByPrimaryKeySelective(userDO);
        return Response.success();
    }

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param findUserByIdReqDTO
     * @return
     */
    @Override
    public Response<FindUserByIdRespDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        // 获取用户Id
        Long userId = findUserByIdReqDTO.getId();

        // 本地缓存中查询
        FindUserByIdRespDTO findUserByIdRspDTOLocalCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdRspDTOLocalCache)) {
            log.info("==> 命中了本地缓存；{}", findUserByIdRspDTOLocalCache);
            return Response.success(findUserByIdRspDTOLocalCache);
        }

        // 用户缓存 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

        // 先从 Redis 缓存中查询
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);

        // 若缓存中存在
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将存储的字符串转换为 UserDO 对象
            FindUserByIdRespDTO findUserByIdRespDTO = JsonUtils.parseObject(userInfoRedisValue, FindUserByIdRespDTO.class);

            threadPoolTaskExecutor.submit(() -> {
                if (Objects.nonNull(findUserByIdRespDTO)) {
                    // 写入本地缓存中
                    LOCAL_CACHE.put(userId, findUserByIdRespDTO);
                }
            });
            return Response.success(findUserByIdRespDTO);
        }
        // 否则，数据库中查询
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        // 判断用户是否存在
        if (Objects.isNull(userDO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存中(过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireTime = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireTime, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        // 构建返回参数
        FindUserByIdRespDTO findUserByIdRespDTO = FindUserByIdRespDTO.builder()
                .id(userDO.getId())
                .nickName(userDO.getNickname())
                .avatar(userDO.getAvatar())
                .introduction(userDO.getIntroduction())
                .build();

        // 异步将用户信息存入 Redis 缓存，提升响应速度
        threadPoolTaskExecutor.submit(() -> {
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisTemplate.opsForValue()
                    .set(userInfoRedisKey, JsonUtils.toJsonString(findUserByIdRespDTO), expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findUserByIdRespDTO);
    }

    /**
     * 根据用户 ID 列表查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    @Override
    public Response<List<FindUserByIdRespDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        // 获取用户 ID 列表
        List<Long> userIds = findUsersByIdsReqDTO.getIds();

        // 构建 Redis Key 集合
        List<String> redisKeys = userIds.stream()
                .map(RedisKeyConstants::buildUserRoleKey)
                .toList();

        // 从 Redis 缓存中查询，multiGet 批量查询提升性能
        List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
        // 如果缓存不为空
        if (CollUtil.isNotEmpty(redisValues)) {
            // 过滤为空的数据
            redisValues = redisValues.stream().filter(Objects::nonNull).toList();
        }
        // 返回参数
        List<FindUserByIdRespDTO> findUserByIdRespDTOS = Lists.newArrayList();

        // 将过滤后的缓存集合，转换为 DTO 返回参数实体类
        // TODO 这里与文档不符
        if (CollUtil.isNotEmpty(redisValues)) {
            findUserByIdRespDTOS = redisValues.stream()
                    .map(value -> (FindUserByIdRespDTO) JsonUtils.parseObject(value.toString(), FindUserByIdRespDTO.class)).toList();
        }
        // 如果查询用户信息均在 Redis 缓存中，则直接返回
        if (CollUtil.size(userIds) == CollUtil.size(findUserByIdRespDTOS)) {
            return Response.success(findUserByIdRespDTOS);
        }

        // 还有另外两种情况：一种是缓存里没有用户信息数据，还有一种是缓存里数据不全，需要从数据库中补充
        // 筛选出缓存里没有的用户数据，去查数据库
        List<Long> userIdsNeedQuery = null;

        if (CollUtil.isNotEmpty(findUserByIdRespDTOS)) {
            // 将 findUserInfoByIdRespDTOS 集合转为 Map
            Map<Long, FindUserByIdRespDTO> map = findUserByIdRespDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRespDTO::getId, p -> p));

            // 筛选出需要查询 DB 的用户 ID
            userIdsNeedQuery = userIds.stream()
                    .filter(id -> Objects.isNull(map.get(id)))
                    .toList();
        } else {
            userIdsNeedQuery = userIds;
        }

        // 从数据库中批量查询
        List<UserDO> userDOS = userDOMapper.selectByIds(userIdsNeedQuery);
        List<FindUserByIdRespDTO> findUserByIdRespDTOS2 = null;
        ;

        if (CollUtil.isNotEmpty(userDOS)) {
            // DO 转 DTO
            findUserByIdRespDTOS2 = userDOS.stream()
                    .map(userDO -> FindUserByIdRespDTO.builder()
                            .id(userDO.getId())
                            .nickName(userDO.getNickname())
                            .avatar(userDO.getAvatar())
                            .introduction(userDO.getIntroduction())
                            .build())
                    .collect(Collectors.toList());
            // 异步线程将用户信息同步到 Redis 缓存中
            List<FindUserByIdRespDTO> finalFindUserByIdRespDTOS = findUserByIdRespDTOS2;
            threadPoolTaskExecutor.submit(() -> {
                // DTO 集合转 Map
                Map<Long, FindUserByIdRespDTO> map = finalFindUserByIdRespDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRespDTO::getId, p -> p));

                // 执行 pipeline 操作
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        for (UserDO userDO : userDOS) {
                            Long userId = userDO.getId();

                            // 用户信息缓存 Redis Key
                            String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

                            // DTO 转 JSON 字符串
                            FindUserByIdRespDTO findUserInfoByIdRespDTO = map.get(userId);
                            String value = JsonUtils.toJsonString(findUserInfoByIdRespDTO);

                            // 过期时间
                            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                            operations.opsForValue().set(userInfoRedisKey, value, expireSeconds, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });
            });
        }

        // 合并两个结果集
        if (CollUtil.isNotEmpty(findUserByIdRespDTOS2)) {
            findUserByIdRespDTOS.addAll(findUserByIdRespDTOS2);
        }
        return Response.success(findUserByIdRespDTOS);
    }

    /**
     * 查询用户简介
     *
     * @param findUserProfileReqVO
     * @return
     */
    @Override
    public Response<FindUserProfileRespVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO) {
        // 用户Id
        Long userId = findUserProfileReqVO.getUserId();
        // 若入参中用户 ID 为空，则查询当前登录用户
        if (Objects.isNull(userId)) {
            userId = LoginUserContextHolder.getUserId();
        }
        // 1.查询本地缓存
        if (!Objects.equals(userId, LoginUserContextHolder.getUserId())) {
            FindUserProfileRespVO userProfileLocalCache = PROFILE_LOCAL_CACHE.getIfPresent(userId);
            if (Objects.nonNull(userProfileLocalCache)) {
                log.info("## 用户主页信息命中本地缓存: {}", JsonUtils.toJsonString(userProfileLocalCache));
                return Response.success(userProfileLocalCache);
            }
        }
        //  2. 优先查询 Redis 缓存
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);

        String userProfileJson = (String) redisTemplate.opsForValue().get(userProfileRedisKey);
        if (StringUtils.isNotBlank(userProfileJson)) {
            FindUserProfileRespVO findUserProfileRespVO = JsonUtils.parseObject(userProfileJson, FindUserProfileRespVO.class);

            // 异步同步到本地缓存
            asyncUserProfile2LocalCache(userId, findUserProfileRespVO);
            return Response.success(findUserProfileRespVO);
        }
        //  3. 再查询数据库
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        FindUserProfileRespVO findUserProfileRespVO = FindUserProfileRespVO.builder()
                .userId(userId)
                .avatar(userDO.getAvatar())
                .nickname(userDO.getNickname())
                .xiaohongshuId(userDO.getXiaohongshuId())
                .sex(userDO.getSex())
                .introduction(userDO.getIntroduction())
                .build();
        // 年龄计算
        LocalDate birthday = userDO.getBirthday();
        findUserProfileRespVO.setAge(Objects.isNull(birthday) ? 0 : DateUtils.calculateAge(birthday));
        // Rpc Feign 调用计数服务
        FindUserCountsByIdRespDTO findUserCountsByIdRspDTO = countRpcService.findUserCountById(userId);

        if (Objects.nonNull(findUserCountsByIdRspDTO)) {
            Long fansTotal = findUserCountsByIdRspDTO.getFansTotal();
            Long followingTotal = findUserCountsByIdRspDTO.getFollowingTotal();
            Long likeTotal = findUserCountsByIdRspDTO.getLikeTotal();
            Long noteTotal = findUserCountsByIdRspDTO.getNoteTotal();
            Long collectTotal = findUserCountsByIdRspDTO.getCollectTotal();

            findUserProfileRespVO.setFansTotal(NumberUtils.formatNumberString(fansTotal));
            findUserProfileRespVO.setFollowingTotal(NumberUtils.formatNumberString(followingTotal));
            findUserProfileRespVO.setLikeTotal(NumberUtils.formatNumberString(likeTotal));
            findUserProfileRespVO.setLikeAndCollectTotal(NumberUtils.formatNumberString(likeTotal + collectTotal));
            findUserProfileRespVO.setCollectTotal(NumberUtils.formatNumberString(collectTotal));
            findUserProfileRespVO.setNoteTotal(NumberUtils.formatNumberString(noteTotal));

            // 异步同步到 Redis 中
            asyncUserProfile2Redis(userProfileRedisKey, findUserProfileRespVO);
        }
        return Response.success(findUserProfileRespVO);
    }

    /**
     * 异步同步 本地缓存
     *
     * @param userId
     * @param findUserProfileRespVO
     */
    private void asyncUserProfile2LocalCache(Long userId, FindUserProfileRespVO findUserProfileRespVO) {
        threadPoolTaskExecutor.submit(() -> {
            PROFILE_LOCAL_CACHE.put(userId, findUserProfileRespVO);
        });
    }

    /**
     * 异步同步 Redis
     *
     * @param userProfileRedisKey
     * @param findUserProfileRespVO
     */
    private void asyncUserProfile2Redis(String userProfileRedisKey, FindUserProfileRespVO findUserProfileRespVO) {
        threadPoolTaskExecutor.submit(() -> {
            // 设置过期时间
            long expireSeconds = 60 * 60 + RandomUtil.randomInt(60 * 60);

            // 将 VO 转为 Json 字符串写入到 Redis 中
            redisTemplate.opsForValue()
                    .set(userProfileRedisKey, JsonUtils.toJsonString(findUserProfileRespVO), expireSeconds);
        });
    }
}
