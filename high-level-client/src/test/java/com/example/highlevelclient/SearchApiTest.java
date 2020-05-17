package com.example.highlevelclient;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
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
        searchRequest.types("docs"); //指定doc类型

        //大多数查询参数要写在searchSourceBuilder里
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//                .query(QueryBuilders.matchAllQuery())  //增加match_all的条件
                .query(QueryBuilders.termQuery("category", "手机"))
                .from(0)
                .size(10)
                .timeout(new TimeValue(60, TimeUnit.SECONDS));   // 设置超时时间

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] results = response.getHits().getHits();
        for(SearchHit hit : results){

            String sourceAsString = hit.getSourceAsString();
            if (sourceAsString != null) {
                // 可以把sourceAsString 格式化为对象
                System.out.println(sourceAsString);
            }
        }


    }
}
