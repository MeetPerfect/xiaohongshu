package com.kaiming.xiaohongshu.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindNoteContentReqDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 21:22
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentReqDTO {
    
    @NotBlank(message = "笔记Id不能为空")
    private String uuid;
}
