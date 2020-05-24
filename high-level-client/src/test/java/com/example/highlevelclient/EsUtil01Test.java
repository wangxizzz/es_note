package com.example.highlevelclient;

import com.alibaba.fastjson.JSONObject;
import com.example.highlevelclient.entity.Book;
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

@RunWith(SpringRunner.class)
@SpringBootTest
// 注意：Test类需要添加public，否则无法进行测试
public class EsUtil01Test {

    public static final String INDEX_NAME = "book-index";

    @Resource
    private EsUtil01 esUtil01;

    @Test
    public void createIndexTest() {
        esUtil01.initIndex();
    }

    @Test
    public void insertOrUpdateOneTest() {
        EsEntity<Book> esEntity = new EsEntity<>();
        Book book = new Book(1, 123, "Java并发实战");
        // 唯一性id
        esEntity.setId(String.valueOf(book.getId() + book.getUserId()));
        esEntity.setData(book);
        esUtil01.insertOrUpdateOne(INDEX_NAME, esEntity);
    }

    /**
     * 重复插入 相同的 doc id，数据值会被覆盖
     */
    @Test
    public void insertBatchTest() {
        long start = System.currentTimeMillis();
        List<EsEntity> esEntities = new ArrayList<>();
        for (int i = 0; i < 10000; i = i + 1) {
            EsEntity<Book> esEntity = new EsEntity<>();
            Book book = new Book(i, 123 + i, "异步编程实战");
            // 唯一性id
            esEntity.setId(String.valueOf(book.getId() + book.getUserId()));
            esEntity.setData(book);
            esEntities.add(esEntity);
        }
        esUtil01.insertBatch(INDEX_NAME, esEntities);

        System.out.println("插入数据耗时" + (System.currentTimeMillis() - start));
    }

    @Test
    public void searchTest() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery());
        List<Book> search = esUtil01.search(INDEX_NAME, searchSourceBuilder, Book.class);

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