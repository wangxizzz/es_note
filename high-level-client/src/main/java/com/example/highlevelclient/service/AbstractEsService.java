package com.example.highlevelclient.service;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

import javax.annotation.Resource;

/**
 *
 * @author wangxi created on 2020/8/2 3:39 PM
 * @version v1.0
 */
public abstract class AbstractEsService implements ESService {
    @Resource
    public RestHighLevelClient client;

    public abstract String getIndexName();

    /**
     * 初始化索引
     */
    public void initIndex(String indexName, String indexJson) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.settings(Settings.builder().put("index.number_of_shards", 2).put("index.number_of_replicas", 2));
            request.mapping(indexJson, XContentType.JSON);
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
