package com.kaiming.xiaohongshu.data.align.domain.mapper;

/**
 * ClassName: CreateTableMapper
 * Package: com.kaiming.xiaohongshu.data.align.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 10:39
 * @Version 1.0
 */
public interface CreateTableMapper {

    /**
     * 创建日增量表：粉丝计数变更
     * @param tableNameSuffix
     */
    void createDataAlignFansCountTempTable(String tableNameSuffix);
    
    /**
     * 创建日增量表：笔记收藏计数变更
     * @param tableNameSuffix
     */
    void createDataAlignNoteCollectCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：用户收藏数计数变更
     * @param tableNameSuffix
     */
    void createDataAlignUserCollectCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：用户点赞数计数变更
     * @param tableNameSuffix
     */
    void createDataAlignUserLikeCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：笔记点赞数计数变更
     * @param tableNameSuffix
     */
    void createDataAlignNoteLikeCountTempTable(String tableNameSuffix);

    /**
     * 创建日增量表：笔记发布数计数变更
     * @param tableNameSuffix
     */
    void createDataAlignNotePublishCountTempTable(String tableNameSuffix);
    /**
     * 创建日增量表：关注数计数变更
     * @param tableNameSuffix
     */
    void createDataAlignFollowingCountTempTable(String tableNameSuffix);
}
