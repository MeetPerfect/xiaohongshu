package com.kaiming.xiaohongshu.search.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.xiaohongshu.search.service.ExtDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ExtDictController
 * Package: com.kaiming.xiaohongshu.search.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 19:15
 * @Version 1.0
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class ExtDictController {
    
    @Resource
    private ExtDictService extDictService;
    
    @GetMapping("/ext/dict")
    @ApiOperationLog(description = "获取热更新词典")
    public ResponseEntity<String> extDict() {
        return extDictService.getHotUpdateExtDict();
    }
}
