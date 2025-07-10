package com.kaiming.xiaohongshu.oss.biz.service;

import com.kaiming.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileService
 * Package: com.kaiming.xiaohongshu.oss.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 13:31
 * @Version 1.0
 */
public interface FileService {
    
    Response<?> uploadFile(MultipartFile file);
}
