package com.kaiming.xiaohongshu.note.biz.domain.mapper;

import com.kaiming.xiaohongshu.note.biz.domain.dataobject.TopicDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TopicDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicDO record);

    int insertSelective(TopicDO record);
    
    int batchInsert(@Param("newTopics") List<TopicDO> newTopics);

    TopicDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDO record);

    int updateByPrimaryKey(TopicDO record);
    
    String selectNameByPrimaryKey(Long id);

    /**
     * 根据关键词模糊查询话题对象
     * @param keyword
     * @return
     */
    List<TopicDO> selectByLikeName(String keyword);

    /**
     * 根据话题Ids集合批量查询话题
     * @param topicIds
     * @return
     */
    List<TopicDO> selectByTopicIdIn(List<Long> topicIds);

    /**
     * 根据话题名称获取话题对象
     * @param topicName
     * @return
     */
    TopicDO selectByTopicName(String topicName);
}