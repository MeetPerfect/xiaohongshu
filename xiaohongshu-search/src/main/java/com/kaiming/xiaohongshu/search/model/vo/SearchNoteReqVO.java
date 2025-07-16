package com.kaiming.xiaohongshu.search.model.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* ClassName: SearchNoteReqVO
* Package: com.kaiming.xiaohongshu.search.model.vo
* Description:
* @Auther gongkaiming
* @Create 2025/7/16 14:14
* @Version 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchNoteReqVO {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNo = 1; // 默认值为第一页
}
