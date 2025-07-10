package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindFollowingUserRespVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/2 21:33
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFollowingUserRespVO {

    private Long userId;

    private String avatar;

    private String nickname;

    private String introduction;
}
