package top.xizai.log.test;

public class AgentLogTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("hello world");

        for (int i = 0; i < 10; i++) {
            String s = new BusinessTest()
                    .business1(i);
            System.out.println(s);
            Thread.sleep(4000);
        }
    }
}
