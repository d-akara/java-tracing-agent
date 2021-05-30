// Note this must not exist in 'dakaraphi.devtools.tracing' as that package is excluded from stack trace info in ApplicationHooks
package dakaraphi.devtools;

public class TestApplication {
    public static void main(String[] args) {
        System.out.println("Test application");
        for (int count = 0; count < 2; count++) {
            method2();
        }

    }
    public static void method2() {
        System.out.println("Method 2 executed");
    }
}
