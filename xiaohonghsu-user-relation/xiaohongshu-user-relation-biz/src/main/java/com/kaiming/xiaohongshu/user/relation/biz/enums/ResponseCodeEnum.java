package com.kaiming.xiaohongshu.user.relation.biz.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 11:06
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("RELATION-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("RELATION-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    CANT_FOLLOW_YOUR_SELF("RELATION-20001", "无法关注自己"),
    FOLLOW_USER_NOT_EXISTED("RELATION-20002", "关注的用户不存在"),
    FOLLOWING_COUNT_LIMIT("RELATION-20003", "您关注的用户已达上限，请先取关部分用户"),
    ALREADY_FOLLOWED("RELATION-20004", "您已经关注了该用户"),
    CANT_UNFOLLOW_YOUR_SELF("RELATION-20005", "无法取关自己"),
    NOT_FOLLOWED("RELATION-20006", "你未关注对方，无法取关"),
    ;
    
    
    private final String errorCode;
    private final String errorMessage;
}
