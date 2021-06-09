package dakaraphi.devtools.tracing;

import java.util.HashSet;
import java.util.Set;

public class StacktraceHasher {
    private static final Set<Integer> stackHashes = new HashSet<>();
    public static StacktraceHash getExistingStackFrameHashChain(StackTraceElement stackFrames[]) {
        int hashValue = 0;
        for (StackTraceElement stackFrame : stackFrames)
            hashValue ^= stackFrame.hashCode() ^ stackFrame.getLineNumber();
        
        Integer hashValueInteger = Integer.valueOf(hashValue);
        if (stackHashes.contains(hashValueInteger))
            return new StacktraceHash(false, hashValue);

        stackHashes.add(hashValueInteger);
        return new StacktraceHash(true, hashValue);  // no existing hash.  
    }

    public static void clear() {
        stackHashes.clear();
    }

    public static class StacktraceHash {
        public boolean firstOccurrence;
        public int hashValue;
        public StacktraceHash(boolean firstOccurrence, int hashValue) {
            this.firstOccurrence = firstOccurrence;
            this.hashValue = hashValue;
        }
    }
}
