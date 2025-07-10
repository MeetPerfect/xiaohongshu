package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UpdateNoteVisibleOnlyMeReqVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/27 12:27
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNoteVisibleOnlyMeReqVO {
    
    @NotNull(message = "笔记ID不能为空")
    private Long id;
}
