package dakaraphi.devtools.tracing.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import dakaraphi.devtools.tracing.ClassMethodSelector;
import dakaraphi.devtools.tracing.ClassMethodSelector.ClassMethodDefinition;

public class ConfigurationSerializer {
	public static final String FILE_PROPERTY_KEY = "dakaraphi.devtools.tracing.config.file";
	
	private TracingConfig readConfig() {
		String filename = System.getProperty(FILE_PROPERTY_KEY);
		TracingConfig config = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			config = mapper.readValue(new File(filename), TracingConfig.class);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	public void map(ClassMethodSelector classMethodSelector) {
		TracingConfig config = readConfig();
		for (Tracer tracerDefinition : config.tracers) {
			classMethodSelector.addDefinition(new ClassMethodDefinition(tracerDefinition.classRegex, 
																		tracerDefinition.methodRegex, 
																		tracerDefinition.action, 
																		Integer.parseInt(tracerDefinition.line), 
																		tracerDefinition.variables));
		}
	}
}
