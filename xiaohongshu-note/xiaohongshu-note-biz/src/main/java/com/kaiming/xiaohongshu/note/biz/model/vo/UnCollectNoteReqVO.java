package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UnCollectNoteReqVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 14:59
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnCollectNoteReqVO {
    
    @NotNull(message = "笔记ID不能为空")    
    private Long id;
}
