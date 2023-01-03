package dialogue.client;

import dialogue.ConfigureConstantArea;
import dialogue.DialogueManager;
import dialogue.server.ConsoleSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * 主控文件会话对象，支持将对方的返回数据存储到文件中，能够实现被控文件的查阅与获取。
 * <p>
 * It controls the file session object, supports the storage of the returned data of the other party in the file, and can realize the query and acquisition of the controlled file.
 *
 * @author zhao
 */
public class MasterFileSession extends TCPSession {

    protected final static ServerSocket fileSocket;

    static {
        ServerSocket fileSocket1;
        try {
            fileSocket1 = new ServerSocket(ConfigureConstantArea.FILE_PORT);
            ConfigureConstantArea.LOGGER.info("file Socket is ok!");
        } catch (IOException e) {
            fileSocket1 = null;
            ConfigureConstantArea.LOGGER.log(Level.WARNING, "The file transfer channel was not initialized successfully! Therefore, the file transfer command cannot be used!");
            e.printStackTrace();
        }
        fileSocket = fileSocket1;
    }

    MasterFileSession() {
    }

    public static MasterSession getInstance() {
        return getInstance(DialogueManager.MASTER_FILE_SESSION);
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
        if (command.length() != 0) {
            if (fileSocket != null) {
                // 首先解析命令运行之后返回的数据格式
                Matcher matcher = COMMAND_PATTERN.matcher(command);
                if (matcher.find()) {
                    String type = matcher.group(1);
                    try {
                        // 根据type 处理到对方发送过来的数据
                        if ("look".equalsIgnoreCase(type)) {
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
                        } else if ("get".equalsIgnoreCase(type)) {
                            // get 目标文件 下载位置
                            if (matcher.find() && matcher.find()) {
                                String downLoadPath = matcher.group(1);
                                if (downLoadPath != null) {
                                    outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
                                    outputStream.flush();
                                    ConfigureConstantArea.LOGGER.info("downloading....");
                                    Socket accept = fileSocket.accept();
                                    DataInputStream inputStream = new DataInputStream(accept.getInputStream());
                                    // 判断状态
                                    String read = inputStream.readUTF();
                                    ConfigureConstantArea.LOGGER.info(read);
                                    if (ConfigureConstantArea.FILE_PROGRESS != null) {
                                        ConfigureConstantArea.FILE_PROGRESS.setMaxSize(inputStream.available());
                                        ConfigureConstantArea.FILE_PROGRESS.function1(0);
                                        if (ConsoleSession.SEND_FILE_ERROR.equals(read)) {
                                            // 这个情况代表本次文件读取有错误，一会传递的是错误信息，而不是文件
                                            ConfigureConstantArea.FILE_PROGRESS.function3(0);
                                            return inputStream.readUTF();
                                        }
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
                                    } else {
                                        if (ConsoleSession.SEND_FILE_ERROR.equals(read)) {
                                            // 这个情况代表本次文件读取有错误，一会传递的是错误信息，而不是文件
                                            return inputStream.readUTF();
                                        }
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
                                    }
                                    return "file download ok!";
                                } else {
                                    return "Incorrect syntax! => get [Remote File Path] [local File Path]";
                                }
                            } else {
                                return "Incorrect syntax! => get [Remote File Path] [local File Path]";
                            }
                        } else {
                            // 如果不是我们的文件会话可以处理的命令，就直接提供给父类处理
                            return super.runCommand(command);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // 如果文件通道没有打开，就直接传递给父类执行
                return super.runCommand(command);
            }
        }
        return "null";
    }
}
