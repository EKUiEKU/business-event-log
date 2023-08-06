package top.xizai.log.agent.monitor;

import lombok.Data;

/**
 * @Classname MonitorProperites
 * @Date 2023/8/6 19:10
 * @Created 吴少聪
 * @Description 探针配置信息
 */
@Data
public class MonitorProperties {
    // 服务器地址信息
    private String serverAddress;
    // 服务命名空间
    private String namespace;


    /**
     * 加载探针的配置信息
     * @param agentArgs
     * @return
     */
    public static MonitorProperties load(String agentArgs) {
        return null;
    }
}
