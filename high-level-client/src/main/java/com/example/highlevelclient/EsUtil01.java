package com.example.highlevelclient;

import com.alibaba.fastjson.JSON;
import com.example.highlevelclient.entity.EsEntity;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 参考网址：https://www.jianshu.com/p/0bad88e0f9b0
 */
@Component
public class EsUtil01 {
    @Resource
    private RestHighLevelClient client;

    /**
     * Description: 插入/更新一条记录
     *
     * @param index  index
     * @param entity 对象
     */
    public void insertOrUpdateOne(String index, EsEntity entity) {
        IndexRequest request = new IndexRequest(index);
        request.id(entity.getId());
        request.source(JSON.toJSONString(entity.getData()), XContentType.JSON);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description: 批量插入数据
     *
     * @param index index
     * @param list  带插入列表
     */
    public void insertBatch(String index, List<EsEntity> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(index)
				// 不使用自定义id
//				.id(item.getId())
                .source(JSON.toJSONString(item.getData()), XContentType.JSON)
                .timeout(new TimeValue(60, TimeUnit.SECONDS))));
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description: 批量删除
     *
     * @param index  index
     * @param idList 待删除列表
     */
    public <T> void deleteBatch(String index, Collection<T> idList) {
        BulkRequest request = new BulkRequest();
        idList.forEach(id -> request.add(new DeleteRequest(index, id.toString())));
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description: 搜索
     *
     * @param index   index
     * @param builder 查询参数
     * @param c       结果类对象
     * @return java.util.ArrayList
     */
    public <T> List<T> search(String index, SearchSourceBuilder builder, Class<T> c) {
        SearchRequest request = new SearchRequest(index);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Description: 删除index
     *
     * @param index index
     * @return void
     */
    public void deleteIndex(String index) {
        try {
            client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description: delete by query
     *
     * @param index   index
     * @param builder builder
     */
    public void deleteByQuery(String index, QueryBuilder builder) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(builder);
        //设置批量操作数量,最大为10000
        request.setBatchSize(10000);
        request.setConflicts("proceed");
        try {
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final String INDEX_NAME = "people-index";

    public static final String CREATE_INDEX = "{\"properties\":{\"crowdId\":{\"type\":\"integer\"},"
			+ "\"userId\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\","
			+ "\"search_analyzer\":\"ik_smart\"},\"city\":{\"type\":\"keyword\"},\"userSex\":{\"type\":\"keyword\"}}}";


	public static void main(String[] args) {
		System.out.println(CREATE_INDEX);
	}

    /**
     * 初始化索引
     */
    public void initIndex() {
        try {
            if (this.indexExist(INDEX_NAME)) {
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
            request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
            request.mapping(CREATE_INDEX, XContentType.JSON);
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            if (!res.isAcknowledged()) {
                throw new RuntimeException("初始化失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Description: 判断某个index是否存在
     *
     * @param index index名
     * @return boolean
     */
    public boolean indexExist(String index) throws Exception {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }
}
