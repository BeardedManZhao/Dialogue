package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 主控look执行器，用于发送指令与接收被控数据。
 * <p>
 * The main look actuator is used to send instructions and receive controlled data.
 *
 * @author zhao
 */
public class MasterLookFileActuator implements Actuator {

    protected final ServerSocket fileSocket;
    protected final OutputStream outputStream;

    public MasterLookFileActuator(ServerSocket fileSocket, OutputStream outputStream) {
        this.fileSocket = fileSocket;
        this.outputStream = outputStream;
    }

    /**
     * @return 能够调用该执行器的会话对象编号。
     * <p>
     * The number of the session object that can call the executor.
     */
    @Override
    public int getType() {
        return Session.MASTER_FILE_SESSION;
    }

    @Override
    public String getName() {
        return "look";
    }

    /**
     * 返回会话当前运行状态，当一个会话没有在运行的时候，该函数将返回false，一个没有运行中的会话将不具备执行命令与回显数据的能力
     * <p>
     * Returns the current running state of the session. When a session is not running, this function will return false. A session that is not running will not have the ability to execute commands and echo data
     *
     * @return 如果返回true，代表当前会话正在运行中
     * <p>
     * If true is returned, the current session is running
     */
    @Override
    public boolean isRunning() {
        return ActuatorManager.containMasterActuator(this.getName());
    }

    /**
     * @param command 需要执行的命令参数
     *                <p>
     *                Command parameters to be executed
     * @param matcher 命令匹配器，通过该匹配器获取到命令中的所需参数
     *                <p>
     *                Command matcher, through which the required parameters in the command can be obtained
     * @return 运行之后的结果的字符串形式
     * <p>
     * String form of the result after running
     */
    @Override
    public String runActuatorCommand(String command, Matcher matcher) throws IOException {
        outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
        outputStream.flush();
        ConfigureConstantArea.LOGGER.info("reading....");
        // look 目标文件
        Socket accept = fileSocket.accept();
        InputStream inputStream = accept.getInputStream();
        String stringByStream = IOUtils.getStringByStream(inputStream);
        inputStream.close();
        accept.close();
        return stringByStream;
    }
}
