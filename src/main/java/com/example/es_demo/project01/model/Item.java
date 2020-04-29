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
     * 注意：index设置为false，表示es没有对其建立索引，如果利用搜索api搜索该列，是报错的。
     */
    @Field(index = false, type = FieldType.Keyword)
    private String images;

    /**
     * 新增一个索引字段，只需要增加一个bean属性即可。es会自动建立mapping关系
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String desc;

    public Item(Long id, String title, String category, String brand, Double price, String images) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.images = images;
    }
}
