package com.example.highlevelclient.service;

import com.example.highlevelclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author wangxi created on 2020/8/2 3:18 PM
 * @version v1.0
 */
@Component
@Slf4j
public class UserService extends AbstractEsService {

    public static final String USER_INDEX_NAME = "user_portrait";

    @Override
    public String getIndexName() {
        return USER_INDEX_NAME;
    }

    public void createIndex() {
        String indexJson = "{\"properties\":{\"userId\":{\"type\":\"long\"},\"username\":{\"type\":\"keyword\"},"
                + "\"city\":{\"type\":\"keyword\"},\"userSex\":{\"type\":\"keyword\"},"
                + "\"userAge\":{\"type\":\"integer\"},\"birthday\":{\"type\":\"date\"},"
                + "\"crowdIds\":{\"type\":\"long\"}}}";
        initIndex(USER_INDEX_NAME, indexJson);
    }


    public boolean batchInsert(int retryCount, Set<User> userSet) throws IOException {
        if (CollectionUtils.isEmpty(userSet) || retryCount < 0) {
            return false;
        }

        int maxAllowed = 2000;
        if (userSet.size() > maxAllowed) {
            log.warn("beyond maxAllowed size.maxAllowed:{}", maxAllowed);
            throw new RuntimeException("beyond maxAllowed size.maxAllowed:" + maxAllowed);
        }
        BulkRequest request = new BulkRequest();
        request.waitForActiveShards(ActiveShardCount.ALL);
        for (User user : userSet) {
            UpdateRequest updateRequest = new UpdateRequest(getIndexName(), user.getUserId().toString());
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("userId", user.getUserId());
            docMap.put("username", user.getUsername());
            docMap.put("city", user.getCity());
            docMap.put("userSex", user.getUserSex());
            docMap.put("userAge", user.getUserAge());
            docMap.put("birthday", DateFormatUtils.ISO_DATE_FORMAT.format(user.getBirthday()));
            docMap.put("crowdIds", user.getCrowdIds());
            updateRequest.doc(docMap);
            updateRequest.docAsUpsert(true);
            request.add(updateRequest);
        }
        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        if (!responses.hasFailures()) {
            return true;
        }

        Set<User> failedRelations = new HashSet<>();
        for (BulkItemResponse itemResponse : responses.getItems()) {
            if (itemResponse.isFailed()) {
                userSet.stream()
                        .filter(v -> Objects.equals(v.getUserId().toString(), itemResponse.getId()))
                        .findFirst()
                        .ifPresent(failedRelations::add);
            }
        }
        if (failedRelations.size() > 0) {
            retryCount = retryCount - 1;
            return batchInsert(retryCount, failedRelations);
        }
        return true;
    }


    public void groupByCount() throws IOException {
        Map<String, Long> groupMap = new HashMap<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.matchAllQuery());
        //.query(QueryBuilders.termQuery("category.keyword", "手机"));
        searchSourceBuilder.size(0);

        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("AGG").field("username");
        ((TermsAggregationBuilder) aggregationBuilder).size(100);
        searchSourceBuilder.aggregation(aggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(getIndexName());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("=============================");
        System.out.println(searchSourceBuilder.toString());
        Terms terms = searchResponse.getAggregations().get("AGG");
        for (Terms.Bucket entry : terms.getBuckets()) {
            groupMap.put(entry.getKey().toString(), entry.getDocCount());
        }
        System.out.println(groupMap);
    }
}
