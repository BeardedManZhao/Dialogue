package dialogue.core.actuator;

import java.io.IOException;
import java.util.regex.Matcher;

/**
 * 执行器对象，执行器本身属于命令的执行者，被各类会话所使用
 * <p>
 * Executor object, which belongs to the command executor and is used by various sessions
 */
public interface Actuator {

    /**
     * 阶段标识，用于同步主与被之间的流程顺序
     */
    String OK_1 = "ok/file1";
    String OK_2 = "ok/file2";

    /**
     * @return 能够调用该执行器的会话对象编号。
     * <p>
     * The number of the session object that can call the executor.
     */
    int getType();

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    String getName();

    /**
     * 返回会话当前运行状态，当一个会话没有在运行的时候，该函数将返回false，一个没有运行中的会话将不具备执行命令与回显数据的能力
     * <p>
     * Returns the current running state of the session. When a session is not running, this function will return false. A session that is not running will not have the ability to execute commands and echo data
     *
     * @return 如果返回true，代表当前会话正在运行中
     * <p>
     * If true is returned, the current session is running
     */
    boolean isRunning();

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
    String runActuatorCommand(String command, Matcher matcher) throws IOException;
}
