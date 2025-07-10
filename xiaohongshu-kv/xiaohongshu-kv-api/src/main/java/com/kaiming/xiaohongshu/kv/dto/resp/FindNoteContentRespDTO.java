package com.kaiming.xiaohongshu.kv.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ClassName: FindNoteContentRespDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.resp
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 21:22
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentRespDTO {

    /**
     * 笔记 ID
     */
    private UUID uuid;

    /**
     * 笔记内容
     */
    private String content;
}
