package dakaraphi.devtools.tracing;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.ByteArrayClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class TracingTransformer implements ClassFileTransformer {
	private static Instrumentation instrumentation;
	private ClassMethodSelector classMethodSelector;
	private MethodRewriter methodRewriter;
	public TracingTransformer(final Instrumentation instrumentation, ClassMethodSelector classMethodSelector, MethodRewriter methodRewriter) {
		this.classMethodSelector = classMethodSelector;
		this.methodRewriter = methodRewriter;
		instrumentation.addTransformer(this, true);
		TracingTransformer.instrumentation = instrumentation;
		TraceLogger.log("TracingTransformer active");
		TraceLogger.log("TracingTransformer classloader: " + TracingAgent.class.getClassLoader());
	}
	
    public byte[] transform(final ClassLoader loader, final String className, final Class classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        final String classNameDotted = className.replaceAll("/", ".");

        if (classMethodSelector.shouldTransformClass(classNameDotted)) {
 
            try {
                final ClassPool classpool = ClassPool.getDefault();
                final ClassPath loaderClassPath = new LoaderClassPath(loader);
                final ClassPath byteArrayClassPath = new ByteArrayClassPath(classNameDotted, byteCode);
                // We add the loaderClassPath so that the classpool can find the dependencies needed when it needs to recompile the class
                classpool.appendClassPath(loaderClassPath);
                // This class has not yet actually been loaded by any classloader, so we must add the class directly so it can be found by the classpool.
                classpool.insertClassPath(byteArrayClassPath);
                
                final CtClass editableClass = classpool.get(classNameDotted);
                final CtMethod declaredMethods[] = editableClass.getDeclaredMethods();
                boolean modifiedMethods = false;
                for (final CtMethod editableMethod : declaredMethods) {
                	if (classMethodSelector.shouldTransformMethod(classNameDotted, editableMethod.getName())) {
                        // TODO should allow for multiple matching definitions being processed
                        methodRewriter.editMethod(editableMethod, classMethodSelector.findMatchingDefinition(classNameDotted, editableMethod.getName()));
                        modifiedMethods = true;
                	}
                }
                if (modifiedMethods)
                    byteCode = editableClass.toBytecode();
                editableClass.detach();
                
                // These appear to only be needed during rewriting
                // If we don't remove, the list just keeps growing as we rewrite more classes
                // or transform the same class again
                classpool.removeClassPath(loaderClassPath);
                classpool.removeClassPath(byteArrayClassPath);
                
                if (modifiedMethods)
                    TraceLogger.log("Transformed " + classNameDotted);
            } catch (Throwable ex) {
            	System.err.println("Unable to transform: " + classNameDotted);
                ex.printStackTrace();
            }
        }
 
        return byteCode;
    }
    

    public void setClassMethodSelector(ClassMethodSelector classMethodSelector) {
    	this.classMethodSelector = classMethodSelector;
    }

    public void setMethodRewriter(MethodRewriter methodRewriter) {
    	this.methodRewriter = methodRewriter;
    }

    public void retransform() {
    	final Class[] loadedClasses = instrumentation.getAllLoadedClasses();
    	for (final Class clazz : loadedClasses) {
            final String classNameDotted = clazz.getName().replaceAll("/", ".");
            if (classMethodSelector.shouldTransformClass(classNameDotted)) {
    			try {
    				TraceLogger.log("retransform " + clazz);
					instrumentation.retransformClasses(clazz);

				} catch (UnmodifiableClassException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
}
