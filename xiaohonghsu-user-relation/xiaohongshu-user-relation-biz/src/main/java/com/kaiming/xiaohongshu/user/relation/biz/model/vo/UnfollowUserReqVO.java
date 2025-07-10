package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UnfollowUserReqVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/29 22:02
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnfollowUserReqVO {

    @NotNull(message = "被取关用户 ID 不能为空")
    private Long unfollowUserId;
}
