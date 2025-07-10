package com.kaiming.xiaohongshu.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AddNoteContentReqDTO
 * Package: com.kaiming.xiaohongshu.kv.biz.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 18:20
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddNoteContentReqDTO {
    @NotNull(message = "笔记ID不能为空")
    private String uuid;
    @NotBlank(message = "内容不能为空")
    private String content;
}
