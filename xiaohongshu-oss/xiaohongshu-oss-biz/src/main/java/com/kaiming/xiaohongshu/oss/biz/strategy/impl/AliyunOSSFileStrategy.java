package com.kaiming.xiaohongshu.oss.biz.strategy.impl;

import com.aliyun.oss.OSS;
import com.kaiming.xiaohongshu.oss.biz.config.AliyunOSSProperties;
import com.kaiming.xiaohongshu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
/**
 * ClassName: AliyunOSSFileStrategy
 * Package: com.kaiming.xiaohongshu.oss.biz.strategy.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 12:57
 * @Version 1.0
 */
@Slf4j
public class AliyunOSSFileStrategy implements FileStrategy {

    @Resource
    private AliyunOSSProperties aliyunOSSProperties;
    
    @Resource
    private OSS ossClient;
    
    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件至阿里云 OSS ...");
        
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }
        String originalFilename = file.getOriginalFilename();

        // 生成存储对象的名称（将 UUID 字符串中的 - 替换成空字符串）
        String key = UUID.randomUUID().toString().replace("-", "");
        
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = String.format("%s%s", key, suffix);

        log.info("==> 开始上传文件至阿里云 OSS, ObjectName: {}", objectName);

        // 上传文件至阿里云 OSS
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(file.getInputStream().readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 返回文件的访问链接
        String url = String.format("https://%s.%s/%s", bucketName, aliyunOSSProperties.getEndpoint(), objectName);
        log.info("==> 上传文件至阿里云 OSS 成功，访问路径: {}", url);
        return url;
    }
}
