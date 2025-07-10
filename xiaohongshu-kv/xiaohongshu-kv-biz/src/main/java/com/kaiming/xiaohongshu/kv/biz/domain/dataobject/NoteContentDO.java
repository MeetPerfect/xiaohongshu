package com.kaiming.xiaohongshu.kv.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

/**
 * ClassName: NoteContentDO
 * Package: com.kaiming.xiaohongshu.kv.biz.domain.dataobject
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 17:42
 * @Version 1.0
 */
@Table("note_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteContentDO {
    @PrimaryKey("id")
    private UUID id;
    private String content;
}
