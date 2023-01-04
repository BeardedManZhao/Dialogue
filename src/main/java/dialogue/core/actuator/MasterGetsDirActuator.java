package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 主控 目录获取执行器，将目录中所有的文件全都进行远程传输的类
 * <p>
 * The class that controls the directory acquisition executor and transfers all files in the directory remotely
 *
 * @author zhao
 */
public class MasterGetsDirActuator extends MasterGetFileActuator {

    private final static String ERROR = "Incorrect syntax! => gets [Remote Dir Path] [local Dir Path]";

    public MasterGetsDirActuator(ServerSocket fileSocket, OutputStream outputStream) {
        super(fileSocket, outputStream);
    }

    /**
     * 判断接受到的状态码，并根据状态码返回对应的结果数据
     *
     * @param size            文件状态码
     * @param dataInputStream 数据输入流
     * @return 不同状态码对应的结果数据
     * @throws IOException 文件读取异常
     */
    private static String getString(int size, DataInputStream dataInputStream) throws IOException {
        if (size == -1) {
            // 这个情况代表发生了错误，直接将错误信息返回出去
            String error = dataInputStream.readUTF();
            dataInputStream.close();
            return error;
        } else if (size == -2) {
            // 这个状态代表操作完成！
            dataInputStream.close();
            return " dir download ok!!!";
        }
        return null;
    }

    @Override
    public String getName() {
        return "gets";
    }

    /**
     * gets [dirPath] [newDirPath]
     *
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
        // 获取到下载到本机的新目录路径
        if (matcher.find() && matcher.find()) {
            String filePath = matcher.group(1);
            File file = new File(filePath);
            // 判断当前目录是否存在
            if (file.exists() && file.isDirectory()) {
                // 如果存在就开始传递命令
                outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
                outputStream.flush();
                // 等待连接接收
                Socket accept = fileSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(accept.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(accept.getOutputStream());
                // 准备进度条
                int size = dataInputStream.readInt();
                if (ConfigureConstantArea.FILE_PROGRESS != null) {
                    // 接受到文件之后开始下一个文件的大小或状态码：-1代表发生错误 -2代表发送完毕
                    while (size >= -2) {
                        String error = getString(size, dataInputStream);
                        if (error != null) return error;
                        // 等待获取当前文件的名称
                        String fileName = dataInputStream.readUTF();
                        System.out.println();
                        ConfigureConstantArea.LOGGER.info(fileName + " Start downloading.....");
                        // 如果都不是 那么， size 就代表当前文件数据的大小
                        ConfigureConstantArea.FILE_PROGRESS.setMaxSize(size);
                        ConfigureConstantArea.FILE_PROGRESS.function1(0);
                        // 开始传输数据
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath + '/' + fileName));
                        byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                        int offset;
                        while ((offset = dataInputStream.read(buffer)) > 0) {
                            ConfigureConstantArea.FILE_PROGRESS.function2(offset);
                            bufferedOutputStream.write(buffer, 0, offset);
                            if ((size -= offset) <= 0) {
                                // 当前文件传输完成
                                ConfigureConstantArea.FILE_PROGRESS.function3(offset);
                                bufferedOutputStream.flush();
                                bufferedOutputStream.close();
                                // 告知被控已经准备好接受下一次数据
                                dataOutputStream.writeUTF(OK_2);
                                // 开始读取下一个状态的数值
                                size = dataInputStream.readInt();
                                // 开始下一个文件的获取
                                break;
                            }
                        }
                    }
                } else {
                    // 代表不使用进度条
                    // 接受到文件之后开始下一个文件的大小或状态码：-1代表发生错误 -2代表发送完毕
                    while (size >= -2) {
                        String error = getString(size, dataInputStream);
                        if (error != null) return error;
                        // 如果都不是 那么， size 就代表当前文件数据的大小
                        // 等待获取当前文件的名称
                        String fileName = dataInputStream.readUTF();
                        ConfigureConstantArea.LOGGER.info(fileName + " Start downloading.....");
                        // 开始传输数据
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath + '/' + fileName));
                        byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                        int offset;
                        while ((offset = dataInputStream.read(buffer)) > 0) {
                            bufferedOutputStream.write(buffer, 0, offset);
                            if ((size -= offset) <= 0) {
                                // 当前文件传输完成
                                bufferedOutputStream.flush();
                                bufferedOutputStream.close();
                                ConfigureConstantArea.LOGGER.info(fileName + " download ok!");
                                // 告知被控已经准备好接受下一次数据
                                dataOutputStream.writeUTF(OK_2);
                                // 开始读取下一个状态的数值
                                size = dataInputStream.readInt();
                                // 开始下一个文件的获取
                                break;
                            }
                        }
                    }
                }
                IOUtils.close(dataInputStream);
                IOUtils.close(accept);
                return "All files in the directory have been obtained successfully!!!";
            } else {
                return file.getPath() + " is not a directory";
            }
        } else {
            return ERROR;
        }
    }
}
