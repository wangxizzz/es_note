package com.example.es_demo.project01.repository;

import com.example.es_demo.project01.model.People;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * @Author wangxi
 * @Time 2020/5/16 20:26
 */
@Component
public interface PeopleRepository extends ElasticsearchRepository<People, Long> {
}
