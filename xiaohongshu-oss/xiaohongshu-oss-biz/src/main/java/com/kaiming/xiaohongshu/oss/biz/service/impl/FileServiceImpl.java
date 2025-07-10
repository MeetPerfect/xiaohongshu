package com.kaiming.xiaohongshu.oss.biz.service.impl;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.oss.biz.service.FileService;
import com.kaiming.xiaohongshu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileServiceimpl
 * Package: com.kaiming.xiaohongshu.oss.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 13:31
 * @Version 1.0
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    
    @Resource
    private FileStrategy fileStrategy;
    private static final String BUCKET_NAME = "xiaohongshu";
    @Override
    public Response<?> uploadFile(MultipartFile file) {
        // 上传文件
        String url = fileStrategy.uploadFile(file, BUCKET_NAME);
        return Response.success(url);
    }
}
