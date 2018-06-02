package dakaraphi.devtools.tracing.logger;

public class TraceLogger {
    public static void log(String content) {
        System.out.println("TracingAgent: " + content);
    }

    public static void trace(String content) {
        System.out.println("trace agent out: " + content);
    }
}