package dakaraphi.devtools.tracing;

import java.util.ArrayList;
import java.util.List;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.config.TracingConfig;

public class ClassMethodSelector {
	public static final int LINE_LAST = -1;
	public static final int LINE_FIRST = 0;
	private final List<Tracer> definitionList = new ArrayList<Tracer>();

	public static ClassMethodSelector makeSelector(TracingConfig config) {
		ClassMethodSelector classMethodSelector = new ClassMethodSelector();
		int id = 0;
		for (Tracer tracerDefinition : config.tracers) {
			tracerDefinition.id = id++; // give each tracer an id matching the index
			classMethodSelector.addDefinition(tracerDefinition);
		}
		return classMethodSelector;
	}

	public void addDefinition(Tracer classMethodDefinition) {
		definitionList.add(classMethodDefinition);
	}
    public boolean shouldTransformClass(final String classNameDotted) {
    	if (findEnabledMatchingDefinition(classNameDotted, null) != null)
    		return true;
    	return false;
	}
    
    public boolean shouldTransformMethod(final String classNameDotted, final String methodName) {
		Tracer definition = findMatchingDefinition(classNameDotted, methodName);
    	if (definition != null && definition.enabled)
    		return true;
    	return false;
    }
	
	public Tracer findMatchingDefinition(final String classNameDotted, final String methodName) {
		for (final Tracer definition : definitionList) {
			if ( doesDefinitionMatch(definition, classNameDotted, methodName)) {
				return definition;
			}
		}
		return null;
	}
	public Tracer findEnabledMatchingDefinition(final String classNameDotted, final String methodName) {
		for (final Tracer definition : definitionList) {
			if ( doesDefinitionMatch(definition, classNameDotted, methodName) && definition.enabled) {
				return definition;
			}
		}
		return null;
	}

	/*
	 * Find definition that matches the classname and methodname or
	 * just classname if methodname is null.
	 */
	private boolean doesDefinitionMatch(final Tracer definition, final String classNameDotted, final String methodName) {
		if ( definition.classRegex.matcher(classNameDotted).matches() && (methodName == null || definition.methodRegex.matcher(methodName).matches())) {
			return true;
		}
		return false;
	}
}
