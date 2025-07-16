package com.kaiming.xiaohongshu.search.model.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: SearchUserReqVO
 * Package: com.kaiming.xiaohongshu.search.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 11:13
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserReqVO {

    @NotBlank(message = "搜索关键字不能为空")
    private String keyword;
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;
}
