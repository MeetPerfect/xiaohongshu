package com.kaiming.xiaohongshu.data.align.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: SelectMapper
 * Package: com.kaiming.xiaohongshu.data.align.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 20:25
 * @Version 1.0
 */
public interface SelectMapper {

    /**
     * 日增量表：关注数计数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                               @Param("batchSize") int batchSize);

    /**
     * 查询 t_following 关注表，获取关注总数
     *
     * @param userId
     * @return
     */
    int selectCountFromFollowingTableByUserId(long userId);

    /**
     * 日增量表：笔记点赞数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                              @Param("batchSize") int batchSize);

    /**
     * 查询 t_note_like 笔记点赞表，获取点赞总数
     *
     * @param noteId
     * @return
     */
    int selectCountFromNoteLikeTableByUserId(long noteId);

    /**
     * 日增量表：笔记收藏数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNoteCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("batchSize") int batchSize);

    /**
     * 查询 t_note_collection 笔记收藏表，获取收藏总数
     *
     * @param noteId
     * @return
     */
    int selectCountFromNoteCollectionTableByUserId(long noteId);

    /**
     * 日增量表：粉丝计数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("batchSize") int batchSize);

    /**
     * 查询 t_fans 粉丝表，获取粉丝总数
     *
     * @param userId
     * @return
     */
    int selectCountFromFansTableByUserId(long userId);

    /**
     * 日增量表：用户点赞数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("batchSize") int batchSize);

    /**
     * 查询 t_note_like 笔记点赞表，获取点赞总数
     *
     * @param userId
     * @return
     */
    int selectUserLikeCountFromNoteLikeTableByUserId(long userId);

    /**
     * 日增量表：用户收藏数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("batchSize") int batchSize);

    /**
     * 查询 t_note_collection 笔记收藏表，获取用户获得的收藏总数
     */
    int selectUserCollectCountFromNoteCollectionTableByUserId(long userId);

    /**
     * 日增量表：笔记发布数变更 - 批量查询
     *
     * @param tableNameSuffix
     * @param batchSize
     * @return
     */
    List<Long> selectBatchFromDataAlignNotePublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("batchSize") int batchSize);

    /**
     * 查询 t_note 笔记表，获取笔记发布总数
     * @param userId
     * @return
     */
    int selectCountFromNoteTableByUserId(long userId);
}
