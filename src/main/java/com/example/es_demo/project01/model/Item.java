package com.example.es_demo.project01.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * indexName：对应索引库名称
 * type：对应在索引库中的类型
 * shards：分片数量，默认5
 * replicas：副本数量，默认1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "item",type = "docs", shards = 1, replicas = 0)
public class Item {
    /**
     * @Description: @Id注解必须是springframework包下的
     * org.springframework.data.annotation.Id
     * @Id 作用在成员变量，标记一个字段作为id主键
     */
    @Id
    private Long id;

    /**
     *  @Field 作用在成员变量，标记为文档的字段，并指定字段映射属性：
     *     type：字段类型，取值是枚举：FieldType
     *     index：是否索引，布尔类型，默认是true
     *     store：是否存储，布尔类型，默认是false
     *     analyzer：分词器名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    /**
     * 分类
     */
    @Field(type = FieldType.Keyword)
    private String category;

    /**
     * 品牌
     */
    @Field(type = FieldType.Keyword)
    private String brand;

    /**
     * 价格
     */
    @Field(type = FieldType.Double)
    private Double price;

    /**
     * 图片地址
     */
    @Field(index = false, type = FieldType.Keyword)
    private String images;
}
