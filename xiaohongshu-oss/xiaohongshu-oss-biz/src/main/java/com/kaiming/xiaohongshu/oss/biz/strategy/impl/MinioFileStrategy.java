package com.kaiming.xiaohongshu.oss.biz.strategy.impl;

import com.kaiming.xiaohongshu.oss.biz.config.MinioProperties;
import com.kaiming.xiaohongshu.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ClassName: MinioFileStrategy
 * Package: com.kaiming.xiaohongshu.oss.biz.strategy.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 12:57
 * @Version 1.0
 */
@Slf4j
public class MinioFileStrategy implements FileStrategy {
    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioProperties minioProperties;
    
    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件至 Minio ...");

        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }
        // 文件的原始名称
        String originalFilename = file.getOriginalFilename();

        // 文件类型
        String contentType = file.getContentType();
        // 生成存储对象的名称（将 UUID 字符串中的 - 替换成空字符串）
        String key = UUID.randomUUID().toString().replace("-", "");
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        String objectName = String.format("%s%s", key, suffix);
        log.info("==> 开始上传文件至 Minio, ObjectName: {}", objectName);
        
        // 上传文件至 Minio
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("==> 上传文件至 Minio 异常: {}", e.getMessage());
        }
        String url = String.format("%s%s%s",  minioProperties.getEndpoint(), bucketName, objectName);
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
