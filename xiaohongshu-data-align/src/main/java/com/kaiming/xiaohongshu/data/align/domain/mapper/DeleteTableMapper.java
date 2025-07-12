package com.kaiming.xiaohongshu.data.align.domain.mapper;

/**
 * ClassName: DeleteTableMapper
 * Package: com.kaiming.xiaohongshu.data.align.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 18:44
 * @Version 1.0
 */
public interface DeleteTableMapper {

    /**
     * 删除日增量表：关注数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignFollowingCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：粉丝数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignFansCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记收藏数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignNoteCollectCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：用户收藏数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignUserCollectCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：用户点赞数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignUserLikeCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记点赞数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignNoteLikeCountTempTable(String tableNameSuffix);

    /**
     * 删除日增量表：笔记发布数计数变更
     * @param tableNameSuffix
     */
    void deleteDataAlignNotePublishCountTempTable(String tableNameSuffix);
}
