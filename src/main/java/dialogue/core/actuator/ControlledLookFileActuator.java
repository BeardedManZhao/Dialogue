package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * Look命令执行器，与主控Look相互对应，用于将数据传递给主控的查看文件操作。
 *
 * @author zhao
 */
public class ControlledLookFileActuator implements Actuator {

    protected final Socket accept;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;

    public ControlledLookFileActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
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
    public String runActuatorCommand(String command, Matcher matcher) throws IOException {
        // look 目标文件，首先要获取到目标文件的数据，并将数据返回
        Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
        if (matcher.find()) {
            String filePath = matcher.group(1);
            // 这个方式不需要状态
            OutputStream outputStream1 = fileSocket.getOutputStream();
            try {
                IOUtils.copy(new BufferedInputStream(new FileInputStream(filePath)), outputStream1, true);
            } catch (IOException e) {
                String s = e.toString();
                ConfigureConstantArea.LOGGER.warning(s);
                outputStream1.write(s.getBytes(ConfigureConstantArea.CHARSET));
            }
            IOUtils.close(fileSocket);
        } else {
            OutputStream outputStream = fileSocket.getOutputStream();
            outputStream.write(("ERROR COMMAND " + command + "\nExample: look [filePath]").getBytes(ConfigureConstantArea.CHARSET));
            IOUtils.close(outputStream);
        }
        return ControlledSession.SEND_TEXT;
    }
}
