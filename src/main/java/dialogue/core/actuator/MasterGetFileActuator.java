package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * get 命令执行类，与被控的get执行类相互对应，用于主控从被控设备中获取数据。
 * <p>
 * The get command execution class corresponds to the controlled get execution class and is used by the master control to obtain data from the controlled device.
 *
 * @author 赵凌宇
 */
public class MasterGetFileActuator implements Actuator {
    private final static String ERROR = "Incorrect syntax! => get [Remote File Path] [local File Path]";

    protected final ServerSocket fileSocket;
    protected final OutputStream outputStream;

    public MasterGetFileActuator(ServerSocket fileSocket, OutputStream outputStream) {
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
        // 首先获取到下载路径
        if (matcher.find() && matcher.find()) {
            String downLoadPath = matcher.group(1);
            if (downLoadPath != null) {
                // 然后传递命令给被控
                outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
                outputStream.flush();
                ConfigureConstantArea.LOGGER.info("downloading....");
                // 等待连接接受
                Socket accept = fileSocket.accept();
                // 接收到后开启数据流
                DataInputStream inputStream = new DataInputStream(accept.getInputStream());
                // 判断状态，如果这里返回的不是-1，那么就是成功了，稍后会返回文件数据，而这里的数值就是文件的大小
                long size = inputStream.readLong();
                if (ConfigureConstantArea.FILE_PROGRESS != null) {
                    if (size != -1) {
                        // 代表没有问题，开始接受文件数据
                        ConfigureConstantArea.FILE_PROGRESS.setMaxSize(size);
                        ConfigureConstantArea.FILE_PROGRESS.function1(0);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(downLoadPath));
                        byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                        int offset;
                        while ((offset = inputStream.read(buffer)) > 0) {
                            ConfigureConstantArea.FILE_PROGRESS.function2(offset);
                            // 当前匹配项就是下载路径
                            bufferedOutputStream.write(buffer, 0, offset);
                        }
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        inputStream.close();
                        accept.close();
                        ConfigureConstantArea.FILE_PROGRESS.function3(0);
                        return " file download ok!";
                    } else {
                        // 如果返回的是 -1 代表稍后传输的是错误信息，需要打印出来
                        return inputStream.readUTF();
                    }
                } else {
                    if (size != -1) {
                        // 代表没有问题，开始接受文件数据
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(downLoadPath));
                        byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                        int offset;
                        while ((offset = inputStream.read(buffer)) > 0) {
                            // 当前匹配项就是下载路径
                            bufferedOutputStream.write(buffer, 0, offset);
                        }
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                        inputStream.close();
                        accept.close();
                        return " file download ok!";
                    } else {
                        // 如果返回的是 -1 代表稍后传输的是错误信息，需要打印出来
                        return inputStream.readUTF();
                    }
                }
            } else {
                return ERROR;
            }
        } else {
            return ERROR;
        }
    }
}
