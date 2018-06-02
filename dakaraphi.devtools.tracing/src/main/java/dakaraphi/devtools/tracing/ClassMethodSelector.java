package dakaraphi.devtools.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.config.TracingConfig;

public class ClassMethodSelector {
	public static final int LINE_LAST = -1;
	public static final int LINE_FIRST = 0;
	private final List<ClassMethodDefinition> definitionList = new ArrayList<ClassMethodSelector.ClassMethodDefinition>();

	public static ClassMethodSelector makeSelector(TracingConfig config) {
		ClassMethodSelector classMethodSelector = new ClassMethodSelector();
		for (Tracer tracerDefinition : config.tracers) {
			if (tracerDefinition.enabled)
				classMethodSelector.addDefinition(new ClassMethodDefinition(tracerDefinition.classRegex, 
																			tracerDefinition.methodRegex, 
																			tracerDefinition.action, 
																			Integer.parseInt(tracerDefinition.line), 
																			tracerDefinition.variables));
		}
		return classMethodSelector;
	}

	public void addDefinition(ClassMethodDefinition classMethodDefinition) {
		definitionList.add(classMethodDefinition);
	}
    public boolean shouldTransformClass(final String classNameDotted) {
    	if (findMatchingDefinition(classNameDotted, null) != null)
    		return true;
    	return false;
    }
    
    public boolean shouldTransform(final String classNameDotted, final String methodName) {
    	if (findMatchingDefinition(classNameDotted, methodName) != null)
    		return true;
    	return false;
    }
	
	public ClassMethodDefinition findMatchingDefinition(final String classNameDotted, final String methodName) {
		for (final ClassMethodDefinition definition : definitionList) {
			if ( doesDefinitionMatch(definition, classNameDotted, methodName)) {
				return definition;
			}
		}
		return null;
	}
	
	
	/*
	 * Find definition that matches the classname and methodname or
	 * just classname if methodname is null.
	 */
	private boolean doesDefinitionMatch(final ClassMethodDefinition definition, final String classNameDotted, final String methodName) {
		if ( definition.classRegex.matcher(classNameDotted).matches() && (methodName == null || definition.methodRegex.matcher(methodName).matches())) {
			return true;
		}
		return false;
	}
	
	public static class ClassMethodDefinition {
		public final Pattern classRegex;
		public final Pattern methodRegex;
		public final String action;
		public final int lineLocation;
		// http://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#before
		public final List<String> parameters;
		public ClassMethodDefinition(String classRegex, String methodRegex, String action, int lineLocation, List<String> parameters) {
			this.classRegex = Pattern.compile(classRegex);
			this.methodRegex = Pattern.compile(methodRegex);
			this.action = action;
			this.lineLocation = lineLocation;
			this.parameters = parameters;
		}
	}
}
