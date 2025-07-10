package com.kaiming.xiaohongshu.kv.biz.domain.repository;

import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * ClassName: NoteContentRepository
 * Package: com.kaiming.xiaohongshu.kv.biz.domain.repository
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 17:43
 * @Version 1.0
 */
public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {
    
    
}
