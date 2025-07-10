package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindFansUserRespVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 16:10
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFansUserRespVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 粉丝总数
     */
    private Long fansTotal;

    /**
     * 笔记总数
     */
    private Long noteTotal;


}
