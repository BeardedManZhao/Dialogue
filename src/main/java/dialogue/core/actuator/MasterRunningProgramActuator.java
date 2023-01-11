package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.utils.StrUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.regex.Matcher;

import static dialogue.Session.COMMAND_PATTERN;

/**
 * 将程序在远程主机中运行，并在运行之后获取到结果数据。
 * <p>
 * Run the program in the remote host, and obtain the result data after running.
 *
 * @author 赵凌宇
 */
public class MasterRunningProgramActuator extends MasterPutFileActuator {

    public final static String JAVA_MODE = "java";
    public final static String PYTHON_MODE = "py";
    public final static String EXE_OR_BAT_MODE = "all";
    private final static String START_PUT = "Start put program file....";
    private final static String START_RUNNING = "Start running the program....";
    private final static String ERROR = "Incorrect syntax! => running [mode] [filePath] [Parameters]\n" +
            "\t[mode]: \n" +
            "\t\tjava   : 远程运行一个Java程序，Jar包。\n" +
            "\t\tpy     : 远程运行一个python文件，py。\n" +
            "\t\tall    : 远程运行一个控制台可直接启动的文件，例如exe或脚本。\n" +
            "\t[filePath]   : 需要被远程运行的程序文件。\n" +
            "\t[Parameters] : 运行程序时需要传递给程序的参数\n";

    public MasterRunningProgramActuator(ServerSocket fileSocket, OutputStream outputStream, InputStream inputStream) {
        super(fileSocket, outputStream, inputStream);
    }

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the getCommand ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "running";
    }

    /**
     * @param command 需要执行的命令参数
     *                <p>
     *                Command parameters to be executed
     * @param matcher 命令匹配器，通过该匹配器获取到命令中的所需参数
     *                <p>
     *                Command matcher, through which the required parameters in the getCommand can be obtained
     * @return 运行之后的结果的字符串形式
     * <p>
     * String form of the result after running
     * <p>
     * running [程序运行模式] [需要被远程运行的本机程序]
     */
    @Override
    public String runActuatorCommand(String command, Matcher matcher) {
        // 校验命令
        if (matcher.find()) {
            // 查看运行模式是否可以被支持
            String group = matcher.group(1);
            if (!(JAVA_MODE.equalsIgnoreCase(group) || PYTHON_MODE.equalsIgnoreCase(group) || EXE_OR_BAT_MODE.equalsIgnoreCase(group))) {
                return "Unknown operation mode：" + group + '\n' + ERROR;
            } else {
                int backStart = matcher.start();
                // 如果可以被支持就获取到文件名称
                if (matcher.find()) {
                    String name = new File(matcher.group(1)).getName();
                    // 然后拼接出新命令
                    command = getCommand(command, name);
                    ConfigureConstantArea.LOGGER.info(command);
                    matcher = COMMAND_PATTERN.matcher(command);
                    // 移动matcher的指针
                    if (!matcher.find(backStart)) {
                        return "ERROR COMMAND " + command + '\n' + ERROR;
                    }
                } else {
                    return ERROR;
                }
                ConfigureConstantArea.LOGGER.info(START_PUT);
                // 将需要运行在远程主机中的文件发送到远程主机中的程序运行目录
                String s1 = super.runActuatorCommand(command, matcher);
                if (SEND_OK.equals(s1)) {
                    // 接收程序运行结果
                    try {
                        ConfigureConstantArea.LOGGER.info(START_RUNNING);
                        byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
                        return new String(buffer, 0, inputStream.read(buffer));
                    } catch (IOException e) {
                        return e.toString();
                    }
                } else {
                    return s1;
                }
            }
        } else {
            return ERROR;
        }
    }

    private String getCommand(String command, String name) {
        // 首先将命令中的参数进行重组
        char c = ' ';
        String[] strings = StrUtils.splitBy(command, c, 4);
        String filePath = c + ConfigureConstantArea.REMOTE_RUNNING_DIRECTORY + name;
        if (strings.length > 3) {
            // 将新的文件路径插入到语句中
            StringBuilder stringBuilder = new StringBuilder(command.length() + filePath.length() + 16);
            // 将前3个命令元素按原样添加到缓冲
            for (int i = 0; i < 3; i++) {
                stringBuilder.append(strings[i]).append(c);
            }
            // 将文件路径插入到缓冲，然后添加剩余所有元素
            return stringBuilder + filePath + c + strings[3];
        } else {
            // 直接拼接返回
            return command + filePath;
        }
    }
}
