package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * get 命令执行类，与主控的get执行类相互对应，用于主控从被控设备中获取数据。
 * <p>
 * The get command execution class corresponds to the get execution class of the master control, which is used for the master control to obtain data from the controlled device.
 *
 * @author zhao
 */
public class ControlledGetFileActuator implements Actuator {

    protected final Socket accept;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;

    public ControlledGetFileActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
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
        return "get";
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
        // get 目标文件 一样要先获取目标文件的数据
        if (matcher.find()) {
            Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
            DataOutputStream outputStream1 = new DataOutputStream(fileSocket.getOutputStream());
            ConfigureConstantArea.LOGGER.info(OK_1);
            try {
                FileInputStream fileInputStream = new FileInputStream(matcher.group(1));
                // 将本次的发送信息状态提供给主控
                outputStream1.writeInt(fileInputStream.available());
                ConfigureConstantArea.LOGGER.info(OK_2);
                // 将数据发送给主控
                IOUtils.copy(fileInputStream, outputStream1, true);
                // 返回成功
                return ControlledSession.SEND_FILE_BYTE;
            } catch (IOException e) {
                // 将错误提供给主控
                outputStream1.writeInt(-1);
                outputStream1.flush();
                String s = e.toString();
                ConfigureConstantArea.LOGGER.warning(s);
                outputStream1.writeUTF(s);
                outputStream1.flush();
                outputStream1.close();
                // 返回错误
                return ControlledSession.SEND_FILE_ERROR;
            } finally {
                IOUtils.close(fileSocket);
            }
        } else {
            return "ERROR COMMAND " + command + "\nExample: get [filePath] [filePath]";
        }
    }

//    /**
//     * @param command 需要执行的命令参数
//     *                <p>
//     *                Command parameters to be executed
//     * @param matcher 命令匹配器，通过该匹配器获取到命令中的所需参数
//     *                <p>
//     *                Command matcher, through which the required parameters in the command can be obtained
//     * @return 运行之后的结果的字符串形式
//     * <p>
//     * String form of the result after running
//     */
//    @Override
//    public String runActuatorCommand(String command, Matcher matcher) throws IOException {
//        // get 目标文件 一样要先获取目标文件的数据
//        if (matcher.find()) {
//            Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
//            DataOutputStream outputStream1 = new DataOutputStream(fileSocket.getOutputStream());
//            try {
//                FileInputStream fileInputStream = new FileInputStream(matcher.group(1));
//                // 将本次的发送信息状态提供给主控
//                outputStream1.writeUTF(ControlledSession.SEND_FILE_BYTE);
//                // 将数据发送给主控
//                IOUtils.copy(fileInputStream, outputStream1, true);
//                // 返回成功
//                return ControlledSession.SEND_FILE_BYTE;
//            } catch (IOException e) {
//                // 将错误提供给主控
//                outputStream1.writeUTF(ControlledSession.SEND_FILE_ERROR);
//                outputStream1.flush();
//                outputStream1.writeUTF(e.toString());
//                outputStream1.flush();
//                outputStream1.close();
//                // 返回错误
//                return ControlledSession.SEND_FILE_ERROR;
//            } finally {
//                IOUtils.close(fileSocket);
//            }
//        } else {
//            return "ERROR COMMAND " + command + "\nExample: get [filePath] [filePath]";
//        }
//    }
}
