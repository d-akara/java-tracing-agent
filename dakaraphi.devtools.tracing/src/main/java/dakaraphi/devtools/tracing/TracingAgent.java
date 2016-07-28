package dakaraphi.devtools.tracing;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

import dakaraphi.devtools.tracing.config.ConfigurationSerializer;

/**
 * TracingAgent can only be launched within a jar file.
 * You must specify the following JVM property at launch to indicate location of the agent jar file
 * -javaagent: dakaraphi.devtools.tracing.jar
 * 
 * If running inside an OSGI application.  You must make this class and its dependencies global to all OSGI modules
 * org.osgi.framework.bootdelegation = dakaraphi.devtools.*
 * 
 * These classes must also be loaded by the JVM boot class loader so that they can be invoked before any other classes are loaded.
 * This is done by setting the JAR's manifest to have this entry.
 * Boot-Class-Path: dakaraphi.devtools.tracing-all.jar
 * 
 * @author chadmeadows
 *
 */
public class TracingAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
    	System.out.println("Starting TracingAgent v0.2.5");
       	System.out.println("TracingAgent classloader: " + TracingAgent.class.getClassLoader());
    	ClassMethodSelector selector = new ClassMethodSelector();
    	ConfigurationSerializer serializer = new ConfigurationSerializer();
    	serializer.map(selector);
    	
    	TracingTransformer transformer = new TracingTransformer(instrumentation, selector, new MethodRewriter());
    }
}
