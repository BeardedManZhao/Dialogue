package dialogue.core.master;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.exception.SessionRunException;
import dialogue.core.result.StringResult;

import java.io.IOException;

/**
 * 主控设备的TCP命令执行实现类，该类能够通过TCP将命令传递给远程设备执行。
 * <p>
 * TCP command execution implementation class of the master control device, which can transmit commands to remote devices for execution through TCP.
 *
 * @author 赵凌宇
 */
public class TCPSession extends MasterSession {

    TCPSession() {
    }

    public static TCPSession getInstance() {
        return (TCPSession) getInstance(MASTER_TCP_SESSION);
    }

    /**
     * 运行一个命令，并返回运行结果。
     * <p>
     * Run a command and return the running result.
     *
     * @param command 需要在主机上运行的命令
     *                <p>
     *                Commands that need to be run on the host
     * @return 运行命令之后的日志数据
     * <p>
     * Log data after running the command
     */
    @Override
    public String runCommand(String command) {
        try {
            outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
            outputStream.flush();
            // 获取被控设备的执行结果
            byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            int read = inputStream.read(buffer);
            if (read > 0) {
                return new String(buffer, 0, read, ConfigureConstantArea.CHARSET);
            } else {
                return "Connection is unstable!!!";
            }
        } catch (IOException e) {
            throw new SessionRunException(e);
        } catch (NullPointerException e) {
            throw SESSION_NOT_STARTED;
        }
    }

    /**
     * 将当前会话克隆一个出来，使得一种会话可以提供给多个网络连接使用，需要注意的是，克隆出来的会话将不会被管理者所管理。
     * <p>
     * Clone the current session to make one session available to multiple network connections. Note that the cloned session will not be managed by the manager.
     *
     * @return 一个与当前会话功能一致的新会话对象，不会与原会话有任何的关系
     * <p>
     * A new session object with the same function as the current session will not have any relationship with the original session
     */
    @Override
    public MasterSession cloneSession() {
        return new TCPSession();
    }

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
    @Override
    public StringResult runCommandGetResult(String command) {
        try {
            return new StringResult(true, Session.MASTER_TCP_SESSION, runCommand(command));
        } catch (Exception e) {
            return new StringResult(false, Session.MASTER_TCP_SESSION, e.toString());
        }
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    @Override
    public short getSessionNum() {
        return MASTER_TCP_SESSION;
    }
}
