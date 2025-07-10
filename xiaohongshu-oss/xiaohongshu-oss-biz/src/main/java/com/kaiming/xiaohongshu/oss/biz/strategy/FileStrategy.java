package com.kaiming.xiaohongshu.oss.biz.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileStrategy
 * Package: com.kaiming.xiaohongshu.oss.biz.strategy
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 12:56
 * @Version 1.0
 */
public interface FileStrategy {

    /**
     * 文件上传
     * @param file
     * @param bucketName
     * @return
     */
    String uploadFile(MultipartFile file, String bucketName);
}
