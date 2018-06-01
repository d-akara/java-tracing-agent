package dakaraphi.devtools.tracing;

import java.util.List;

import dakaraphi.devtools.tracing.ClassMethodSelector.ClassMethodDefinition;
import dakaraphi.devtools.tracing.config.TracingConfig;
import dakaraphi.devtools.tracing.filter.LogFilter;
import javassist.CannotCompileException;
import javassist.CtMethod;

public class MethodRewriter {

	public void editMethod(final CtMethod editableMethod, final ClassMethodDefinition classMethodDefinition) throws CannotCompileException {
		final String classname = editableMethod.getDeclaringClass().getName();
		final String methodInvocation = this.getClass().getPackage().getName() +"."+ this.getClass().getSimpleName() + "."+classMethodDefinition.action;
		final String javaStatement = methodInvocation + "(\""+classname+"\", \""+editableMethod.getName()+"\"" + constructVariablesString(classMethodDefinition.parameters) +");";
		System.out.println("inserting statement: " + javaStatement);
		performStatementInsertion(editableMethod, classMethodDefinition, javaStatement);
	}
	
	private String constructVariablesString(List<String> variables) {
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

	private void performStatementInsertion(final CtMethod editableMethod, final ClassMethodDefinition classMethodDefinition, final String javaStatement) throws CannotCompileException {
		if ( classMethodDefinition.lineLocation == ClassMethodSelector.LINE_LAST)
			editableMethod.insertAfter(javaStatement);
		else if (classMethodDefinition.lineLocation == ClassMethodSelector.LINE_FIRST) {
			editableMethod.insertBefore(javaStatement);
		} else {
			editableMethod.insertAt(classMethodDefinition.lineLocation, javaStatement);
		}
	}
	
	public static void logMethodParameters(final String classname, final String methodname, final Object[] parameters) {
		StringBuilder builder = new StringBuilder();
		builder.append(classname + ": "+methodname);
		//builder.append('\n');
		int count = 0;
		for ( final Object parameter : parameters ) {
			if ( parameter instanceof Object[]) {
				int listIndex = 0;
				for ( final Object nestedParameter : (Object[])parameter) {
					builder.append(" :list-value "+listIndex+++": " + nestedParameter);
				}
			} else {
				builder.append(" :value "+count+++": " + parameter);
			}
			//builder.append('\n');
		}
		writeLog(builder.toString());
	}
	
	public static void writeLog(String text) {
		StringBuilder builder = new StringBuilder();
		String time = new java.text.SimpleDateFormat("hh:mm:ss,SSS").format(new java.util.Date());
		String thread = Thread.currentThread().getName();
		builder.append(time);
		builder.append(" thread:[");
		builder.append(thread);
		builder.append("]: ");	
		builder.append(text);
		String output = builder.toString();

		if (LogFilter.isIncluded(TracingAgent.tracingConfig.filters, output))
			System.out.println(builder.toString());
	}
}
