package com.example.highlevelclient;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxi created on 2020/6/2 3:29 PM
 * @version v1.0
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchApiTest01 {
	@Resource
	private RestHighLevelClient client;

	private String index = "item";

	/**
	 * 根据某一字段分组
	 * https://blog.csdn.net/winterking3/article/details/103178732
	 *
	 * @throws IOException
	 */


	/**
	 * 统计符合某一查询的总数
	 *
	 * @throws IOException
	 */
	@Test
	public void countTest() throws IOException {
		CountRequest countRequest = new CountRequest(index);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		countRequest
				//.query(QueryBuilders.matchAllQuery());
				.query(QueryBuilders.termQuery("category.keyword", "手机"));

		CountResponse response = client.count(countRequest, RequestOptions.DEFAULT);
		System.out.println(response.getCount());
	}

	/**
	 * 范围查询
	 * @throws IOException
	 */
	@Test
	public void rangeQueryTest() throws IOException {
		// 1.创建并设置SearchSourceBuilder对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 查询条件--->生成DSL查询语句
		RangeQueryBuilder priceBuilder = QueryBuilders
				.rangeQuery("price")
				.gt(4000);
		searchSourceBuilder.query(priceBuilder);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		displayResponse(searchResponse);
	}

	/**
	 * 判断某个查询是否存在
	 */
	@Test
	public void existTest() throws IOException {
		index = "peopleindex";
		// 1.创建并设置SearchSourceBuilder对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 查询条件--->生成DSL查询语句
//		QueryBuilder likeBuilder = QueryBuilders.termQuery("likes.keyword", "二珂");
		QueryBuilder ageBuilder = QueryBuilders.termQuery("ages", 222);
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder()
//				.must(likeBuilder)
				.must(ageBuilder);

		searchSourceBuilder.query(boolQueryBuilder);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		for (SearchHit hit : searchResponse.getHits().getHits()) {

			Object likes = hit.getSourceAsMap().get("likes");
			if (likes instanceof List) {
				System.out.println("uuuuuuuu");
			}
			List<String> temp = (List) likes;
			System.out.println(temp.get(0));

			System.out.println(likes.toString());
//			System.out.println(JSON.parse(likes.toString()));

			System.out.println(JSON.parseArray("[\"二珂\",\"提莫\"]"));
		}
		//displayResponse(searchResponse);
	}

	private void displayResponse(SearchResponse response) {
		SearchHit[] results = response.getHits().getHits();
		if (results.length == 0) {
			System.out.println("查询为空。。。。");
		}
		for(SearchHit hit : results){

			String sourceAsString = hit.getSourceAsString();
			if (sourceAsString != null) {
				// 可以把sourceAsString 格式化为对象
				System.out.println(sourceAsString);
			}
		}
	}
}
