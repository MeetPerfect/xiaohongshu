package com.kaiming.xiaohongshu.search.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: RebuildUserDocumentReqDTO
 * Package: com.kaiming.xiaohongshu.search.api.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 22:20
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RebuildUserDocumentReqDTO {

    @NotNull(message = "用户 ID 不能为空")
    private Long id;
}
