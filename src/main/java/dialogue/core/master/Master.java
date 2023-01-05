package dialogue.core.master;

import java.net.InetAddress;

/**
 * 主控端服务接口，代表被控端的身份，这里主要用于标记身份
 * <p>
 * The service interface of the main control terminal represents the identity of the controlled terminal, which is mainly used to mark the identity
 */
public interface Master {
    /**
     * 获取到与当前主控会话互相连接的被控信息
     * <p>
     * Acquire the controlled information connected to the current master session
     *
     * @return 当主控会话正在运行的时候，获取到该主控会话连接的被控会话网络信息
     * <p>
     * When the master session is running, obtain the network information of the controlled session connected to the master session
     */
    InetAddress ConnectedControlled();
}
