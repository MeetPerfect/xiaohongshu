package com.kaiming.xiaohongshu.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.constant.DateConstants;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.util.NumberUtils;
import com.kaiming.xiaohongshu.search.index.NoteIndex;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteReqVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteRespVO;
import com.kaiming.xiaohongshu.search.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ClassName: NoteServiceImpl
 * Package: com.kaiming.xiaohongshu.search.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 14:21
 * @Version 1.0
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 搜索笔记
     *
     * @param searchNoteReqVO
     * @return
     */
    @Override
    public PageResponse<SearchNoteRespVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        // 关键词
        String keyword = searchNoteReqVO.getKeyword();
        // 页码
        Integer pageNo = searchNoteReqVO.getPageNo();
        // 构建 SearchRequest, 指定查询索引
        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);

        // 创建查询器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword)
                .field(NoteIndex.FIELD_NOTE_TITLE, 2.0f)    // 手动设置笔记标题的权重值为 2.0
                .field(NoteIndex.FIELD_NOTE_TOPIC);     // 不设置，权重默认为 1.0
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                // function 1
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL)
                                .factor(0.5f)
                                .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                .missing(0)
                ),
                // function 2
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL)
                                .factor(0.3f)
                                .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                .missing(0)
                ),
                // function 3
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL)
                                .factor(0.2f)
                                .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                .missing(0)
                )
        };
        // 构建 function_score 查询
        // "score_mode": "sum",
        // "boost_mode": "sum"

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(queryBuilder,
                        filterFunctionBuilders)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM)        // score_mode 为 sum
                .boostMode(CombineFunction.SUM);            // boost_mode 为 sum

        // 设置查询
        sourceBuilder.query(functionScoreQueryBuilder);

        // 设置排序
        sourceBuilder.sort(new FieldSortBuilder("_score").order(SortOrder.DESC));

        // 设置分页
        int pageSize = 10;
        int from = (pageNo - 1) * pageSize;     // 偏移量
        sourceBuilder.from(from);
        sourceBuilder.size(pageSize);

        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(NoteIndex.FIELD_NOTE_TITLE)
                .preTags("<strong>")
                .postTags("</strong>");
        sourceBuilder.highlighter(highlightBuilder);
        // 将构建的查询条件设置到 SearchRequest 中
        searchRequest.source(sourceBuilder);
        // 返参 VO 集合
        List<SearchNoteRespVO> searchNoteRspVOS = null;

        // 总文档数，默认为 0
        long total = 0;

        try {
            log.info("==> SearchRequest: {}", searchRequest);
            // 执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 处理搜索结果
            total = searchResponse.getHits().getTotalHits().value;
            log.info("==> 命中文档总数, hits: {}", total);

            searchNoteRspVOS = Lists.newArrayList();
            // 获取搜索命中的文档列表
            SearchHits hits = searchResponse.getHits();

            for (SearchHit hit : hits) {
                log.info("==> 文档数据: {}", hit.getSourceAsString());

                // 获取文档的所有字段（以 Map 的形式返回）
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                // 提取特定字段值
                Long noteId = (Long) sourceAsMap.get(NoteIndex.FIELD_NOTE_ID);
                String cover = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_COVER);
                String title = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_TITLE);
                String avatar = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_AVATAR);
                String nickname = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_NICKNAME);
                // 获取更新时间
                String updateTimeStr = (String) sourceAsMap.get(NoteIndex.FIELD_NOTE_UPDATE_TIME);
                LocalDateTime updateTime = LocalDateTime.parse(updateTimeStr, DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
                Integer likeTotal = (Integer) sourceAsMap.get(NoteIndex.FIELD_NOTE_LIKE_TOTAL);


                // 获取高亮字段
                String highlightedTitle = null;
                if (CollUtil.isNotEmpty(hit.getHighlightFields())
                        && hit.getHighlightFields().containsKey(NoteIndex.FIELD_NOTE_TITLE)) {
                    highlightedTitle = hit.getHighlightFields().get(NoteIndex.FIELD_NOTE_TITLE).fragments()[0].string();
                }
                
                // 构建实体类 VO
                SearchNoteRespVO searchNoteRespVO = SearchNoteRespVO.builder()
                        .noteId(noteId)
                        .cover(cover)
                        .title(title)
                        .avatar(avatar)
                        .nickname(nickname)
                        .updateTime(updateTime)
                        .likeTotal(NumberUtils.formatNumberString(likeTotal))
                        .build();
                
                searchNoteRspVOS.add(searchNoteRespVO);
            }
        } catch (Exception e) {
            log.error("==> 查询 Elasticserach 异常: ", e);
        }
        return PageResponse.success(searchNoteRspVOS, pageNo, total);
    }
}
