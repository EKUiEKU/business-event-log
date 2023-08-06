package top.xizai.log.test;

public class BusinessTest {
    public String business1(int i) {
        System.out.println(".......业务1");

        if (i == 3) {
            throw new UnsupportedOperationException("不支持啦");
        }
        return new Business2Test().business2();
    }
}
