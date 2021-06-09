package dakaraphi.devtools.tracing;

import java.util.ArrayList;
import java.util.List;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.config.TracingConfig;
import dakaraphi.devtools.tracing.config.Tracer.Type;

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
    
    public boolean shouldTransformMethod(final String classNameDotted, final String methodName, final List<String> typeNames) {
		Tracer definition = findMatchingDefinition(classNameDotted, methodName, typeNames);
    	if (definition != null && definition.enabled)
    		return true;
    	return false;
    }
	
	public Tracer findMatchingDefinition(final String classNameDotted, final String methodName, final List<String> typeNames) {
		for (final Tracer definition : definitionList) {
			if ( doesDefinitionMatch(definition, classNameDotted, methodName, typeNames)) {
				return definition;
			}
		}
		return null;
	}
	public Tracer findEnabledMatchingDefinition(final String classNameDotted, final String methodName) {
		for (final Tracer definition : definitionList) {
			if ( doesDefinitionMatch(definition, classNameDotted, methodName, null) && definition.enabled) {
				return definition;
			}
		}
		return null;
	}

	/*
	 * Find definition that matches the classname and methodname or
	 * just classname if methodname is null.
	 */
	private boolean doesDefinitionMatch(final Tracer definition, final String classNameDotted, final String methodName, final List<String> typeNames) {
		if ( definition.classRegex.matcher(classNameDotted).matches() 
		     && (methodName == null || definition.methodRegex.matcher(methodName).matches())  // if methodName null then match not required on method
			 && (typeNames == null || doTypesMatch(definition, typeNames))) { // if typeNames null then match not required on types
			return true;
		}
		return false;
	}

	private boolean doTypesMatch(final Tracer definition, final List<String> typeNames) {
		for (Type type : definition.types) {
			if (((type.index < typeNames.size()) && (type.typeRegex.matcher(typeNames.get(type.index)).matches()))) {
				// all specified definitions for types must match
				continue;
			}
			return false;
		}
		return true;
	}
}
