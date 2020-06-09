package com.example.highlevelclient;

import com.alibaba.fastjson.JSONObject;
import com.example.highlevelclient.entity.PeopleEntity;
import com.example.highlevelclient.entity.EsEntity;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
// 注意：Test类需要添加public，否则无法进行测试
public class EsUtil01Test {

    public static final String INDEX_NAME = "people-index";

    @Resource
    private EsUtil01 esUtil01;

    @Test
    public void createIndexTest() {
        esUtil01.initIndex();
    }

    @Test
    public void insertOrUpdateOneTest() {
//        EsEntity<PeopleEntity> esEntity = new EsEntity<>();
//        PeopleEntity peopleEntity = new PeopleEntity(1, 123, "Java并发实战");
//        // 唯一性id
//        esEntity.setId(String.valueOf(peopleEntity.getId() + peopleEntity.getUserId()));
//        esEntity.setData(peopleEntity);
//        esUtil01.insertOrUpdateOne(INDEX_NAME, esEntity);
    }

    /**
     * 重复插入 相同的 doc id，数据值会被覆盖
     */
    @Test
    public void insertBatchTest() {
        long start = System.currentTimeMillis();
        List<EsEntity> esEntities = new ArrayList<>();
		Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            EsEntity<PeopleEntity> esEntity = new EsEntity<>();
            PeopleEntity peopleEntity = new PeopleEntity();
            if ((i & 1) == 1) {
            	peopleEntity.setUserSex("user_sex01");
			} else {
            	peopleEntity.setUserSex("user_sex02");
			}
			peopleEntity.setUserId(random.nextInt(1000));
            peopleEntity.setCrowdId(random.nextInt(100));
            peopleEntity.setCity("city_0" + random.nextInt(4));
            peopleEntity.setName("username" + random.nextInt(50));
            // 唯一性id。使用es自己生成的
//            esEntity.setId(String.valueOf(peopleEntity.getId() + peopleEntity.getUserId()));
            esEntity.setData(peopleEntity);
            esEntities.add(esEntity);
        }
        esUtil01.insertBatch(INDEX_NAME, esEntities);

        System.out.println("插入数据耗时" + (System.currentTimeMillis() - start));
    }

    @Test
    public void searchTest() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery());
        List<PeopleEntity> search = esUtil01.search(INDEX_NAME, searchSourceBuilder, PeopleEntity.class);

        System.out.println(JSONObject.toJSON(search));
    }

    @Test
    public void deleteBatchTest() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 传入的唯一标识doc的id
            list.add(String.valueOf(i + 123 + i));
        }
        esUtil01.deleteBatch(INDEX_NAME, list);
    }
}