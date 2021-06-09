package dakaraphi.devtools.tracing.common;

import java.util.List;
import java.util.function.Function;

public class Common {
    public static <T> int indexInList(List<T> list, Function<T, Boolean> conditional) {
        int index = 0;
        for (T item : list) {
            if (conditional.apply(item)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
