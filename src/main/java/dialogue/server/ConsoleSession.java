package dialogue.server;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
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
 * @author zhao
 */
public class ConsoleSession extends ControlledSession {

    ConsoleSession() {
    }

    public static ControlledSession getInstance() {
        return getInstance(Session.CONTROLLED_CONSOLE_SESSION);
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
            InputStream inputStream1 = runtime.exec(command).getInputStream();
            String stringByStream = IOUtils.getStringByStream(inputStream1);
            inputStream1.close();
            if (stringByStream.length() != 0) {
                return stringByStream;
            } else {
                return "Command executed, but no data returned.";
            }
        } catch (RuntimeException | IOException e) {
            return "ERROR => " + e;
        }
    }
}
