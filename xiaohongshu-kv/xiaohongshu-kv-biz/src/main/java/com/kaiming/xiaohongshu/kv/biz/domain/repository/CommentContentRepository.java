package com.kaiming.xiaohongshu.kv.biz.domain.repository;

import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.CommentContentDO;
import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.CommentContentPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;
/**
 * ClassName: CommentContentRepository
 * Package: com.kaiming.xiaohongshu.kv.biz.domain.repository
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 13:58
 * @Version 1.0
 */
public interface CommentContentRepository extends CassandraRepository<CommentContentDO, CommentContentPrimaryKey> {


    /**
     * 批量查询评论内容
     * @param noteId
     * @param yearMonths
     * @param contentIds
     * @return
     */
    List<CommentContentDO> findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(
            Long noteId, List<String> yearMonths, List<UUID> contentIds
    );
}
