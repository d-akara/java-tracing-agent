package dakaraphi.devtools.tracing;

import java.util.List;

import dakaraphi.devtools.tracing.config.Tracer;
import dakaraphi.devtools.tracing.config.Tracer.Variable;
import dakaraphi.devtools.tracing.hooks.ApplicationHooks;
import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.CannotCompileException;
import javassist.CtBehavior;

// TODO - the method rewrite is specific to the ApplicationHooks logger.  We should refactor for common features of rewriters such as the new InputStreamHook
public class MethodRewriter {

	public void editMethod(final CtBehavior editableMethod, final String methodName, final Tracer classMethodDefinition) throws CannotCompileException {
		final String classname = editableMethod.getDeclaringClass().getName();
		final String methodInvocation = ApplicationHooks.class.getPackage().getName() +"."+ ApplicationHooks.class.getSimpleName() + ".logMethodParameters";
		final String javaStatement = methodInvocation + "("+classMethodDefinition.id+", \""+classname+"\", \""+methodName+"\"" + constructVariablesString(classMethodDefinition.variables) +");";
		TraceLogger.log("inserting statement: " + javaStatement);
		performStatementInsertion(editableMethod, classMethodDefinition, javaStatement);
	}
	
	// http://jboss-javassist.github.io/javassist/tutorial/tutorial2.html#before
	private String constructVariablesString(List<Variable> variables) {
		if (variables.size() == 0) return ", null";

		StringBuilder builder = new StringBuilder();
		builder.append(", new java.lang.Object[]{");
		boolean first = true;
		for (Variable variable : variables) {
			if ( !first ) builder.append(',');
			builder.append(variable.expression);
			first = false;
		}
		builder.append("}");
		return builder.toString();
	}

	private void performStatementInsertion(final CtBehavior editableMethod, final Tracer classMethodDefinition, final String javaStatement) throws CannotCompileException {
		if ( classMethodDefinition.line == ClassMethodSelector.LINE_LAST)
			editableMethod.insertAfter(javaStatement);
		else if (classMethodDefinition.line == ClassMethodSelector.LINE_FIRST) {
			editableMethod.insertBefore(javaStatement);
		} else {
			editableMethod.insertAt(classMethodDefinition.line, javaStatement);
		}
	}
}
