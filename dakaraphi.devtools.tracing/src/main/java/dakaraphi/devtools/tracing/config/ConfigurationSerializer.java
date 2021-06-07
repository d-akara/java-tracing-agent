package dakaraphi.devtools.tracing.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationSerializer {
	public static final String FILE_PROPERTY_KEY = "dakaraphi.devtools.tracing.config.file";
	private final File configFile;
	private TracingConfig tracingConfig;

	public ConfigurationSerializer(File configFile) {
		this.configFile = configFile;
	}
	
	public TracingConfig readConfig() throws JsonParseException, JsonMappingException, IOException {
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		ObjectMapper mapper = new ObjectMapper(jsonFactory);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		tracingConfig = mapper.readValue(configFile, TracingConfig.class);

		return tracingConfig;
	}
}
