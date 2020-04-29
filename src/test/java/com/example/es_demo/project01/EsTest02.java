package com.example.es_demo.project01;

import com.example.es_demo.project01.mapper.ExtResultMapper;
import com.example.es_demo.project01.model.Item;
import com.example.es_demo.project01.repository.ItemRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Iterator;

/**
 * @Author wangxi
 * @Time 2020/4/28 10:58
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest02 {

    private static final Logger logger = LoggerFactory.getLogger(EsTest02.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;
    @Resource
    private ExtResultMapper extResultMapper;

    /**
     * 测试高级查询，使用基本api无法做到的
     */

    @Test
    public void test01() {
        // 词条查询
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "小米");
        // 执行查询
        Iterable<Item> items = this.itemRepository.search(queryBuilder);
        items.forEach(System.out::println);
    }


    @Test
    public void testQuery(){
        String keyword = "程序设计";

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 给name字段更高的权重
        queryBuilder.should(QueryBuilders.matchQuery("title", keyword).boost(3));
        // description 默认权重 1
        queryBuilder.should(QueryBuilders.matchQuery("category", keyword));
        // 至少一个should条件满足
        queryBuilder.minimumShouldMatch(1);

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10))
                .build();
        logger.info("\n search(): searchContent [" + keyword + "] \n DSL  = \n " + searchQuery.getQuery().toString());

        Page<Item> page = itemRepository.search(searchQuery);

        // 总条数
        logger.info(page.getTotalElements() + "");

        Iterator<Item> iterator = page.iterator();
        while (iterator.hasNext()){
            logger.info(iterator.next().toString());
        }

        // 总页数
        logger.info(page.getTotalPages() + "");
    }

    /**
     * 测试查询高亮
     * 参考文章：https://www.cnblogs.com/vcmq/p/9966693.html
     */
    @Test
    public void testHighlightQuery(){
        String keyword = "程序设计";

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 至少一个should条件满足
        boolQuery.minimumShouldMatch(1);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<span style=\"color:red\">").postTags("</span>"),
                        new HighlightBuilder.Field("brand").preTags("<span style=\"color:red\">").postTags("</span>"))
                .withPageable(PageRequest.of(0, 10));
        // 最佳字段  + 降低除了name之外字段的权重系数
        MatchQueryBuilder nameQuery = QueryBuilders.matchQuery("title", keyword).analyzer("ik_max_word");
        MatchQueryBuilder authorQuery = QueryBuilders.matchQuery("brand", keyword).boost(0.8f);
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery().add(nameQuery).add(authorQuery);
        queryBuilder.withQuery(disMaxQueryBuilder);

        NativeSearchQuery searchQuery = queryBuilder.build();
        Page<Item> items = elasticsearchTemplate.queryForPage(searchQuery, Item.class, extResultMapper);

        items.forEach(e -> logger.info("{}", e));
    }
}
