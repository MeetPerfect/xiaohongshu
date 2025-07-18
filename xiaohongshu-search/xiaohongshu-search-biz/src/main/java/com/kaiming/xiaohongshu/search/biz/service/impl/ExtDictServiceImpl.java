package com.kaiming.xiaohongshu.search.biz.service.impl;

import com.kaiming.xiaohongshu.search.biz.service.ExtDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * ClassName: ExtDictServiceImpl
 * Package: com.kaiming.xiaohongshu.search.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 19:03
 * @Version 1.0
 */
@Service
@Slf4j
public class ExtDictServiceImpl implements ExtDictService {

    @Value("${elasticsearch.hotUpdateExtDict}")
    private String hotUpdateExtDict;

    /**
     * 获取热更新词典
     *
     * @return
     */
    @Override
    public ResponseEntity<String> getHotUpdateExtDict() {

        try {
            // 获取热更新词典
            Path path = Paths.get(hotUpdateExtDict);
            // 获取文件的最后修改时间
            long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
            // 生成 ETag（使用文件内容的哈希值）
            String fileContent = Files.lines(path).collect(Collectors.joining("\n"));

            String eTag = String.valueOf(fileContent.hashCode());
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.set("ETag", eTag);
            
            // 设置内容类型为 UTF-8
            headers.setContentType(MediaType.valueOf("text/plain;charset=UTF-8"));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .lastModified(lastModifiedTime)
                    .body(fileContent);
        } catch (Exception e) {
            log.error("==> 获取热更新词典异常: ", e);
        }
        return null;
    }
}
