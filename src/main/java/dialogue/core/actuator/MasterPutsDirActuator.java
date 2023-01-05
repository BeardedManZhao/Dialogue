package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.utils.IOUtils;
import dialogue.utils.progressEvent.ProgressFileNumber;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 将主控指定的目录中所有的文件发送到被控的设备中。
 * <p>
 * Send all files in the directory designated by the master controller to the controlled device.
 *
 * @author zhao
 */
public class MasterPutsDirActuator implements Actuator {

    private final static String ERROR = "Incorrect syntax! => puts [local File Path] [Remote New Path]";
    protected final ServerSocket fileSocket;
    protected final OutputStream outputStream;

    public MasterPutsDirActuator(ServerSocket fileSocket, OutputStream outputStream) {
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

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "puts";
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
        // puts [Master dir] [Controlled dir]
        if (matcher.find()) {
            // 获取到当前需要获取的文件目录
            File masterDir = new File(matcher.group(1));
            if (masterDir.exists()) {
                // 开始获取到此目录下的所有文件
                File[] files = masterDir.listFiles();
                if (files != null) {
                    // 给对方发送数据，告知对方要传输文件了
                    outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
                    outputStream.flush();
                    // 接受对方的文件请求
                    Socket accept = fileSocket.accept();
                    DataInputStream dataInputStream = new DataInputStream(accept.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(accept.getOutputStream());
                    for (File file : files) {
                        if (file.isFile()) {
                            // 等待对方回复是否准备好接收数据，如果对方返回的是ok_2代表可以开始传输文件
                            String s = dataInputStream.readUTF();
                            if (OK_2.equals(s)) {
                                // 开始发送编码，告知对方还有下一个文件
                                dataOutputStream.writeUTF(file.getName());
                                // 开始发送文件数据量
                                long length = file.length();
                                dataOutputStream.writeLong(length);
                                // 打开文件数据流
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                                ProgressFileNumber fileProgress = ConfigureConstantArea.FILE_PROGRESS;
                                if (fileProgress != null) {
                                    fileProgress.setMaxSize(length);
                                    System.out.println();
                                }
                                IOUtils.copy(length, bufferedInputStream, dataOutputStream, fileProgress);
                                dataOutputStream.flush();
                                // 发送完毕后关闭当前文件的数据流
                                bufferedInputStream.close();
                            } else {
                                // 代表被控发生错误，这里直接将错误信息获取到并返回出去
                                dataOutputStream.close();
                                dataInputStream.close();
                                accept.close();
                                return s;
                            }
                        }
                    }
                    // 开始发送编码，告知对方，文件传输已经结束
                    dataInputStream.readUTF();
                    dataOutputStream.writeUTF(OK_2);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    dataInputStream.close();
                    accept.close();
                    return " Sending files in directory completed!!!";
                } else {
                    return masterDir.getPath() + " is not a directory";
                }
            } else {
                return masterDir.getPath() + " does not exist in the file system.";
            }
        } else {
            return ERROR;
        }
    }
}
