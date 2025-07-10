package com.kaiming.xiaohongshu.oss.biz.controller;

import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.oss.biz.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: FileController
 * Package: com.kaiming.xiaohongshu.oss.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 13:35
 * @Version 1.0
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private FileService fileService;
    
    @RequestMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> uploadFile(@RequestPart(value = "file") MultipartFile file) {
        log.info("当前用户 ID: {}", LoginUserContextHolder.getUserId());
        return fileService.uploadFile(file); 
    }
}
