package com.example.es_demo.project01;

import com.example.es_demo.project01.model.Item;
import com.example.es_demo.project01.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * 根据官方文档测试常用的api
 * 文档地址:https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticsearchApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchApplicationTests.class);
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;

    /**
     * 创建索引，会根据Item类的@Document注解信息来创建
     */
    @Test
    public void testCreateIndex() {
        // 创建索引，会根据Item类的@Document注解信息来创建
        elasticsearchTemplate.createIndex(Item.class);
        // 配置映射，会根据Item类中的id、Field等字段来自动完成映射
        elasticsearchTemplate.putMapping(Item.class);
    }

    /**
     * 索引数据
     */
    @Test
    public void indexItem(){
        Item item = new Item();
        item.setId(8L);
        item.setTitle("MacBook Pro");
        item.setCategory("笔记本电脑");
        item.setBrand("苹果");
        item.setPrice(12999.0);
        item.setImages("https://www.apple.com/mac.png");
        item.setDesc("笔记本超级好");

        Item item1 = new Item();
        item1.setId(9L);
        item1.setTitle("重构 改善既有代码的设计程序");
        item1.setCategory("程序设计");
        item1.setBrand("马丁·福勒(Martin Fowler)");
        item1.setPrice(118.00);
        item1.setImages("http://product.dangdang.com/26913154.html");
        item1.setDesc("重构的书很好");

        Item item2 = new Item();
        item2.setId(10L);
        item2.setTitle("Python编程 从入门到实践");
        item2.setCategory("Python");
        item2.setBrand("埃里克·马瑟斯（Eric Matthes）");
        item2.setPrice(61.40);
        item2.setImages("http://bang.dangdang.com/books/bestsellers/01.54.00.00.00.00-recent7-0-0-1-1");

        Item item3 = new Item();
        item3.setId(11L);
        item3.setTitle("统计之美：人工智能时代的科学思维");
        item3.setCategory("数学");
        item3.setBrand("李舰");
        item3.setPrice(56.70);
        item3.setImages("http://product.dangdang.com/26915070.html");

        Item item4 = new Item();
        item4.setId(12L);
        item4.setTitle("机器学习");
        item4.setCategory("人工智能");
        item4.setBrand("周志华");
        item4.setPrice(61.60);
        item4.setImages("http://product.dangdang.com/23898620.html");

        itemRepository.index(item);
        itemRepository.index(item1);
        itemRepository.index(item2);
        itemRepository.index(item3);
        itemRepository.index(item4);
    }

    /**
     * 批量新增
     */
    @Test
    public void indexBatch() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.zq.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.zq.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.zq.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.zq.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.zq.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
        list.add(new Item(6L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.zq.com/123.jpg"));
        list.add(new Item(7L, "华为META10", " 手机", "华为", 4499.00, "http://image.zq.com/3.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    /**
     * 更新es记录，把id相同的重新添加，就覆盖了
     */
    @Test
    public void update() {
        Item item = new Item(6L, "坚果手机R1aaaaa", " 手机", "锤子", 3699.00, "http://image.zq.com/123.jpg");
        itemRepository.save(item);
    }

    /**
     * 搜索
     */
    @Test
    public void testSearch(){
        List<Item> itemList = itemRepository.findByTitleLike("Mac");
        for (Item item : itemList) {
            System.out.println(item.toString());
        }
    }

    /**
     * 返回实体数量
     */
    @Test
    public void testCount(){
        long count = itemRepository.count();
        System.out.println(count);
    }

    /**
     * 查找全部
     */
    @Test
    public void testFindAll(){
        // 查询全部，并安装价格降序排序
        Iterable<Item> items = this.itemRepository.findAll(Sort.by(Sort.Direction.DESC, "price"));
        items.forEach(item-> System.out.println(item));
    }

    /**
     * 返回由给定ID标识的实体
     */
    @Test
    public void testFindById(){
        Optional<Item> item = itemRepository.findById(1L);
        logger.info(item.get().toString());
    }

    /**
     * 指示是否存在具有给定ID的实体
     */
    @Test
    public void testExistsById(){
        logger.info(itemRepository.existsById(2L) + "");
    }


    /**
     * 测试分页
     */
    @Test
    public void testPage(){
        // 注意：页数从 0 开始，0 代表第一页
        Page<Item> page = itemRepository.findAll(PageRequest.of(0, 3));
        // 总条数
        logger.info(page.getTotalElements() + "");

        Iterator<Item> iterator = page.iterator();
        while (iterator.hasNext()){
            logger.info(iterator.next().toString());
        }

        // 总页数
        logger.info(page.getTotalPages() + "");
    }

    /**
     * 区间检索
     */
    @Test
    public void testBetween(){
        List<Item> items = itemRepository.findByPriceBetween(50.0,70.0);
        for (Item item : items) {
            logger.info(item.toString());
        }
    }

}
