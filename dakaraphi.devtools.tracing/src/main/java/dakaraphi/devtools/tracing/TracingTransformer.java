package dakaraphi.devtools.tracing;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class TracingTransformer implements ClassFileTransformer {
	private static Instrumentation instrumentation;
	private final ClassMethodSelector classMethodSelector;
	private final MethodRewriter methodRewriter;
	public TracingTransformer(final Instrumentation instrumentation, ClassMethodSelector classMethodSelector, MethodRewriter methodRewriter) {
		this.classMethodSelector = classMethodSelector;
		this.methodRewriter = methodRewriter;
		instrumentation.addTransformer(this, true);
		TracingTransformer.instrumentation = instrumentation;
		System.out.println("TracingTransformer active");
		System.out.println("TracingTransformer classloader: " + TracingAgent.class.getClassLoader());
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
                for (final CtMethod editableMethod : declaredMethods) {
                	if (classMethodSelector.shouldTransform(classNameDotted, editableMethod.getName())) {
                		methodRewriter.editMethod(editableMethod, classMethodSelector.findMatchingDefinition(classNameDotted, editableMethod.getName()));
                	}
                }
                
                byteCode = editableClass.toBytecode();
                editableClass.detach();
                
                // These appear to only be needed during rewriting
                // If we don't remove, the list just keeps growing as we rewrite more classes
                // or transform the same class again
                classpool.removeClassPath(loaderClassPath);
                classpool.removeClassPath(byteArrayClassPath);
                
                System.out.println("Transformed " + classNameDotted);
            } catch (Throwable ex) {
            	System.err.println("Unable to transform: " + classNameDotted);
                ex.printStackTrace();
            }
        }
 
        return byteCode;
    }
    


    public static void retransform(final String regex) {
    	final Class[] loadedClasses = instrumentation.getAllLoadedClasses();
    	for (final Class clazz : loadedClasses) {
    		if (clazz.getName().matches(regex)) {
    			try {
    				System.out.println("retransform " + clazz);
					instrumentation.retransformClasses(clazz);

				} catch (UnmodifiableClassException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
}
