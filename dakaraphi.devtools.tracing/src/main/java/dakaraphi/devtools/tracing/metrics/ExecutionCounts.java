package dakaraphi.devtools.tracing.metrics;

import java.util.HashMap;
import java.util.Map;

public class ExecutionCounts {
    private static Map<Integer,Integer> executionCounts = new HashMap<>();
	public static synchronized Integer incrementExecutionCount(Integer tracerId) {
		Integer count = executionCounts.get(tracerId);
		if (count == null) {
			count = 0;
		}
		count++;
		executionCounts.put(tracerId, count);
		return count;
	} 
    public static void clear() {
        executionCounts.clear();
    }
}
