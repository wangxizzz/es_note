package com.example.es_demo.project01;

import com.example.es_demo.project01.mapper.ExtResultMapper;
import com.example.es_demo.project01.model.Item;
import com.example.es_demo.project01.repository.ItemRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * @Author wangxi
 * @Time 2020/4/28 10:58
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest02 {

    private static final Logger logger = LoggerFactory.getLogger(EsTest02.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ItemRepository itemRepository;
    @Resource
    private ExtResultMapper extResultMapper;

    /**
     * 测试高级查询，使用基本api无法做到的
     * <p>
     * QueryBuilders提供了大量的静态方法，
     * 用于生成各种不同类型的查询对象，例如：词条、模糊、通配符等QueryBuilder对象。
     */

    @Test
    public void test01() {
        // 词条查询. title字段是text类型，因此通过分词可以匹配
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "小米");
        // 执行查询
        Iterable<Item> items = this.itemRepository.search(queryBuilder);
        items.forEach(System.out::println);
    }

    /**
     * 自定义基本查询
     * <p>
     * NativeSearchQueryBuilder：Spring提供的一个查询条件构建器，帮助构建json格式的请求体
     */
    @Test
    public void testNativeQuery() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米"));
        // 执行搜索，获取结果(默认是分页查询)
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(items.getTotalElements());
        // 打印总页数
        System.out.println(items.getTotalPages());
        items.forEach(System.out::println);
    }


    /**
     * 多条件分页查询, 利用NativeSearchQueryBuilder构建查询条件
     */
    @Test
    public void testQuery() {
        String keyword = "程序";
        // 构建条件查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 给name字段更高的权重
        queryBuilder.should(QueryBuilders.matchQuery("title", keyword).boost(3));
        // description 默认权重 1 . termQuery是精确查找，当然也可以使用matchQuery，应该是根据字段的类型决定的
        queryBuilder.should(QueryBuilders.termQuery("category", keyword));
        // 至少一个should条件满足
        queryBuilder.minimumShouldMatch(1);

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .withPageable(PageRequest.of(0, 10))
                .build();
        logger.info("\n search(): searchContent [" + keyword + "] \n DSL  = \n " + searchQuery.getQuery().toString());

        Page<Item> page = itemRepository.search(searchQuery);

        // 总条数
        logger.info(page.getTotalElements() + "");

        Iterator<Item> iterator = page.iterator();
        while (iterator.hasNext()) {
            logger.info(iterator.next().toString());
        }
        // 当期页数
        logger.info("当前页数 = {}", page.getNumber() + "");
        // 总页数
        logger.info(page.getTotalPages() + "");
    }

    /**
     * 利用NativeSearchQueryBuilder 构造排序查询条件
     */
    @Test
    public void testSort(){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));

        // 执行搜索，获取结果
        Page<Item> items = this.itemRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(items.getTotalElements());
        items.forEach(System.out::println);
    }

    /**
     * 利用NativeSearchQueryBuilder 构造聚合
     */
    @Test
    public void testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand"));
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }

    }

    /**
     * 套聚合，求平均值
     */
    @Test
    public void testSubAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand")
                        .subAggregation(AggregationBuilders.avg("priceAvg").field("price")) // 在品牌聚合桶内进行嵌套聚合，求平均值
        );
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>) this.itemRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称  3.5、获取桶中的文档数量
            System.out.println(bucket.getKeyAsString() + "，共" + bucket.getDocCount() + "台");

            // 3.6.获取子聚合结果：
            InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
            System.out.println("平均售价：" + avg.getValue());
        }

    }


    /**
     * 测试查询高亮
     * 参考文章：https://www.cnblogs.com/vcmq/p/9966693.html
     */
    @Test
    public void testHighlightQuery() {
        String keyword = "程序设计";

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 至少一个should条件满足
        boolQuery.minimumShouldMatch(1);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<span style=\"color:red\">").postTags("</span>"),
                        new HighlightBuilder.Field("brand").preTags("<span style=\"color:red\">").postTags("</span>"))
                .withPageable(PageRequest.of(0, 10));
        // 最佳字段  + 降低除了name之外字段的权重系数
        MatchQueryBuilder nameQuery = QueryBuilders.matchQuery("title", keyword).analyzer("ik_max_word");
        MatchQueryBuilder authorQuery = QueryBuilders.matchQuery("brand", keyword).boost(0.8f);
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery().add(nameQuery).add(authorQuery);
        queryBuilder.withQuery(disMaxQueryBuilder);

        NativeSearchQuery searchQuery = queryBuilder.build();
        Page<Item> items = elasticsearchTemplate.queryForPage(searchQuery, Item.class, extResultMapper);

        items.forEach(e -> logger.info("{}", e));
    }
}
