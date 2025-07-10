package com.kaiming.xiaohongshu.note.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.model.vo.*;

/**
 * ClassName: NoteService
 * Package: com.kaiming.xiaohongshu.note.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 22:15
 * @Version 1.0
 */
public interface NoteService {

    /**
     * 发布笔记
     *
     * @param publishNoteReqVO
     * @return
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

    /**
     * 根据笔记ID实体类查询笔记详情
     *
     * @param findNoteDetailReqVO
     * @return
     */
    Response<FindNoteDetailRespVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);

    /**
     * 删除本地笔记缓存
     *
     * @param noteId
     */
    void deleteNoteLocalCache(Long noteId);

    /**
     * 删除笔记
     *
     * @param deleteNoteReqVO
     */
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    /**
     * 更新笔记可见性为仅自己可见
     *
     * @param updateNoteVisibleOnlyMeReqVO
     * @return
     */
    Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);

    /**
     * 置顶笔记
     *
     * @param topNoteReqVO
     * @return
     */
    Response<?> topNote(TopNoteReqVO topNoteReqVO);

    /**
     * 点赞笔记
     * @param likeNoteReqVO
     * @return
     */
    Response<?> likeNote(LikeNoteReqVO likeNoteReqVO);

    /**
     * 取消点赞笔记
     * @param unlikeNoteReqVO
     * @return
     */
    Response<?> unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO);

    /**
     * 收藏笔记
     * @param collectNoteReqVO
     * @return
     */
    Response<?> collectNote(CollectNoteReqVO collectNoteReqVO);

    /**
     * 取消收藏笔记
     * @param unCollectNoteReqVO
     * @return
     */
    Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);
}
