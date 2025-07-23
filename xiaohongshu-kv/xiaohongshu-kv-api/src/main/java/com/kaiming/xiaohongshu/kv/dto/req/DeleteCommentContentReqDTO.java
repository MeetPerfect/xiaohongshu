package com.kaiming.xiaohongshu.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: DeleteCommentContentReqDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 14:14
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteCommentContentReqDTO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

    @NotBlank(message = "发布年月不能为空")
    private String yearMonth;

    @NotBlank(message = "评论正文 ID 不能为空")
    private String contentId;
}
