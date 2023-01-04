package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 被控端的文件接受put命令执行器，在该执行器中，有着执行命令接受数据的功能。
 *
 * @author zhao
 */
public class ControlledPutFileActuator implements Actuator {

    protected final Socket accept;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;

    public ControlledPutFileActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        this.accept = accept;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * @return 能够调用该执行器的会话对象编号。
     * <p>
     * The number of the session object that can call the executor.
     */
    @Override
    public int getType() {
        return Session.CONTROLLED_FILE_SESSION;
    }

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "put";
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
        return ActuatorManager.containControlledActuator(this.getName());
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
    public String runActuatorCommand(String command, Matcher matcher) {
        // 获取到文件新路径
        if (matcher.find() && matcher.find()) {
            File file = new File(matcher.group(1));
            // 创建一个Socket，向主控发送接受请求
            try {
                Socket socket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                ConfigureConstantArea.LOGGER.info(OK_1);
                dataOutputStream.writeUTF(OK_1);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                try {
                    // 打开文件数据流
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                    // 开始发送准备好的标识
                    ConfigureConstantArea.LOGGER.info(OK_2);
                    dataOutputStream.writeUTF(OK_2);
                    // 开始接受数据主体
                    IOUtils.copy(dataInputStream, bufferedOutputStream, false);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    return ControlledSession.SEND_FILE_BYTE;
                } catch (IOException e) {
                    String s = e.toString();
                    // 出现了获取文件数据错误，发送错误数据
                    dataOutputStream.writeUTF(s);
                    ConfigureConstantArea.LOGGER.warning(s);
                    return ControlledSession.SEND_FILE_ERROR;
                } finally {
                    IOUtils.close(dataInputStream);
                    IOUtils.close(dataOutputStream);
                    IOUtils.close(socket);
                }
            } catch (IOException e) {
                // 发生了连接错误
                return e.toString();
            }
        } else {
            return "ERROR COMMAND " + command + "\nExample: put [filePath] [filePath]";
        }
    }
}
