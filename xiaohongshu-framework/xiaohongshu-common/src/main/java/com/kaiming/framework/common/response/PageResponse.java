package com.kaiming.framework.common.response;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PageResponse
 * Package: com.kaiming.framework.common.response
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/2 21:35
 * @Version 1.0
 */
@Data
public class PageResponse<T> extends Response<T> {
    
    private long pageNo;    // 当前页
    private long totalCount;    // 总数据量
    private long pageSize;      // 每页展示的数据量
    private long totalPage;     // 总页数
    
    public static <T> PageResponse<T> success(List<T> data, long pageNo, long totalCount) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setSuccess(true);
        pageResponse.setData((T) data);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        
        // 每页展示的数据量
        long pageSize = 10L;
        pageResponse.setPageSize(pageSize);
        // 计算总页数
        long totalPage = (totalCount + pageSize - 1) / pageSize; // 向上取整
        pageResponse.setTotalPage(totalPage);
        return pageResponse;
    }

    public static <T> PageResponse<T> success(List<T> data, long pageNo, long totalCount, long pageSize) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setSuccess(true);
        pageResponse.setData((T) data);
        pageResponse.setPageNo(pageNo);
        pageResponse.setTotalCount(totalCount);
        pageResponse.setPageSize(pageSize);
        // 计算总页数
        long totalPage = pageSize == 0 ? 0 : (totalCount + pageSize - 1) / pageSize;
        pageResponse.setTotalPage(totalPage);
        return pageResponse;
    }

    /**
     * 计算总页数
     * @param totalCount
     * @param pageSize
     * @return
     */
    public static long getTotalPage(long totalCount, long pageSize) {
        return pageSize == 0 ? 0 : (totalCount + pageSize - 1) / pageSize; // 向上取整
    }

    /**
     * 计算当前页的 offset
     * @param pageNo
     * @param pageSize
     * @return
     */
    public static long getOffset(long pageNo, long pageSize) {
        // 如果页码小于1，默认返回第一页的 offset
        if(pageNo < 1) {
            pageNo = 1;
        }
        return (pageNo - 1) * pageSize;
    }
}
