package com.kaiming.xiaohongshu.user.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FindUsersByIdReqDTO
 * Package: com.kaiming.xiaohongshu.user.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/1 21:08
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUsersByIdsReqDTO {
    
    @NotNull(message = "用户ID列表不能为空")
    @Size(min = 1, max = 10, message = "用户 ID 集合大小必须大于等于1, 小于等于10")
    private List<Long> ids;
}
