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
 * 主控文件发送执行器，该对象中支持向原主机中发送文件数据。
 * <p>
 * The master file sending actuator supports sending file data to the original host.
 *
 * @author 赵凌宇
 */
public class MasterPutFileActuator implements Actuator {
    protected final static String SEND_OK = " File sending completed";
    private final static String ERROR = "Incorrect syntax! => put [local File Path] [Remote New Path]";
    protected final ServerSocket fileSocket;
    protected final OutputStream outputStream;
    protected final InputStream inputStream;

    public MasterPutFileActuator(ServerSocket fileSocket, OutputStream outputStream, InputStream inputStream) {
        this.fileSocket = fileSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
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
    public String runActuatorCommand(String command, Matcher matcher) {
        if (matcher.find()) {
            // 首先获取到下一个参数，也就是本地路径
            File localFile = new File(matcher.group(1));
            // 然后获取到下一个参数，也就是新文件路径
            if (matcher.find()) {
                try {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(localFile));
                    // 提前给对方发送一个数据，告知我们要开始传输数据了
                    outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
                    // 开始等待对方发送来的接收请求
                    Socket fileSendSocket = fileSocket.accept();
                    // 查看对方是否准备好了接受数据
                    DataOutputStream dataOutputStream = new DataOutputStream(fileSendSocket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(fileSendSocket.getInputStream());
                    ConfigureConstantArea.LOGGER.info(dataInputStream.readUTF());
                    ConfigureConstantArea.LOGGER.info("Start sending file....");
                    String res1 = dataInputStream.readUTF();
                    if (ControlledPutFileActuator.OK_2.equals(res1)) {
                        // 开始发送文件数据主体，这里根据配置准备发送进度条对象
                        {
                            ProgressFileNumber fileProgress = ConfigureConstantArea.FILE_PROGRESS;
                            if (fileProgress != null) {
                                fileProgress.setMaxSize(localFile.length());
                                fileProgress.function1(0);
                                byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                                int offset;
                                while ((offset = (bufferedInputStream.read(buffer))) > 0) {
                                    fileProgress.function2(offset);
                                    dataOutputStream.write(buffer, 0, offset);
                                }
                                fileProgress.function3(0);
                            } else {
                                IOUtils.copy(bufferedInputStream, dataOutputStream, false);
                            }
                        }
                        dataOutputStream.flush();
                        bufferedInputStream.close();
                        dataOutputStream.close();
                        return " File sending completed";
                    } else {
                        // 出现了错误，将错误信息读取出来，然后返回
                        dataOutputStream.flush();
                        bufferedInputStream.close();
                        dataOutputStream.close();
                        return res1;
                    }
                } catch (IOException e) {
                    return e.toString();
                }
            }
        }
        return ERROR;
    }
}
