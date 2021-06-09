// Note this must not exist in 'dakaraphi.devtools.tracing' as that package is excluded from stack trace info in ApplicationHooks
package dakaraphi.devtools;

import org.junit.Test;

public class TestApplication {
    public static void main(String[] args) throws Exception {
        System.out.println("Test application");
        for (int count = 0; count < 2; count++) {
            method2();
        }
        method3("testing");

        new SimpleObject();
        new PlainObject();

        Thread background = new Thread(()-> backgroundTask());
        background.setDaemon(true);
        background.start();
        Thread.sleep(20000);
    }
    public static void method2() {
        System.out.println("Method 2 executed");
    }

    public static void method3(String name) {
        System.out.println(name);
    }

    public static void backgroundTask() {
        try {
            while(true) {
                System.out.println("background task");
                Thread.sleep(1000);
            }
        } catch(Throwable e) {

        }
    }
    public static class SimpleObject {
        public SimpleObject() {
            System.out.println("invoked constructor");
        }
    }
}

