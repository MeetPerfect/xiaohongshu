package com.kaiming.xiaohongshu.search.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.NumberUtils;
import com.kaiming.xiaohongshu.search.dto.RebuildUserDocumentReqDTO;
import com.kaiming.xiaohongshu.search.biz.domain.mapper.SelectMapper;
import com.kaiming.xiaohongshu.search.biz.index.UserIndex;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserReqVO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserRespVO;
import com.kaiming.xiaohongshu.search.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ClassName: SearchServiceImpl
 * Package: com.kaiming.xiaohongshu.search.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 12:32
 * @Version 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private SelectMapper selectMapper;

    /**
     * 搜索用户
     *
     * @param searchUserReqVO
     * @return
     */
    @Override
    public PageResponse<SearchUserRespVO> searchUser(SearchUserReqVO searchUserReqVO) {
        // 关键词
        String keyword = searchUserReqVO.getKeyword();
        // 页码
        Integer pageNo = searchUserReqVO.getPageNo();
        // 构建 SearchRequest, 指定索引
        SearchRequest searchRequest = new SearchRequest(UserIndex.NAME);
        // 构建查询内容
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建 multi_match 查询，查询 nickname 和 xiaohongshu_id 字段
        sourceBuilder.query(QueryBuilders.multiMatchQuery(
                keyword, UserIndex.FIELD_USER_NICKNAME, UserIndex.FIELD_USER_XIAOHONGSHU_ID
        ));
        // 排序，按 fans_total 降序
        SortBuilder<?> sortBuilder = new FieldSortBuilder(UserIndex.FIELD_USER_FANS_TOTAL)
                .order(SortOrder.DESC);

        // 设置分页
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;     // 偏移量

        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(UserIndex.FIELD_USER_NICKNAME)
                .preTags("<strong>")
                .postTags("</strong>");
        sourceBuilder.highlighter(highlightBuilder);

        // 将构建的查询条件设置到 SearchRequest 中
        searchRequest.source(sourceBuilder);

        // 返回 VO 集合
        List<SearchUserRespVO> searchUserRespVOS = null;
        // 总文档数，默认为 0
        long total = 0;

        try {
            log.info("==> SearchRequest: {}", searchRequest);
            // 执行查询请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 处理搜索结果
            total = searchResponse.getHits().getTotalHits().value;
            log.info("==> 命中文档总数, hits: {}", total);
            searchUserRespVOS = Lists.newArrayList();

            // 获取搜索命中的文档列表
            SearchHits hits = searchResponse.getHits();

            for (SearchHit hit : hits) {
                log.info("==> 文档数据: {}", hit.getSourceAsString());
                // 获取文档的所有字段（以 Map 的形式返回）
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                // 提取特定字段值
                Long userId = ((Number) sourceAsMap.get(UserIndex.FIELD_USER_ID)).longValue();
                String nickname = (String) sourceAsMap.get(UserIndex.FIELD_USER_NICKNAME);
                String avatar = (String) sourceAsMap.get(UserIndex.FIELD_USER_AVATAR);
                String xiaohongshuId = (String) sourceAsMap.get(UserIndex.FIELD_USER_XIAOHONGSHU_ID);
                Integer noteTotal = (Integer) sourceAsMap.get(UserIndex.FIELD_USER_NOTE_TOTAL);
                Integer fansTotal = (Integer) sourceAsMap.get(UserIndex.FIELD_USER_FANS_TOTAL);

                String highlightNickname = null;
                if (CollUtil.isNotEmpty(hit.getHighlightFields())
                        && hit.getHighlightFields().containsKey(UserIndex.FIELD_USER_NICKNAME)) {
                    highlightNickname = hit.getHighlightFields().get(UserIndex.FIELD_USER_NICKNAME).fragments()[0].string();
                }

                // 构建实体类 VO
                SearchUserRespVO searchUserRespVO = SearchUserRespVO.builder()
                        .userId(userId)
                        .nickname(nickname)
                        .avatar(avatar)
                        .xiaohongshuId(xiaohongshuId)
                        .noteTotal(noteTotal)
                        .fansTotal(NumberUtils.formatNumberString(fansTotal))
                        .highlightNickname(highlightNickname)
                        .build();

                searchUserRespVOS.add(searchUserRespVO);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return PageResponse.success(searchUserRespVOS, pageNo, total);
    }

    /**
     * 重建用户文档
     *
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    @Override
    public Response<Long> RebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO) {
        // 用户Id
        Long userId = rebuildUserDocumentReqDTO.getId();

        List<Map<String, Object>> result = selectMapper.selectEsUserIndexData(userId);
        for (Map<String, Object> recordMap : result) {
            // 索引请求对象
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id(String.valueOf(recordMap.get(UserIndex.FIELD_USER_ID)));
            // 设置文档的内容，使用查询结果的记录数据
            indexRequest.source(recordMap);
            // 将数据写入 Elasticsearch 索引
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("==> 重建用户文档异常: ", e);
            }

        }
        return Response.success();
    }
}
