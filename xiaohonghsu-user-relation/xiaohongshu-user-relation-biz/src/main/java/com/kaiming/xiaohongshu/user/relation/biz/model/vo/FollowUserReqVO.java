package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FollowUserReqVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:10
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUserReqVO {
    
    @NotNull(message = "被关注的用户Id不能为空")
    private Long followUserId;
}
