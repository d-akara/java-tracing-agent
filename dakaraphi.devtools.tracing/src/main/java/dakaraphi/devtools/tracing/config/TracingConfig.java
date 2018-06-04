package dakaraphi.devtools.tracing.config;

import java.util.List;

public class TracingConfig {
	public List<Tracer> tracers;
	public List<Filter> filters;
	public LogConfig logConfig = new LogConfig();

	public static class LogConfig {
		public boolean threadName;
		public boolean threadId;
		public boolean multiLine;
	}
}
