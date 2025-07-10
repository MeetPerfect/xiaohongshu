package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UnlikeNoteReqVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/8 18:46
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnlikeNoteReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;
}
