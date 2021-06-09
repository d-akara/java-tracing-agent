package dakaraphi.devtools.tracing.config;

import java.io.File;

import org.junit.Test;

public class ConfigurationSerializer_Test {

	@Test
	public void canReadJsonFile() throws Exception {
		File tracerFile = new File(ConfigurationSerializer_Test.class.getResource("/dakaraphi/devtools/tracing/config/tracer.json").toURI());
		ConfigurationSerializer serializer = new ConfigurationSerializer(tracerFile);
		TracingConfig config = serializer.readConfig();
		System.out.println(config.tracers.get(0).name);
	}
}
