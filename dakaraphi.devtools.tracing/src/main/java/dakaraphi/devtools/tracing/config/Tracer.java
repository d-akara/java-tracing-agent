package dakaraphi.devtools.tracing.config;

import java.util.List;

public class Tracer {
	public String classRegex;
	public String methodRegex;
	public String line;
	public String action;
	public List<String> variables;
	public StackTraceCondition stackTraceCondition;
}
