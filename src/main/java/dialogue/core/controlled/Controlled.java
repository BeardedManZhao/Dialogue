package dialogue.core.controlled;

import java.net.InetAddress;

/**
 * 被控端服务接口，代表被控端的身份，这里主要用于标记身份
 * <p>
 * The service interface of the controlled end represents the identity of the controlled end, which is mainly used to mark the identity
 */
public interface Controlled {

    /**
     * 获取到连接至该被控会话的主控信息
     * <p>
     * Obtain the master control information connected to the controlled session
     *
     * @return 该函数在会话运行时可以获取到该会话建立连接的主控信息，如果返回null，则代表当前被控会话没有运行或没有获取到连接。
     * <p>
     * This function can obtain the master control information of the session when the session is running. If null is returned, it means that the current controlled session is not running or the connection is not obtained.
     */
    InetAddress ConnectedMaster();

    /**
     * 终止会话中的所有服务，彻底停止该会话的一切行为，同时将该会话在端口中的服务也终止
     * <p>
     * Terminate all services in the session, completely stop all behaviors of the session, and also terminate the services in the port of the session
     */
    void shutDown();
}
