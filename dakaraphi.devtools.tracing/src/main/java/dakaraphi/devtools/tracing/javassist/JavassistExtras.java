package dakaraphi.devtools.tracing.javassist;

import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;

public class JavassistExtras {

    /**
     * use to effectively remove or comment out a line of code.
     * useful for when we want to replace a line of code with something different.
     * 
     * TODO - note not yet tested
     * @param method
     * @param lineNumberToReplace
     */
    public static void removeLineOfCode(CtMethod method, int lineNumberToReplace) {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);

        // find bytecode offset
        int beginOffset = lineNumberAttribute.toStartPc(lineNumberToReplace);
        int endOffset   = lineNumberAttribute.toStartPc(lineNumberToReplace+1);

        byte[] methodBytecode = codeAttribute.getCode();
        for (int bytecodeIndex = beginOffset; bytecodeIndex < endOffset; bytecodeIndex++) {
            // replace all bytecodes for this line with no-op instructions which do nothing.
            methodBytecode[bytecodeIndex] = CodeAttribute.NOP;
        }
    }
}
