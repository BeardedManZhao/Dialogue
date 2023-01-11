package dialogue.core.master;

import dialogue.core.result.StringResult;

import java.net.InetAddress;

/**
 * 主控端服务接口，代表被控端的身份，这里主要用于标记身份
 * <p>
 * The service interface of the main control terminal represents the identity of the controlled terminal, which is mainly used to mark the identity
 */
public interface Master {
    /**
     * 运行一个命令，并返回运行结果。
     * <p>
     * Run a command and return the running result.
     *
     * @param command 需要在主机上运行的命令
     *                <p>
     *                Commands that need to be run on the host
     * @return 运行命令之后的结果数据，该函数与普通的runCommand的最大区别在于该函数不会轻易的抛出异常信息，更多的是将异常信息记录再结果对象中
     * <p>
     * The result data after running the command. The biggest difference between this function and the common runCommand is that this function does not easily throw exception information, and more importantly, it records the exception information in the result object
     */
    StringResult runCommandGetResult(String command);

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
