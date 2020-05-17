package com.example.es_demo.project01.model;

import com.example.es_demo.project01.repository.PeopleRepository;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author wangxi
 * @Time 2020/5/16 20:20
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ESPeopleTest {

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private RestHighLevelClient client;

    /**
     * 批量新增
     */
    @Test
    public void indexBatch() {
        List<People> list = new ArrayList<>();
        list.add(new People(1L, Lists.newArrayList("看书", "直播"), Lists.newArrayList(1, 2)));
        list.add(new People(2L, Lists.newArrayList("二珂", "提莫"), Lists.newArrayList(100, 222)));
        // 接收对象集合，实现批量新增
        peopleRepository.saveAll(list);
    }

    @Test
    public void test01() {
        // 词条查询. title字段是text类型，因此通过分词可以匹配
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("likes", "二珂");
        // 执行查询
        Iterable<People> items = this.peopleRepository.search(queryBuilder);
        items.forEach(System.out::println);
    }

    @Test
    public void test02() {
//        SearchResponse response = client.prepareSearch("index1", "index2")
//                .setTypes("type1", "type2")
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(QueryBuilders.termQuery("multi", "test"))                 // Query
//                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
//                .setFrom(0).setSize(60).setExplain(true)
//                .get();
    }

}