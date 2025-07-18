package com.kaiming.xiaohongshu.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: CommentContentReqDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 21:57
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentContentReqDTO {

    @NotNull(message = "评论 ID 不能为空")
    private Long noteId;

    @NotBlank(message = "发布年月不能为空")
    private String yearMonth;

    @NotBlank(message = "评论正文 ID 不能为空")
    private String contentId;

    @NotBlank(message = "评论正文不能为空")
    private String content;
}
