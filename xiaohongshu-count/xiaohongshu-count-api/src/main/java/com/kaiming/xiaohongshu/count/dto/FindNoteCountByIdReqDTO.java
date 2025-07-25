package com.kaiming.xiaohongshu.count.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FindNoteCountByIdReqDTO
 * Package: com.kaiming.xiaohongshu.count.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 19:06
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteCountByIdReqDTO {
    
    @NotNull(message = "笔记集合Ids不能为空")
    @Size(min = 1, max = 20, message = "笔记 ID 集合大小必须大于等于 1, 小于等于 20")
    private List<Long> noteIds;
}
