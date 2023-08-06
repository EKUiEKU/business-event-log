package top.xizai.log.agent.monitor;

import com.google.common.eventbus.Subscribe;
import top.xizai.log.agent.monitor.config.BusinessConfig;
import top.xizai.log.agent.monitor.log.LogInfo;

import java.util.List;

/**
 * @Classname CacheableMonitorContext
 * @Date 2023/8/6 19:27
 * @Created 吴少聪
 * @Description 可缓存的日志监控上下文
 * 当监控服务器挂掉了或者本地引用挂掉之后可以将日志信息暂存本地磁盘
 */
public class CacheableMonitorContext extends MonitorContext {


    /**
     * 放置缓存
     * @param logInfo
     */
    public void putCache(LogInfo logInfo) {

    }

    /**
     * 获取业务Id下的所有的日志信息
     * @param businessId
     * @return
     */
    public List<LogInfo> getCache(String businessId) {
        return null;
    }

    /**
     * 根据业务Id清理缓存
     * @param businessId
     * @return
     */
    public Integer clearCache(String businessId) {
        return null;
    }


    @Override
    protected List<BusinessConfig> getBusinessConfigs(MonitorProperties monitorProperties) {
        return null;
    }


    /**
     * 批量上传日志
     * @param businessId
     * @param logInfos
     */
    public void uploadLogInfos(String businessId, List<LogInfo> logInfos) {

    }


    /**
     * 日志消费端
     * @param logInfo
     */
    @Subscribe
    public void consumeLogInfo(LogInfo logInfo) {

    }
}
