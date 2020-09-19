package com.example.highlevelclient.express;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author wangxi created on 2020/9/19 21:22
 * @version v1.0
 */
public class ExpressionTest {



    @Test
    public void test01() {
//        String expression = "(1-(2*3))";
        String expression = "27+(32*1+1-(2-3+5)*8)";

        List<String> expressionList = PolandNotation.toInFiExpressionList(expression);

        List<String> list = PolandNotation.parseSuffixExpression(expressionList);

        System.out.println(list);

        int calculate = PolandNotation.calculate(list);
        System.out.println(calculate);
    }


    @Test
    public void test02() {
        // 去掉符号优先级
//        String expression = "1-(2*3)";
        String expression = "27+(32*1+1-(2-3+5)*8)";

        List<String> expressionList = PolandNotation.toInFiExpressionList(expression);

        List<String> list = PolandNotation.parseSuffixExpression(expressionList);
        System.out.println(list);


        Stack<QueryBuilder> stack = new Stack<>();
        for (String s : list) {
            if (!isOperator(s)) {
                QueryBuilder queryBuilder = QueryBuilders.termQuery("labelId:" + s, "fdsfd_4234");
                stack.push(queryBuilder);
            } else {
                QueryBuilder queryBuilderRight = stack.pop();
                QueryBuilder queryBuilderLeft = stack.pop();
                switch (s) {
                    case "+" : {
                        // num1,num2找到对应的值域，类别
                        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                                .should(queryBuilderLeft)
                                .should(queryBuilderRight);
                        stack.push(boolQueryBuilder);
                        break;
                    }
                    case "-" : {
                        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                                .must(queryBuilderLeft)
                                .mustNot(queryBuilderRight);
                        stack.push(boolQueryBuilder);
                        break;
                    }
                    case "*" : {
                        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                                .must(queryBuilderLeft)
                                .must(queryBuilderRight);
                        stack.push(boolQueryBuilder);
                        break;
                    }
                    default:{
                        throw new RuntimeException();
                    }
                }
            }

        }
        QueryBuilder pop = stack.pop();
        System.out.println(pop.toString());
    }


    /**
     * 判断字符串是否为操作符
     *
     * @param op
     * @return
     */
    public static boolean isOperator(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }
}
