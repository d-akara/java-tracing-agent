package dakaraphi.devtools.tracing;

import java.util.List;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.CannotCompileException;
import javassist.CtMethod;

public class MethodRewriter {

	public void editMethod(final CtMethod editableMethod, final Tracer classMethodDefinition) throws CannotCompileException {
		final String classname = editableMethod.getDeclaringClass().getName();
		// TODO - need to add some Trace id so we can lookup the tracer on invocation
		final String methodInvocation = ApplicationHooks.class.getPackage().getName() +"."+ ApplicationHooks.class.getSimpleName() + ".logMethodParameters";
		final String javaStatement = methodInvocation + "("+classMethodDefinition.id+", \""+classname+"\", \""+editableMethod.getName()+"\"" + constructVariablesString(classMethodDefinition.variables) +");";
		TraceLogger.log("inserting statement: " + javaStatement);
		performStatementInsertion(editableMethod, classMethodDefinition, javaStatement);
	}
	
	// http://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#before
	private String constructVariablesString(List<String> variables) {
		if (variables == null) return ", null";

		StringBuilder builder = new StringBuilder();
		builder.append(", new java.lang.Object[]{");
		boolean first = true;
		for (String variable : variables) {
			if ( !first ) builder.append(',');
			builder.append(variable);
			first = false;
		}
		builder.append("}");
		return builder.toString();
	}

	private void performStatementInsertion(final CtMethod editableMethod, final Tracer classMethodDefinition, final String javaStatement) throws CannotCompileException {
		if ( classMethodDefinition.line == ClassMethodSelector.LINE_LAST)
			editableMethod.insertAfter(javaStatement);
		else if (classMethodDefinition.line == ClassMethodSelector.LINE_FIRST) {
			editableMethod.insertBefore(javaStatement);
		} else {
			editableMethod.insertAt(classMethodDefinition.line, javaStatement);
		}
	}
}
