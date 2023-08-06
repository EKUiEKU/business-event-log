package top.xizai.log.agent.ams;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import top.xizai.log.agent.utils.PrintUtils;

import java.lang.instrument.Instrumentation;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE:
 **/
public class AsmClassFileTransformerProcessor {
    private Instrumentation inst;
    private AsmClassObserverContext ctx;

    public AsmClassFileTransformerProcessor(AsmClassObserverContext ctx) {
        this.ctx = ctx;
    }

    private class BusinessLogClassProxy extends ClassVisitor {
        public BusinessLogClassProxy(ClassVisitor cv) {
            super(ASM9, cv);
        }

        private String className;

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                         String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (methodVisitor != null) {
                // 排除抽象方法和本地方法
                boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
                boolean isNativeMethod = (access & ACC_NATIVE) != 0;
                if (!isAbstractMethod && !isNativeMethod) {
                    // 排除构造方法
                    if (!"<init>".equals(name) && !"main".equals(name)) {
                        methodVisitor = new LocalVariablesSorter(access, descriptor, methodVisitor);
                        return new BusinessLogMethodAdapter(methodVisitor, access, name, descriptor, className);
                    }
                }
            }
            return methodVisitor;
        }

    }


    private class BusinessLogMethodAdapter extends AdviceAdapter {
        public BusinessLogMethodAdapter(MethodVisitor mv, int access, String name, String descriptor, String className) {
            super(ASM9, mv, access, name, descriptor);
            str = "类名->" + className + str;
        }

        String str = "，方法名->" + super.getName() + "，方法描述符->" + super.methodDesc;
        // 开始时间在局部变量表中的位置
        int start = 0;


        int localIndex;
        Label startLabel = new Label();
        Label endLabel = new Label();
        Label handlerLabel = new Label();


        @Override
        protected void onMethodEnter() {
            // 记录开始时间
            super.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            // 在局部变量表中nextLocal位置存放long类型的数值，nextLocal表示当前已存数据的下一个位置的索引
            // 下面执行完visitVarInsn(LSTORE, start)后，nextLocal会根据存入的数据类型长度，后移一位或两位
            start = nextLocal;
            // 将栈顶的数值放入局部变量表中start位置
            super.visitVarInsn(LSTORE, start);
            // 进入方法时，先打印一句话 Enter: xxx
            printText("Enter: " + str);
            // 取出方法所有的入参类型
            Type[] argumentTypes = getArgumentTypes();
            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                // 将方法的入参从局部变量表中取出，压入到操作数栈中
                loadArg(i);
                // 对操作数栈顶的数据按照argumentType类型进行包装，并用包装好的值替换原来栈顶的这个数值，而且数据类型也是一致的
                box(argumentType);
                // 打印操作数栈顶的这个值，就实现了对方法入参的循环打印
                printObject("入参类型：");
            }

            // 标记try开始位置
            super.visitLabel(startLabel);
            super.onMethodEnter();
        }

        @Override
        protected void onMethodExit(int opcode) {
            // 退出方法时，打印一句话 Exit: xxx
            printText("Exit: " + str);
            // throw 与 return 指令没有返回值，这里手动将希望打印到控制台的字符串压入到操作数栈顶
            if (opcode == ATHROW) {
                super.visitLdcInsn("有异常抛出了");
            } else if (opcode == RETURN) {
                super.visitLdcInsn("void方法，没有返回值");
            } else if (opcode == ARETURN) {
                // 复制操作数栈顶的1个数值，并将复制结果压入操作数栈顶，此时操作数栈上有2个连续相同的数值
                // 复制的目的是，多出来的这个数值用来打印到控制台，原来栈顶的数值不受影响
                dup();
            } else if (opcode == LRETURN || opcode == DRETURN) {
                // 因为double和long类型（64bit）占2个slot，所以要复制操作数栈顶的2个数值，并将其压入操作数栈顶
                dup2();
                // 对栈顶的数据按照返回值类型进行包装，并用包装好的值替换原来栈顶的这个数值
                // double类型会用Double.valueOf()进行包装，long类型会用Long.valueOf()进行包装
                box(getReturnType());
            } else {
                dup();
                // 这里排除上面几种返回值类型，这里的opcode应该是 FRETURN 和 IRETURN
                // 对相应类型的数据进行Float.valueOf()或者Integer.valueOf()包装
                box(getReturnType());
            }
            // 因为这里打印时，需要参数是Object类型，所以上面的2个box(getReturnType())必须有，目的是将基本数据类型转成包装类
            // 否则打印时，传的是基本数据类型，不是Object一定会报错
            // 前面2个if没有返回值，所以不需要按照返回值数据类型进行包装，直接传入String类型数据给printObject方法进行打印
            // 第3个if是Object类型返回值，复制一份压到栈顶即可，不需要再包装了
            printObject("返回值类型：");
            // 打印耗时
            printSpendTime();

            super.onMethodExit(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // 标记try结束位置
            super.visitLabel(endLabel);
            // 添加catch内处理逻辑
            super.visitLabel(handlerLabel);

            // 拿到局部变量表中空闲位置的索引，用于后面添加数据到局部变量表中
            localIndex = nextLocal;
            // 将异常信息exception添加到局部变量表localIndex位置
            super.visitVarInsn(ASTORE, localIndex);
            printException();

            // 访问try catch块

            // 将局部变量表中localIndex位置的数据再次压入操作数栈顶，用于抛出
            super.visitVarInsn(ALOAD, localIndex);
            // 将操作数栈顶的数据抛出
            super.visitInsn(ATHROW);

            super.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception");


            // 调用父类方法实现
            super.visitMaxs(maxStack, maxLocals);
        }

        private void printException() {
            // 方法名压入栈顶
            super.visitLdcInsn(getName());
            // 将局部变量表中localIndex位置的数据压入操作数栈顶
            super.visitVarInsn(ALOAD, localIndex);
            // 调用打印类
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(PrintUtils.class), "printException",
                    "(Ljava/lang/String;Ljava/lang/Exception;)V", false);
        }

        private void printText(String str) {
            // 将str从常量池中取出，压入操作数栈顶
            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitLdcInsn(str);
            // 从操作数栈顶取出一个数据，作为入参调用PrintUtils的public static void printText(String str)方法
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }

        private void printObject(String name) {
            // 将name压入栈顶
            super.visitLdcInsn(name);
            // printObject方法入参是name和value，从栈顶取参数时，从后往前输入
            // 所以要先拿到Object类型的value再拿String类型的name，但此时栈顶是name，name下面是value的包装类
            // 所以要调用swap方法，将栈顶最顶端的两个数值互换(数值不能是long或double类型)
            swap();
            // 从操作数栈顶取出一个数据，作为入参调用PrintUtils的public static void printObject(String name, Object value)方法
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(PrintUtils.class), "printObject",
                    "(Ljava/lang/String;Ljava/lang/Object;)V", false);
        }

        private void printSpendTime() {
            // 方法名压入栈顶
            super.visitLdcInsn(super.getName());
            // 将开始时间从局部变量表start位置压入栈顶
            super.visitVarInsn(LLOAD, start);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(PrintUtils.class), "printSpendTime",
                    "(Ljava/lang/String;J)V", false);
        }


    }


    public byte[] changeMethodByClassBufferMethodVal(byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, COMPUTE_FRAMES);
        BusinessLogClassProxy proxy = new BusinessLogClassProxy(classWriter);

        classReader.accept(proxy, EXPAND_FRAMES);

        byte[] bytes = classWriter.toByteArray();

        return bytes;
    }
}
