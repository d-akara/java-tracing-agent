package dakaraphi.devtools.tracing.filter;

import java.util.List;
import java.util.regex.Pattern;

import dakaraphi.devtools.tracing.config.Filter;

public class LogFilter {
    public static boolean isIncluded(List<Filter> filters, String content) {

        for (Filter filter : filters) {
            if ("exclude".equals(filter.type)) {
                // TODO we should find a location to precompile the regex
                if (Pattern.compile(filter.regex).matcher(content).matches()) return false;
            } else if ("include".equals(filter.type)) {
                if (!Pattern.compile(filter.regex).matcher(content).matches()) return false;
            }
        }

        return true;
    }
}