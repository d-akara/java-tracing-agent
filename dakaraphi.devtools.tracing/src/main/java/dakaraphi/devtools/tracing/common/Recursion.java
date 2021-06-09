package dakaraphi.devtools.tracing.common;

public class Recursion {
    // use counter to determine if there is recursion into our hooks
	// if so, this may indicate a low level method has been traced that we ourself use.  For example System.out.prinln
	private static ThreadLocal<Integer> detectRecursion = ThreadLocal.withInitial(()-> 0);
	public static boolean isRecursion() {
		final int current = detectRecursion.get();
		detectRecursion.set(current + 1);
		if (current > 0) return true;
		return false;
	}
	public static void exitRecursionCheck() {
		final int current = detectRecursion.get();
		detectRecursion.set(current - 1);
	}
}
