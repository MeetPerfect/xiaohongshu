package com.kaiming.xiaohongshu.count.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FindUserCountByIdReqDTO
 * Package: com.kaiming.xiaohongshu.count.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 19:57
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserCountsByIdsReqDTO {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private List<Long> userIds;
}
