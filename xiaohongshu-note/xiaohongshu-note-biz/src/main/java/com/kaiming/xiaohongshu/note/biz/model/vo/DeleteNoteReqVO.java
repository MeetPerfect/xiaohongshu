package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: DeleteNoteReqVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/26 22:35
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteNoteReqVO {

    @NotNull(message = "笔记ID不能为空")
    private Long id;

}
