package com.kaiming.xiaohongshu.search.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* ClassName: SelectMapper
* Package: com.kaiming.xiaohongshu.search.domain.mapper
* Description:
* @Auther gongkaiming
* @Create 2025/7/17 14:46
* @Version 1.0
*/    
public interface SelectMapper {

    /**
     * 查询笔记文档所需的全字段数据
     * @param noteId
     * @return
     */
    List<Map<String, Object>> selectEsNoteIndexData(@Param("noteId") Long noteId);
}
