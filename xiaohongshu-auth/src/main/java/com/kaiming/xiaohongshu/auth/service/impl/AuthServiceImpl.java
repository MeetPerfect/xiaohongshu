package com.kaiming.xiaohongshu.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.auth.enums.LoginTypeEnum;
import com.kaiming.xiaohongshu.auth.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.auth.model.vo.user.UpdatePasswordReqVO;
import com.kaiming.xiaohongshu.auth.model.vo.user.UserLoginReqVO;
import com.kaiming.xiaohongshu.auth.rpc.UserRpcService;
import com.kaiming.xiaohongshu.auth.service.AuthService;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * ClassName: UserServiceImpl
 * Package: com.kaiming.xiaohongshu.auth.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 22:11
 * @Version 1.0
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserRpcService userRpcService;

    /**
     * 用户登录和注册
     *
     * @param userLoginReqVO
     * @return
     */
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        
        // 登录类型错误
        if (Objects.isNull(loginTypeEnum)) {
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }
        
        Long userId = null;

        // 判断登录类型
        switch (loginTypeEnum) {
            // 验证码登录
            case VERIFICATION_CODE:
                String verificationCode = userLoginReqVO.getCode();
                // 校验参数验证码是否为空
//                if (StringUtils.isBlank(verificationCode)) {
//                    return Response.fail(ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode(), "验证码不能为空");
//                }
                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode), "验证码不能为空");
                // 构建验证码 Redis Key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                // 从 Redis 中获取该用户的登录验证码
                String sendCode = (String) redisTemplate.opsForValue().get(key);
                if (!StringUtils.equals(verificationCode, sendCode)) {
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }
//                // 通过手机号查询用户
//                UserDO userDO = userDOMapper.selectByPhone(phone);
//                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));
//
//                // 判断是否注册
//                if (Objects.isNull(userDO)) {
//                    // 未注册，系统自动注册用户
//                    userId = registerUser(phone);
//                } else {
//                    userId = userDO.getId();
//                }
                
                // RPC 调用用户服务
                Long userIdTmp = userRpcService.registerUser(phone);
                // 判断用户ID是否为空
                if (Objects.isNull(userIdTmp)) {
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }
                userId = userIdTmp;
                break;
            case PASSWORD:
                // 密码登录
                String password = userLoginReqVO.getPassword();
                
                // RPC 调用用户服务，通过手机号查询用户
                FindUserByPhoneRespDTO findUserByPhoneRespDTO = userRpcService.findUserByPhoneRespDTO(phone);
                
                // 根据手机号查询
//                UserDO userDO1 = userDOMapper.selectByPhone(phone);
                if (Objects.isNull(findUserByPhoneRespDTO)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }
                // 获取数据库中的用户加密密码
                String encodePassword = findUserByPhoneRespDTO.getPassword();
//                String encodePassword = userDO1.getPassword();
                
                boolean isPasswordCorrect = passwordEncoder.matches(password, encodePassword);
                if (!isPasswordCorrect) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }
//                userId = userDO1.getId();
                userId = findUserByPhoneRespDTO.getId();
                break;
            default:
                break;
        }
        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    /**
     * 用户退出
     *
     * @return
     */
    @Override
    public Response<?> logout() {
        // 指定用户ID退出
        Long userId = LoginUserContextHolder.getUserId();
        log.info("==> 用户退出登录, userId: {}", userId);
        threadPoolTaskExecutor.submit(() -> {
            Long userId1 = LoginUserContextHolder.getUserId();
            log.info("==> 异步线程中获取 userId: {}", userId1);
        });

        StpUtil.logout(userId);
        return Response.success();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        
        // 新密码
        String newPassword = updatePasswordReqVO.getNewPassword();
        // 密码加密
        String encodePassword = passwordEncoder.encode(newPassword);
        
//        // 获取当前请求的用户Id
//        Long userId = LoginUserContextHolder.getUserId();
//        UserDO userDO = UserDO.builder()
//                .id(userId)
//                .password(encodePassword)
//                .updateTime(LocalDateTime.now())
//                .build();
//        // 更新数据库中的密码
//        userDOMapper.updateByPrimaryKeySelective(userDO);
        // RPC 调用用户服务更新密码
        userRpcService.updatePassword(encodePassword);
        return Response.success();
    }

    /**
     * 系统自动注册用户
     *
     * @param phone
     * @return
     */
//    public Long registerUser(String phone) {
//        return transactionTemplate.execute(status -> {
//            try {
//                // 获取全局自增的小红书ID
//                Long xiaohongshuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHONGSHU_ID_GENERATOR_KEY);
//
//                UserDO userDO = UserDO.builder()
//                        .phone(phone)
//                        .xiaohashuId(String.valueOf(xiaohongshuId))
//                        .nickname("小红书用户" + xiaohongshuId)
//                        .status(StatusEnum.ENABLE.getValue())
//                        .createTime(LocalDateTime.now())
//                        .updateTime(LocalDateTime.now())
//                        .isDeleted(DeleteEnum.NO.getValue())
//                        .build();
//                // 插入用户信息
//
//                userDOMapper.insert(userDO);
//                // 给用户分配默认角色
//                Long userId = userDO.getId();
//                UserRoleDO userRoleDO = UserRoleDO.builder()
//                        .userId(userId)
//                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
//                        .createTime(LocalDateTime.now())
//                        .updateTime(LocalDateTime.now())
//                        .isDeleted(DeleteEnum.NO.getValue())
//                        .build();
//                // 插入用户角色信息
//                userRoleDOMapper.insert(userRoleDO);
//
//                RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
//
//                // 将该用户的角色ID存入到Redis中
//                List<String> roles = new ArrayList<>(1);
//                roles.add(roleDO.getRoleKey());
//
//                String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
//                redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));
//                return userId;
//            } catch (Exception e) {
//                status.setRollbackOnly();
//                log.error("==> 系统注册用户异常: ", e);
//                throw new RuntimeException(e);
//            }
//        });
//    }
}
