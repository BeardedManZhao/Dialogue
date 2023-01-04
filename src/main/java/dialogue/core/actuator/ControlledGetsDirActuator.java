package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 被控 目录获取执行器，将目录中所有的文件全都进行远程传输的类
 * <p>
 * Controlled directory acquisition actuator, a class that transmits all files in the directory remotely
 *
 * @author zhao
 */
public class ControlledGetsDirActuator extends ControlledGetFileActuator {

    public ControlledGetsDirActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        super(accept, inputStream, outputStream);
    }

    @Override
    public String getName() {
        return "gets";
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
        // 首先找到目录路径
        if (matcher.find()) {
            String dirPath = matcher.group(1);
            File file = new File(dirPath);
            Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(fileSocket.getInputStream());
            ConfigureConstantArea.LOGGER.info(OK_1);
            if (file.exists() && file.isDirectory()) {
                // 开始迭代每一个文件
                File[] files = file.listFiles();
                if (files != null) {
                    for (File listFile : files) {
                        if (listFile.isDirectory()) {
                            continue;
                        }
                        String name = listFile.getName();
                        new Thread(() -> {
                            try {
                                ConfigureConstantArea.LOGGER.info(name);
                                // 获取到当前文件的大小，返回给主控
                                dataOutputStream.writeInt((int) listFile.length());
                                // 获取到当前文件的名称，返回给主控
                                dataOutputStream.writeUTF(name);
                                // 开始传输数据
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(listFile));
                                IOUtils.copy(bufferedInputStream, dataOutputStream, false);
                                dataOutputStream.flush();
                                // 当前文件传输完毕，关闭当前文件的数据输入流
                                IOUtils.close(bufferedInputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        // 等待主控发送准备好接受下一次数据的指令
                        ConfigureConstantArea.LOGGER.info(dataInputStream.readUTF());
                    }
                    // 发送完毕，返回 -2
                    dataOutputStream.writeInt(-2);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    IOUtils.close(fileSocket);
                    // 返回成功
                    return ControlledSession.SEND_FILE_BYTE;
                } else {
                    // 这个情况代表有错误，返回错误码
                    dataOutputStream.writeInt(-1);
                    dataOutputStream.flush();
                    // 然后返回错误数据
                    dataOutputStream.writeUTF("Access to this directory is denied. " + dirPath);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    IOUtils.close(fileSocket);
                    // 返回错误
                    return ControlledSession.SEND_FILE_ERROR;
                }
            } else {
                // 这个情况代表有错误，返回错误码
                dataOutputStream.writeInt(-1);
                dataOutputStream.flush();
                // 然后返回错误数据
                dataOutputStream.writeUTF("The file directory does not exist, or the directory you specified is not a directory. " + dirPath);
                dataOutputStream.flush();
                dataOutputStream.close();
                IOUtils.close(fileSocket);
                // 返回错误
                return ControlledSession.SEND_FILE_ERROR;
            }
        } else {
            // 这个情况代表有错误，且无法避免
            return "ERROR COMMAND " + command + "\nExample: gets [dirPath] [dirPath]";
        }
    }
}
