package com.kaiming.xiaohongshu.kv.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * ClassName: CommentContentDO
 * Package: com.kaiming.xiaohongshu.kv.biz.domain.dataobject
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 21:59
 * @Version 1.0
 */
@Table("comment_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentContentDO {
    @PrimaryKey
    private CommentContentPrimaryKey primaryKey;

    private String content;
}
