package com.example.highlevelclient.service;

import com.example.highlevelclient.entity.Crowd;
import com.example.highlevelclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
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
public class CrowdService extends AbstractEsService {
    public static final String CROWD_INDEX_NAME = "crowd_relation";

    @Override
    public String getIndexName() {
        return CROWD_INDEX_NAME;
    }

    public void createIndex() {
        String indexJson = "{\"properties\":{\"crowdId\":{\"type\":\"long\"},\"userId\":{\"type\":\"long\"},"
                + "\"insertTime\":{\"type\":\"date\"}}}";

        initIndex(CROWD_INDEX_NAME, indexJson);
    }

    public boolean batchInsert(int retryCount, Set<Crowd> crowdSet) throws IOException {
        if (CollectionUtils.isEmpty(crowdSet) || retryCount < 0) {
            return false;
        }

        int maxAllowed = 2000;
        if (crowdSet.size() > maxAllowed) {
            log.warn("beyond maxAllowed size.maxAllowed:{}", maxAllowed);
            throw new RuntimeException("beyond maxAllowed size.maxAllowed:" + maxAllowed);
        }
        BulkRequest request = new BulkRequest();
        request.waitForActiveShards(ActiveShardCount.ALL);
        for (Crowd crowd : crowdSet) {
            UpdateRequest updateRequest = new UpdateRequest(getIndexName(), crowd.getId());
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("crowdId", crowd.getCrowdId());
            docMap.put("userId", crowd.getUserId());
            docMap.put("insertTime", crowd.getInsertTime());
            updateRequest.doc(docMap);
            updateRequest.docAsUpsert(true);
            request.add(updateRequest);
        }
        BulkResponse responses = client.bulk(request, RequestOptions.DEFAULT);
        if (!responses.hasFailures()) {
            return true;
        }

        Set<Crowd> failedRelations = new HashSet<>();
        for (BulkItemResponse itemResponse : responses.getItems()) {
            if (itemResponse.isFailed()) {
                crowdSet.stream()
                        .filter(v -> Objects.equals(v.getId(), itemResponse.getId()))
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
}
