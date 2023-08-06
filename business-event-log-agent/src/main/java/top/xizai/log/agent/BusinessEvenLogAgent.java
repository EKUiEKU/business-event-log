package top.xizai.log.agent;

import lombok.extern.slf4j.Slf4j;
import top.xizai.log.agent.ams.AsmClassObserverContext;
import top.xizai.log.agent.monitor.CacheableMonitorContext;
import top.xizai.log.agent.monitor.MonitorContext;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.logging.Logger;

/**
 * @Classname BusinessEvenLogAgent
 * @Date 2023/8/6 18:44
 * @Created 吴少聪
 * @Description 分布式下的日志探针, 业务无论是在哪个机器节点下，只要是同一个业务链路，则日志信息都能记录到
 *              此外在技术层面会对此业务链下的所有的SQL日志进行记录
 */
@Slf4j
public class BusinessEvenLogAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        MonitorContext ctx = new CacheableMonitorContext().start(agentArgs, inst);
        log.info("业务日志探针启动成功...");
    }
}
