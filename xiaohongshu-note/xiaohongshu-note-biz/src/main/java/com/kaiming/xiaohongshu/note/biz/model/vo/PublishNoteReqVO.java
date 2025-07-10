package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: PublishNoteReqVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 20:29
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishNoteReqVO {

    @NotNull(message = "笔记类型不能为空")
    private Integer type;

    private List<String> imgUris;

    private String videoUri;

    private String title;

    private String content;

    private Long topicId;
}
