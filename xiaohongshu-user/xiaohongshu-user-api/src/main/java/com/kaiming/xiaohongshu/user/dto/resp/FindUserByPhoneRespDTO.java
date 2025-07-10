package com.kaiming.xiaohongshu.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserByPhoneRespDTO
 * Package: com.kaiming.xiaohongshu.user.dto.resp
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 20:19
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByPhoneRespDTO {
    
    private Long id;
    private String password;
}
