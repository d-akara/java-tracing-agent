package dakaraphi.devtools.tracing.hooks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.CannotCompileException;
import javassist.CtBehavior;

public class InputStreamHook {
    // public static String createHookStatements(final CtBehavior editableMethod, final String methodName, final Tracer classMethodDefinition) throws CannotCompileException {
    //     final String classname = editableMethod.getDeclaringClass().getName();
    //     final String methodInvocation = InputStreamHook.class.getPackage().getName() +"."+ InputStreamHook.class.getSimpleName() + ".asStreamAndContent";
    //     final String javaStatement = methodInvocation + "("+classMethodDefinition.id+", \""+classname+"\", \""+methodName+"\"" + constructVariablesString(classMethodDefinition.variables) +");";
    //     TraceLogger.log("inserting statement: " + javaStatement);
    //     performStatementInsertion(editableMethod, classMethodDefinition, javaStatement);
    // }

    public static StreamAndContent asStreamAndContent(InputStream is) throws IOException {
        StreamAndContent streamAndString = new StreamAndContent();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            streamAndString.content =  br.lines().collect(Collectors.joining(System.lineSeparator()));
        }

        streamAndString.inputStream = new ByteArrayInputStream(streamAndString.content.getBytes(StandardCharsets.UTF_8));

        return streamAndString;
    }
    public static class StreamAndContent {
        public InputStream inputStream;
        public String content;
    }
}
