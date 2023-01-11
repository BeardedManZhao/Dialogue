package dialogue.core.controlled;

import dialogue.ConfigureConstantArea;
import dialogue.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * 命令行会话对象，是被控设备的命令执行对象，该类的实现会将命令执行，并返回执行结果。
 * <p>
 * The command line conversation object is the command execution object of the controlled device. The implementation of this class will execute the command and return the execution result.
 * <p>
 * 该命令行只能执行查看类的命令，例如 cat type 或 python xxx等，不能执行 cd等命令以及不正确的命令
 *
 * @author 赵凌宇
 */
public class ConsoleSession extends ControlledSession {

    protected ConsoleSession(int port) {
        super(port);
    }

    public static ControlledSession getInstance() {
        return getInstance(CONTROLLED_CONSOLE_SESSION);
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
        ConfigureConstantArea.LOGGER.log(Level.INFO, "run -> " + command);
        try {
            final Process exec = runtime.exec(command);
            InputStream inputStream1 = exec.getInputStream();
            String stringByStream = IOUtils.getStringByStream(inputStream1);
            inputStream1.close();
            if (stringByStream.length() != 0) {
                return stringByStream;
            } else {
                InputStream errorStream = exec.getErrorStream();
                stringByStream = IOUtils.getStringByStream(errorStream);
                errorStream.close();
                if (stringByStream.length() != 0) {
                    return stringByStream;
                } else {
                    return "Command executed, but no data returned.";
                }
            }
        } catch (RuntimeException | IOException e) {
            return "ERROR => " + e;
        }
    }

    /**
     * 将当前会话克隆一个出来，使得一种会话可以提供给多个网络连接使用，需要注意的是，克隆出来的会话将不会被管理者所管理。
     * <p>
     * Clone the current session to make one session available to multiple network connections. Note that the cloned session will not be managed by the manager.
     *
     * @param port 该被控会话所使用的新端口，当原会话不能够满足主控连接数量时，您可以在此处手动开启一个新的被控端口。
     *             <p>
     *             The new port used by the controlled session. When the original session cannot meet the number of master connections, you can manually open a new controlled port here.
     * @return 一个与当前会话功能一致的新会话对象，不会与原会话有任何的关系
     * <p>
     * A new session object with the same function as the current session will not have any relationship with the original session
     */
    @Override
    public ControlledSession cloneSession(int port) {
        return new ConsoleSession(port);
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    @Override
    public short getSessionNum() {
        return CONTROLLED_CONSOLE_SESSION;
    }
}
