package com.example.highlevelclient;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangxi
 * @Time 2020/5/17 19:25
 *
 * 参考网址：https://segmentfault.com/a/1190000017123560
 *
 * crud、创建文档 API相关：https://www.cnblogs.com/ginb/p/8716485.html  (不包含查询)
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HighClientTest01 {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    public void createIndexTest() throws IOException {
//        IndexRequest request = new IndexRequest(
//                "posts",
//                "doc",
//                "1");
//        String jsonString = "{" +
//                "\"user\":\"kimchy\"," +
//                "\"postDate\":\"2020-01-30\"," +
//                "\"message\":\"trying out Elasticsearch\"" +
//                "}";
//        request.source(jsonString, XContentType.JSON);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        IndexRequest indexRequest = new IndexRequest("posts", "doc", "1")
                .source(jsonMap);

        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

    }

    @Test
    public void createIndexTest02() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("twitter_two");//创建索引
        //创建的每个索引都可以有与之关联的特定设置。
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        //创建索引时创建文档类型映射
        request.mapping("tweet",//类型定义
                "  {\n" +
                        "    \"tweet\": {\n" +
                        "      \"properties\": {\n" +
                        "        \"message\": {\n" +
                        "          \"type\": \"text\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }",//类型映射，需要的是一个JSON字符串
                XContentType.JSON);

        //为索引设置一个别名
        request.alias(
                new Alias("twitter_alias")
        );

        //同步执行
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        // 异步调用参照 上面的链接
    }

    /**
     * 获取数据
     * @throws IOException
     */
    @Test
    public void getTest() throws IOException {
        GetRequest getRequest = new GetRequest(
                "posts",
                "doc",
                "1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

    }

    /**
     * 异步获取数据
     * @throws InterruptedException
     */
    @Test
    public void getAsyncTest() throws InterruptedException {
        GetRequest getRequest = new GetRequest(
                "posts",
                "doc",
                "1");
        ActionListener<GetResponse> listener = new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                String index = getResponse.getIndex();
                String type = getResponse.getType();
                String id = getResponse.getId();
                if (getResponse.isExists()) {
                    long version = getResponse.getVersion();
                    String sourceAsString = getResponse.getSourceAsString();
                    Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                    System.out.println(sourceAsMap);
                    System.out.println("dsadasdsa");
                } else {
                    System.out.println("========");
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        // 异步执行
        restHighLevelClient.getAsync(getRequest, RequestOptions.DEFAULT, listener);

        Thread.sleep(100000);
    }

    /**
     * 删除数据
     * @throws IOException
     */
    public void deleteTest() throws IOException {
        DeleteRequest request = new DeleteRequest(
                "posts",
                "doc",
                "1");
        DeleteResponse deleteResponse = restHighLevelClient.delete(
                request, RequestOptions.DEFAULT);

    }
}
