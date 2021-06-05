package dakaraphi.devtools.tracing;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.ByteArrayClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class TracingTransformer implements ClassFileTransformer {
	private static Instrumentation instrumentation;
    private static Set<Class> redefinedClasses = new HashSet<>();
	private ClassMethodSelector classMethodSelector;
	private MethodRewriter methodRewriter;
	public TracingTransformer(final Instrumentation instrumentation, ClassMethodSelector classMethodSelector, MethodRewriter methodRewriter) {
		this.classMethodSelector = classMethodSelector;
		this.methodRewriter = methodRewriter;
		instrumentation.addTransformer(this, true);
		TracingTransformer.instrumentation = instrumentation;
		TraceLogger.log("TracingTransformer active");
	}
	
    public byte[] transform(final ClassLoader loader, final String className, final Class classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        final String classNameDotted = className.replaceAll("/", ".");

        // process the transform if we have tracers defined or if it was previously transformed as we might need to restore if the tracer definitions changed
        if (shouldTransformClass(classNameDotted, classBeingRedefined)) {
 
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
                        // TODO we might be broken handling primitives.  Need to handle or filter out for now
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
                
                if (modifiedMethods) {
                    redefinedClasses.add(classBeingRedefined);
                    TraceLogger.log("modified class - " + classNameDotted);
                } else {
                    // If this class was previously transformed we can now remove it
                    // from our list as it is not transformed using the current rules
                    if (redefinedClasses.remove(classBeingRedefined))
                        TraceLogger.log("restored class - " + classNameDotted);
                }
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
        // first retransform any previous loaded classes.  
        // this is to ensure if rules in the tracer.json have changed that we can restore
        // classes that may have been removed from the tracer.json or rules don't apply anymore
        for (final Class clazz : Set.copyOf(redefinedClasses)) {
            attemptTransform(clazz);
        }

    	final Class[] loadedClasses = instrumentation.getAllLoadedClasses();
    	for (final Class clazz : loadedClasses) {
            if (!redefinedClasses.contains(clazz)) {
                // we have not processed this class yet
                attemptTransform(clazz);
            }
    	}
    }

    private void attemptTransform(final Class clazz) {
        final String classNameDotted = clazz.getName().replaceAll("/", ".");
        if (shouldTransformClass(classNameDotted, clazz)) {
        	try {
        		TraceLogger.log("re-evaluating rule definitions for class - " + clazz);
        		instrumentation.retransformClasses(clazz);

        	} catch (UnmodifiableClassException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }
    }

    private boolean shouldTransformClass(String classNameDotted, Class classBeingRedefined) {
        return (classMethodSelector.shouldTransformClass(classNameDotted) || redefinedClasses.contains(classBeingRedefined));
    }
}
