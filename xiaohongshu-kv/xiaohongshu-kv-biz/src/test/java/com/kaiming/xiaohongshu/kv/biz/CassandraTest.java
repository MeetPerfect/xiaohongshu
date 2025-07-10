package com.kaiming.xiaohongshu.kv.biz;

import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.NoteContentDO;
import com.kaiming.xiaohongshu.kv.biz.domain.repository.NoteContentRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * ClassName: CassandraTest
 * Package: com.kaiming.xiaohongshu.kv.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 18:06
 * @Version 1.0
 */
@SpringBootTest
public class CassandraTest {
    @Resource
    private NoteContentRepository noteContentRepository;

    @Test
    public void test() {
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.randomUUID())
                .content("测试内容")
                .build();
        noteContentRepository.save(noteContentDO);
    }
}
