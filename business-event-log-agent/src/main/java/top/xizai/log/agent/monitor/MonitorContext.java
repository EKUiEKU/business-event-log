package top.xizai.log.agent.monitor;

import top.xizai.log.agent.ams.AsmClassObserverContext;
import top.xizai.log.agent.monitor.config.BusinessConfig;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @Classname MonitorContext
 * @Date 2023/8/6 18:32
 * @Created 吴少聪
 * @Description 监控上下文
 */
public abstract class MonitorContext {
    /**
     * 探针信息
     */
    private MonitorProperties monitorProperties;
    /**
     * 字节码增强器
     */
    private AsmClassObserverContext asmClassObserverContext;

    /**
     * 对监控探针进行启动
     *  1.首先args配置信息去验证服务器是否健康
     *  2.获取当前节点下的所有的业务配置信息
     *  3.根据匹配的规则去增强相应类中的字节码
     *
     * @param agentArgs
     * @param inst
     * @return
     */
    public  MonitorContext start(String agentArgs, Instrumentation inst) {
        monitorProperties = MonitorProperties.load(agentArgs);
        List<BusinessConfig> businessConfigList = getBusinessConfigs(monitorProperties);
        asmClassObserverContext = new AsmClassObserverContext(inst, businessConfigList);
        return this;
    }

    /**
     * 获取当前命名空间下的所有的业务配置信息
     * @param monitorProperties 配置信息
     * @return
     */
    protected abstract List<BusinessConfig> getBusinessConfigs(MonitorProperties monitorProperties);

    public void setMonitorProperties(MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
    }

    public void setAsmClassObserverContext(AsmClassObserverContext asmClassObserverContext) {
        this.asmClassObserverContext = asmClassObserverContext;
    }
}
