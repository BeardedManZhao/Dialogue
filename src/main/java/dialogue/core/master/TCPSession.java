package dialogue.core.master;

import dialogue.ConfigureConstantArea;
import dialogue.core.exception.SessionRunException;

import java.io.IOException;

/**
 * 主控设备的TCP命令执行实现类，该类能够通过TCP将命令传递给远程设备执行。
 * <p>
 * TCP command execution implementation class of the master control device, which can transmit commands to remote devices for execution through TCP.
 *
 * @author zhao
 */
public class TCPSession extends MasterSession {

    TCPSession() {
    }

    public static MasterSession getInstance() {
        return getInstance(MASTER_TCP_SESSION);
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
}
