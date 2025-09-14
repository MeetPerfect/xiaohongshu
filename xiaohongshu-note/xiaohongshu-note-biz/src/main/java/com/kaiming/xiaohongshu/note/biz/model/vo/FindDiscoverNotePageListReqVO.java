package com.kaiming.xiaohongshu.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: FindDiscoverNotePageListRespVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 15:26
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FindDiscoverNotePageListReqVO {

    /**
     * 频道Id
     */
    private Long channelId;
    
    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;
}
