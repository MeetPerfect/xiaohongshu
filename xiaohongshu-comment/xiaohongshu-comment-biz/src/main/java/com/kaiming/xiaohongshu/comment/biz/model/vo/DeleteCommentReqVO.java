package com.kaiming.xiaohongshu.comment.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: DeleteCommentReqVO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 14:45
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteCommentReqVO {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;
}
