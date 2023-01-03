package dialogue.server;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 被控文件会话对象，该对象中支持文件数据的查阅(look)与获取(get)，如果无法处理命令，将会把数据传递给父类去运行
 * <p>
 * Controlled file session object, which supports file data query (look) and get (get). If the command cannot be processed, the data will be passed to the parent class to run
 *
 * @author zhao
 */
public class ControlledFileSession extends ConsoleSession {

    ControlledFileSession() {
    }

    public static ControlledSession getInstance() {
        return getInstance(Session.CONTROLLED_FILE_SESSION);
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
            // 首先解析命令运行之后返回的数据格式
            Matcher matcher = COMMAND_PATTERN.matcher(command);
            if (matcher.find()) {
                String type = matcher.group(1);
                try {
                    // 根据type 处理到对方发送过来的命令
                    ConfigureConstantArea.LOGGER.info("Extract to file data. => " + command);
                    if ("look".equalsIgnoreCase(type)) {
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
                        return SEND_TEXT;
                    } else if ("get".equalsIgnoreCase(type)) {
                        // get 目标文件 一样要先获取目标文件的数据
                        if (matcher.find()) {
                            Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
                            DataOutputStream outputStream1 = new DataOutputStream(fileSocket.getOutputStream());
                            try {
                                FileInputStream fileInputStream = new FileInputStream(matcher.group(1));
                                // 将本次的发送信息状态提供给主控
                                outputStream1.writeUTF(SEND_FILE_BYTE);
                                // 将数据发送给主控
                                IOUtils.copy(fileInputStream, outputStream1, true);
                                // 返回成功
                                return SEND_FILE_BYTE;
                            } catch (IOException e) {
                                // 将错误提供给主控
                                outputStream1.writeUTF(SEND_FILE_ERROR);
                                outputStream1.flush();
                                outputStream1.writeUTF(e.toString());
                                outputStream1.flush();
                                outputStream1.close();
                                // 返回错误
                                return SEND_FILE_ERROR;
                            } finally {
                                IOUtils.close(fileSocket);
                            }
                        } else {
                            return "ERROR COMMAND " + command + "\nExample: get [filePath] [filePath]";
                        }
                    } else {
                        // 如果不是我们的文件会话可以处理的命令，就直接提供给父类处理
                        return super.runCommand(command);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("发生了未知编码错误，请修改配置文件中的 charset 对应的值。\nAn unknown encoding error has occurred. Please modify the value corresponding to charset in the configuration file.", e);
                } catch (IOException e) {
                    return e.toString();
                }
            }
        }
        return "null";
    }
}
