package com.kaiming.xiaohongshu.note.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FindPublishedNoteListRespVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 17:54
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindPublishedNoteListRespVO {

    /**
     * 笔记分页数据
     */
    private List<NoteItemRespVO> notes;

    /**
     * 下一页的游标
     */
    private Long nextCursor;

}
