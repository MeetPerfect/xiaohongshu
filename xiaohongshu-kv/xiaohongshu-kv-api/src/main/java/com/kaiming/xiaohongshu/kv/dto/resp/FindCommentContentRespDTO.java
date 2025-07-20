package com.kaiming.xiaohongshu.kv.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindCommentContentRspDTO
 * Package: com.kaiming.xiaohongshu.kv.dto.resp
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 13:58
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentRespDTO {

    /**
     * 评论内容 UUID
     */
    private String contentId;

    /**
     * 评论内容
     */
    private String content;
}
