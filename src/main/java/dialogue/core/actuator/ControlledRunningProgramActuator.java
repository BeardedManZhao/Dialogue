package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledSession;
import dialogue.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.Socket;
import java.util.regex.Matcher;

import static dialogue.core.actuator.MasterRunningProgramActuator.*;

/**
 * @author 赵凌宇
 */
public final class ControlledRunningProgramActuator extends ControlledPutFileActuator {

    public ControlledRunningProgramActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        super(accept, inputStream, outputStream);
    }

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
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
     *                Command matcher, through which the required parameters in the command can be obtained
     * @return 运行之后的结果的字符串形式
     * <p>
     * String form of the result after running
     */
    @Override
    public String runActuatorCommand(String command, Matcher matcher) {
        // 接收到文件运行模式
        if (matcher.find()) {
            String mode = matcher.group(1);
            // 接收来自对方的文件
            String s = super.runActuatorCommand(command, matcher);
            if (ControlledSession.SEND_FILE_BYTE.equals(s)) {
                // 这里代表没有错误，开始获取到本文件的路径 并拼接上参数
                String filePath = command.substring(matcher.start());
                try {
                    String command1 = getCommand(mode, filePath);
                    ConfigureConstantArea.LOGGER.info(command1);
                    Process exec = Runtime.getRuntime().exec(command1);
                    SequenceInputStream sequenceInputStream = new SequenceInputStream(exec.getErrorStream(), exec.getInputStream());
                    String stringByStream = IOUtils.getStringByStream(sequenceInputStream);
                    sequenceInputStream.close();
                    return stringByStream;
                } catch (IOException e) {
                    return e.toString();
                }
            } else {
                // 这个位置代表文件接收发生错误，直接将错误返回
                return s;
            }
        } else {
            return "ERROR COMMAND => " + command + "\nrunning [mode] [filePath]";
        }
    }

    private String getCommand(String mode, String filePath) {
        // 根据不同的运行模式构造不同的运行命令，并获取到结果数据
        switch (mode) {
            case EXE_OR_BAT_MODE:
                return filePath;
            case JAVA_MODE:
                return "java -jar " + filePath;
            case PYTHON_MODE:
                return "python " + filePath;
            default:
                return "un";
        }
    }
}
