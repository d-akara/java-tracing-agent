package dakaraphi.devtools.tracing;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dakaraphi.devtools.tracing.config.ConfigurationSerializer;
import dakaraphi.devtools.tracing.config.TracingConfig;
import dakaraphi.devtools.tracing.filewatcher.FileWatcher;
import dakaraphi.devtools.tracing.filewatcher.IFileListener;
import dakaraphi.devtools.tracing.logger.TraceLogger;
import dakaraphi.devtools.tracing.metrics.ExecutionCounts;

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
	public static TracingConfig tracingConfig = null;
    public static void premain(String agentArgs, Instrumentation instrumentation) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
    	TraceLogger.log("Starting v1.0.7");
		String tracerDefinitionFile = System.getProperty(ConfigurationSerializer.FILE_PROPERTY_KEY);
		if (tracerDefinitionFile == null) {
			TraceLogger.log("Missing system property " + ConfigurationSerializer.FILE_PROPERTY_KEY);
			return;
		}
		final File configFile = new File(System.getProperty(ConfigurationSerializer.FILE_PROPERTY_KEY));
		TraceLogger.log("using tracer definitions: " + configFile);

		ClassMethodSelector selector = null;
		try {
			selector = loadConfig(configFile);
		} catch (Throwable e) {
			TraceLogger.log("unable to load configuration definition - " + e.getMessage());
		}
    	
    	final TracingTransformer transformer = new TracingTransformer(instrumentation, selector, new MethodRewriter());
    	
    	FileWatcher.createFileWatcher().addListener(new IFileListener() {
			public void onFileChange() throws Throwable {
				ClassMethodSelector selector = loadConfig(configFile);
				transformer.setClassMethodSelector(selector);
				transformer.retransform();
				StacktraceHasher.clear();
				ExecutionCounts.clear();
			}
		}, configFile).start();
	}
	
	private static ClassMethodSelector loadConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
		ConfigurationSerializer serializer = new ConfigurationSerializer(configFile);
		tracingConfig = serializer.readConfig();
		return ClassMethodSelector.makeSelector(tracingConfig);
	}
}

/*
 TODO 
 - consider exposing a live interface using - https://github.com/perwendel/spark
 - add support for triggers
 - add support for constructors
 - prevent infinite recursion if someone attempts to trace a JDK class that the tracer itself uses
 - allow tracers to override the global logConfig
 - add support for timers.  Just measure and log diff between each occurrence of timer by same name. option, on same thread.
*/

