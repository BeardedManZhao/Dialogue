package dialogue.client;

import dialogue.ConfigureConstantArea;
import dialogue.DialogueManager;

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
        return getInstance(DialogueManager.MASTER_TCP_SESSION);
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
            e.printStackTrace();
            return "null";
        }
    }
}
