package com.example.highlevelclient;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangxi
 * @Time 2020/5/17 21:03
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApiTest {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void test01() throws IOException {
        //构造search request .在这里无参，查询全部索引
        //SearchRequest searchRequest = new SearchRequest();
        SearchRequest searchRequest = new SearchRequest("item"); //指定posts索引
        //searchRequest.types("docs"); //指定doc类型

        //大多数查询参数要写在searchSourceBuilder里
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
				// ConstantScoreQueryBuilder 性能更好，适用于非搜索排名的场景
//                .query(QueryBuilders.constantScoreQuery(QueryBuilders.matchAllQuery()))  //增加match_all的条件
//                .query(QueryBuilders.termQuery("category", "手机"))
                .query(QueryBuilders.termQuery("category.keyword", "手机"))  //利用keyword搜索
                .from(0)
                .size(10)
                .timeout(new TimeValue(60, TimeUnit.SECONDS));   // 设置超时时间

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] results = response.getHits().getHits();
        if (results.length == 0) {
			System.out.println("查询元素为空。。。。。");
		}
        for(SearchHit hit : results){

            String sourceAsString = hit.getSourceAsString();
            if (sourceAsString != null) {
                // 可以把sourceAsString 格式化为对象
                System.out.println(sourceAsString);
            }
        }
    }

    /**
     * 分页查询步骤。 以及or should 查询
     */
    @Test
    public void test02() throws IOException {
        // 2.创建BoolQueryBuilder对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 3.设置boolQueryBuilder条件
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders
                .matchPhraseQuery("key_word", "广东");
        MatchPhraseQueryBuilder matchPhraseQueryBuilder2 = QueryBuilders
                .matchPhraseQuery("key_word", "湖人");
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders
                .rangeQuery("postdate")
                .from("2016-01-01 00:00:00");
        // 子boolQueryBuilder条件条件，用来表示查询条件or的关系
        BoolQueryBuilder childBoolQueryBuilder = new BoolQueryBuilder()
                .should(matchPhraseQueryBuilder)
                .should(matchPhraseQueryBuilder2);
        // 4.添加查询条件到boolQueryBuilder中
        boolQueryBuilder
                .must(childBoolQueryBuilder)
                .must(rangeQueryBuilder);

        // 1.创建并设置SearchSourceBuilder对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询条件--->生成DSL查询语句
        searchSourceBuilder.query(boolQueryBuilder);
        // 第几页
        searchSourceBuilder.from(0);
        // 每页多少条数据
        searchSourceBuilder.size(100);
        // 获取的字段（列）和不需要获取的列
        searchSourceBuilder.fetchSource(new String[]{"postdate", "key_word"}, new String[]{});
        // 设置排序规则
        searchSourceBuilder.sort("postdate", SortOrder.ASC);
        // 设置超时时间为2s
        searchSourceBuilder.timeout(new TimeValue(2000));

        // 2.创建并设置SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        // 设置request要搜索的索引和类型
        searchRequest.indices("spnews").types("news");
        // 设置SearchSourceBuilder查询属性
        searchRequest.source(searchSourceBuilder);

        // 3.查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.toString());
    }

    /**
     * 常见API操作介绍
     */
    @Test
    public void test03() {
        // 首先创建搜索请求对象：
         SearchRequest searchRequest = new SearchRequest();
        // 查询多个文档库. 下面两种写法均可
//        SearchRequest searchRequest = new SearchRequest("posts2","posts", "posts2", "posts1");
        searchRequest.indices("posts2","posts", "posts2", "posts1");
        // 多种类型，同样是文档类型之间用逗号隔开
        searchRequest.types("doc1", "doc1", "doc2");
        // 设置指定查询的路由分片
        searchRequest.routing("routing");
        // 用preference方法去指定优先去某个分片上去查询（默认的是随机先去某个分片）
        searchRequest.preference("_local");


        // 创建搜索内容参数设置对象:SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 将SearchSourceBuilder对象添加到搜索请求中:
        searchRequest.source(searchSourceBuilder);
        // 为搜索的文档内容对象SearchSourceBuilder设置参数：

        // 查询所有内容
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 查询包含关键词字段的文档：如下，表示查询出来所有包含user字段且user字段包含kimchy值的文档
        searchSourceBuilder.query(QueryBuilders.termQuery("user", "kimchy"));

        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy")
                .fuzziness(Fuzziness.AUTO)  // 打开模糊查询
                .prefixLength(3)        // 在匹配查询上设置前缀长度选项
                .maxExpansions(10);        // 设置最大扩展选项以控制查询的模糊过程

        // 把match的相关配置加入到SearchSourceBuilder
        searchSourceBuilder.query(matchQueryBuilder);

        // 设置分页、超时时间
    }


    /**
     * 待定：
     * （1）批量写入能力
     * （2）聚合的写法
     */
}
