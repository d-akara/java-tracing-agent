package dakaraphi.devtools.tracing;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dakaraphi.devtools.tracing.logger.TraceLogger;
import javassist.ByteArrayClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.LoaderClassPath;

public class TracingTransformer implements ClassFileTransformer {
	private static Instrumentation instrumentation;
    private static Set<String> redefinedClasses = new HashSet<>();
	private ClassMethodSelector classMethodSelector;
	private MethodRewriter methodRewriter;
	public TracingTransformer(final Instrumentation instrumentation, ClassMethodSelector classMethodSelector, MethodRewriter methodRewriter) {
		this.classMethodSelector = classMethodSelector;
		this.methodRewriter = methodRewriter;
		instrumentation.addTransformer(this, true);
		TracingTransformer.instrumentation = instrumentation;
		TraceLogger.log("TracingTransformer active");
	}
	
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        final String classNameDotted = className.replaceAll("/", ".");

        // process the transform if we have tracers defined or if it was previously transformed as we might need to restore if the tracer definitions changed
        if (shouldTransformClass(classNameDotted)) {
 
            try {
                final ClassPool classpool = ClassPool.getDefault();
                final ClassPath loaderClassPath = new LoaderClassPath(loader);
                final ClassPath byteArrayClassPath = new ByteArrayClassPath(classNameDotted, byteCode);
                // We add the loaderClassPath so that the classpool can find the dependencies needed when it needs to recompile the class
                classpool.appendClassPath(loaderClassPath);
                // This class has not yet actually been loaded by any classloader, so we must add the class directly so it can be found by the classpool.
                classpool.insertClassPath(byteArrayClassPath);
                final CtClass editableClass = classpool.get(classNameDotted);
                List<CtBehavior> allBehaviors = collectAllBehaviors(editableClass);

                boolean modifiedMethods = false;
                for (final CtBehavior editableMethod : allBehaviors) {
                    String methodName = resolveMethodName(editableMethod);
                    List<String> typeNames = Arrays.stream(editableMethod.getParameterTypes()).map(clazz -> clazz.getName()).collect(Collectors.toList());
                	if (classMethodSelector.shouldTransformMethod(classNameDotted, methodName, typeNames)) {
                        // TODO should allow for multiple matching definitions being processed
                        // TODO we might be broken handling primitives.  Need to handle or filter out for now
                        methodRewriter.editMethod(editableMethod, methodName, classMethodSelector.findMatchingDefinition(classNameDotted, methodName, typeNames));
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
                    redefinedClasses.add(classNameDotted);
                    TraceLogger.log("modified class - " + classNameDotted);
                } else {
                    // If this class was previously transformed we can now remove it
                    // from our list as it is not transformed using the current rules
                    if (redefinedClasses.remove(classNameDotted))
                        TraceLogger.log("restored class - " + classNameDotted);
                }
            } catch (Throwable ex) {
            	System.err.println("Unable to transform: " + classNameDotted);
                ex.printStackTrace();
            }
        }
 
        return byteCode;
    }

    /**
     * Get the collection of all methods and constructors defined on class
     * @param editableClass
     * @return
     */
    private List<CtBehavior> collectAllBehaviors(final CtClass editableClass) {
        final CtBehavior declaredMethods[] = editableClass.getDeclaredMethods();
        final CtBehavior declaredConstructors[] = editableClass.getDeclaredConstructors();
        List<CtBehavior> allBehaviors = new ArrayList<>();
        for (CtBehavior behavior : declaredConstructors) {
            allBehaviors.add(behavior);
        }
        for (CtBehavior behavior : declaredMethods) {
            allBehaviors.add(behavior);
        }
        return allBehaviors;
    }

    public void setClassMethodSelector(ClassMethodSelector classMethodSelector) {
    	this.classMethodSelector = classMethodSelector;
    }

    public void setMethodRewriter(MethodRewriter methodRewriter) {
    	this.methodRewriter = methodRewriter;
    }

    public void retransform() {
    	final Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
    	for (final Class<?> clazz : loadedClasses) {
            attemptTransform(clazz);
    	}
    }

    private void attemptTransform(final Class<?> clazz) {
        final String classNameDotted = clazz.getName().replaceAll("/", ".");
        if (shouldTransformClass(classNameDotted)) {
        	try {
        		TraceLogger.log("re-evaluating rule definitions for class - " + clazz);
        		instrumentation.retransformClasses(clazz);

        	} catch (UnmodifiableClassException e) {
        		e.printStackTrace();
        	}
        }
    }

    private boolean shouldTransformClass(String classNameDotted) {
        return (classMethodSelector.shouldTransformClass(classNameDotted) || redefinedClasses.contains(classNameDotted));
    }

    /**
     * remove $ in the name of inner class methods to make tracer config simple.
     * @param behavior
     * @return
     */
    private String resolveMethodName(CtBehavior behavior) {
        String rawName = behavior.getName();
        String[] parts = rawName.split("\\$");
        if (parts.length == 1) return parts[0];
        return parts[1];
    }
}
