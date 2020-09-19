package com.example.highlevelclient.express;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author wangxi created on 2020/9/19 22:48
 * @version v1.0
 *
 * https://cloud.tencent.com/developer/article/1588044
 *
 *
 * https://cloud.tencent.com/developer/information/java%E6%A0%88%E5%90%8E%E7%BC%80%E8%A1%A8%E8%BE%BE%E5%BC%8F
 */
public class PolandNotation {
    public static void main(String[] args) {
        //中缀表达式转后缀表达式
//        String expression = "1+((2+3*2/1+2-1)*4)-5";
        String expression = "27+(32*1+1-(2-3+5)*8)";
        //将中缀表达式对应的list转化为后缀表达式对应的list
        List<String> parseSuffixExpression = parseSuffixExpression(toInFiExpressionList(expression));
        System.out.println("parseSuffixExpression: " + parseSuffixExpression);

        System.out.println(calculate(parseSuffixExpression));

    }

    /*
    中缀表达式转后缀表达式
1) 初始化两个栈，运算符栈 S1 存储中间结果栈 S2
2)从左至右扫描中缀表达式
3)当遇到操作数，将其压入 S2 栈
4)如果遇到运算符，比较其与 S1 栈顶的优先级
1.如果S1为空，或栈顶运算符为"(",则直接将此运算符入栈
2.否则，若优先级比栈顶运算符高，也将其压入 S1 栈
3.否则，将 S1 栈顶的运算符弹出并压入到 S2 中，
再次转到 4.1 与 S1 中新的栈顶运算符相比较
5）遇到括号时：
1.如果是左括号"(",则直接压入 S1
2.如果是右括号")“，则依次弹出 S1 栈顶的运算符，并压入 S2,直到遇到左括号为止，此时将这一对括号丢弃
6)重复步骤2至5，直到表达式的最右边
7)将S1中剩余的运算符依次弹出并压入 S2
8)依次弹出S2中元素并输出，结果的逆序即为中缀表达式对应的后缀表达式
     */
    public static List<String> parseSuffixExpression(List<String> ls) {
        //定义两个栈
        Stack<String> s1 = new Stack<>();
        //因为 S2 这个栈在整个转换过程中，没有 pop 操作，而且后面我们还需要逆序输出，会比较麻烦，因此这里用 list
        ArrayList<String> s2 = new ArrayList<>();

        for (String item : ls) {
            if (item.matches("\\d+")) {
                s2.add(item);
            } else if (item.equals("(")) {
                s1.push(item);
            } else if (item.equals(")")) {
                while (!s1.peek().equals("(")) {
                    s2.add(s1.pop());
                }
                s1.pop();
            } else {
                //当 s1 栈顶的运算符优先级大于等 item 的优先级，将 s1 栈顶弹出并加入 s2
                while (s1.size() != 0 && Operation.getValue(s1.peek()) >= Operation.getValue(item)) {
//                while (s1.size() != 0) {
                    s2.add(s1.pop());
                }
                s1.push(item);
            }
        }

        //将 s1 中剩余的运算符依次弹出加入 s2 中
        while (s1.size() != 0) {
            s2.add(s1.pop());
        }

        return s2;
    }


    //将中缀表达式转化成对应的list
    public static List<String> toInFiExpressionList(String s) {
        ArrayList<String> ls = new ArrayList<>();
        int i = 0;
        //多位数拼接
        String str;
        char c;
        do {
            //非数字
            if ((c = s.charAt(i)) < 48 || (c = s.charAt(i)) > 57) {
                ls.add("" + c);
                i++;
            } else {
                str = "";
                while (i < s.length() && (c = s.charAt(i)) >= 48 && (c = s.charAt(i)) <= 57) {
                    str += c;
                    i++;
                }
                ls.add(str);
            }

        } while (i < s.length());
        return ls;
    }


    //将逆波兰表达式转化为list
    public static List<String> getListString(String suffixExpression) {
        String[] split = suffixExpression.split(" ");
        ArrayList<String> list = new ArrayList<>();

        for (String s : split) {
            list.add(s);
        }
        return list;
    }


    /**
     * 逆波兰表达式 求值
     * <p>
     * 从左至右扫描表达式，
     * 遇到数字，将数字压入堆栈，
     * 遇到运算符，弹出栈顶的两个数，
     * 用运算符对它们做相应的计算(次栈顶元素和栈顶元素)，并将结果入栈。
     * 重复上述过程直到表达式最右端，最后运算得出的值即为表达式的结果。
     *
     * @param s
     * @return
     */
    public static int calculate(List<String> s) {
        Stack<String> stack = new Stack<>();
        for (String item : s) {
            if (item.matches("\\d+")) {
                stack.push(item);
            } else {
                int num1 = Integer.parseInt(stack.pop());
                int num2 = Integer.parseInt(stack.pop());
                int res = 0;
                if (item.equals("+")) {
                    res = num2 + num1;
                } else if (item.equals("-")) {
                    res = num2 - num1;
                } else if (item.equals("*")) {
                    res = num2 * num1;
                } else if (item.equals("/")) {
                    res = num2 / num1;
                } else {
                    throw new RuntimeException("运算符有误");
                }
                stack.push("" + res);
            }
        }
        return Integer.parseInt(stack.pop());
    }
}


class Operation {
    private static int ADD = 1;
    private static int SUB = 1;
    // 去掉符号* 、 / 的优先级，统一为1
    private static int MUL = 1;
    private static int DIV = 1;

    public static int getValue(String operation) {
        int result = 0;
        switch (operation) {
            case "+":
                result = ADD;
                break;
            case "-":
                result = SUB;
                break;
            case "*":
                result = MUL;
                break;
            case "/":
                result = DIV;
                break;
            default:
//                System.out.println("不存在运算符");
//                break;
                return 0;
        }
        return result;
    }
}
