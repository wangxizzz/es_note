package com.example.es_demo.project01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * @Author wangxi
 * @Time 2020/5/16 20:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "peopleindex",type = "docs", shards = 1, replicas = 0)
public class People {
    /**
     * @Description: @Id注解必须是springframework包下的
     * org.springframework.data.annotation.Id
     * @Id 作用在成员变量，标记一个字段作为id主键
     */
    @Id
    private Long id;

    @Field(type = FieldType.Keyword, analyzer = "ik_max_word")
    private List<String> likes;

    @Field(type = FieldType.Keyword)

    private List<Integer> ages;

//    @Field(type = FieldType.Keyword)
//    private String name;
}
