package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * @author 赵凌宇
 * 2023/4/3 19:30
 */
public class ControlledPutsNCActuator extends ControlledPutFileActuator {

    public ControlledPutsNCActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        super(accept, inputStream, outputStream);
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
        return "puts-nc";
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
        // 向对方发送文件接收请求
        try {
            Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
            if (matcher.find() && matcher.find()) {
                // 获取到本地文件目录
                String dirPath = matcher.group(1);
                File file = new File(dirPath);
                if (file.exists() && file.isDirectory()) {
                    ConfigureConstantArea.LOGGER.info(OK_1);
                    DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(fileSocket.getInputStream());
                    try {
                        while (true) {
                            dataOutputStream.writeUTF(OK_2);
                            // 开始接收文件名称&文件大小
                            String fileName = dataInputStream.readUTF();
                            if (fileName.equals(OK_2)) {
                                // 代表文件发送结束了
                                break;
                            } else {
                                File file1 = new File(dirPath + '/' + fileName);
                                if (file1.exists()) {
                                    // 如果文件已存在就回复 AE_1;
                                    dataOutputStream.writeUTF(AE_1);
                                    continue;
                                } else {
                                    // 如果需要接受就回复 ok_file2
                                    dataOutputStream.writeUTF(OK_2);
                                    ConfigureConstantArea.LOGGER.info(OK_2);
                                }
                                long fileSize = dataInputStream.readLong();
                                // 代表有下一个文件，开始创建文件数据流
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file1));
                                ConfigureConstantArea.LOGGER.info(fileName);
                                // 开始发送数据
                                IOUtils.copy(fileSize, dataInputStream, bufferedOutputStream, null);
                                // 关闭当前文件数据流
                                bufferedOutputStream.flush();
                                bufferedOutputStream.close();
                            }
                        }
                        // 文件传输完毕
                        return ControlledSession.SEND_FILE_BYTE;
                    } catch (IOException e) {
                        // 发生文件传输错误
                        dataOutputStream.writeUTF(e.toString());
                        return ControlledSession.SEND_FILE_ERROR;
                    } finally {
                        IOUtils.close(dataInputStream);
                        IOUtils.close(dataOutputStream);
                        IOUtils.close(fileSocket);
                    }
                } else {
                    // 目标路径不是一个目录，在这里将错误信息返回出去
                    DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
                    String error = dirPath + "  is not a directory or non-existent";
                    dataOutputStream.writeUTF(error);
                    IOUtils.close(dataOutputStream);
                    IOUtils.close(fileSocket);
                    return ControlledSession.SEND_FILE_ERROR;
                }
            } else {
                // 语法错误
                DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
                String error = "ERROR COMMAND " + command + "\nExample: puts [filePath] [filePath]";
                dataOutputStream.writeUTF(error);
                IOUtils.close(dataOutputStream);
                IOUtils.close(fileSocket);
                return ControlledSession.SEND_FILE_ERROR;
            }
        } catch (IOException e) {
            // 发生了连接错误
            return e.toString();
        }
    }
}
