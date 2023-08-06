package top.xizai.log.agent.ams;


import cn.hutool.core.io.IoUtil;
import top.xizai.log.agent.monitor.config.BusinessConfig;

import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: WSC
 * @DATE: 2022/8/20
 * @DESCRIBE: ASM编码的可缓存的热部署上下文
 **/
public class AsmClassObserverContext implements ClassFileTransformer {
    private Instrumentation instrumentation;

    private Set<String> enhance = new HashSet<>();


    public AsmClassObserverContext(Instrumentation instrumentation, List<BusinessConfig> businessConfigList) {
        this.instrumentation = instrumentation;
        this.instrumentation.addTransformer(this, true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String clsName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (clsName == null || classfileBuffer == null) {
            return new byte[0];
        }

        if (clsName == null) {
            return null;
        }
        if (clsName.startsWith("java")) {
            return null;
        }
        if (clsName.startsWith("javax")) {
            return null;
        }
        if (clsName.startsWith("jdk")) {
            return null;
        }
        if (clsName.startsWith("sun")) {
            return null;
        }
        if (clsName.startsWith("org")) {
            return null;
        }

        if (clsName.startsWith("top/xizai") && !enhance.contains(clsName)) {
            try {
                AsmClassFileTransformerProcessor processor = new AsmClassFileTransformerProcessor(this);
                byte[] bytes = processor.changeMethodByClassBufferMethodVal(classfileBuffer);
                String[] split = clsName.split("/");
                // 调试输出到本地
                IoUtil.write(new FileOutputStream("c:\\DevEnv\\" + split[split.length - 1] + ".class"), true, bytes);
                enhance.add(clsName);
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

}
