package com.kaiming.xiaohongshu.kv.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: BatchFindCommentContentReqDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 13:56
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchFindCommentContentReqDTO {

    /**
     * 笔记 ID
     */
    @NotNull(message = "评论 ID 不能为空")
    private Long noteId;

    @NotEmpty(message = "评论内容 Key 集合")
    @Valid  // 指定集合中的 DTO 也需要进行参数校验
    private List<FindCommentContentReqDTO> commentContentKeys;
}
