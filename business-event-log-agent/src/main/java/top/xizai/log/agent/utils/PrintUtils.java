package top.xizai.log.agent.utils;

import java.util.Arrays;

public class PrintUtils {

    public static void printText(String str) {
        System.out.println(str);
    }

    public static void printObject(String name, Object value) {
        if (value == null) {
            System.out.println("null");
        } else {
            if (value instanceof Object[]) {
                System.out
                        .println(name + value.getClass().getSimpleName() + "，参数值：" + Arrays.toString((Object[])value));
            } else {
                System.out.println(name + value.getClass().getSimpleName() + "，参数值：" + value);
            }
        }
    }

    public static void printSpendTime(String methodName, long startTime) {
        System.out.println(methodName + " 耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("*************************************************");
    }


    public static void printException(String methodName, Exception exception) {
        System.out.println("监控 -> [方法名：" + methodName + "，异常信息：" + exception.getMessage() + "]");
    }
}