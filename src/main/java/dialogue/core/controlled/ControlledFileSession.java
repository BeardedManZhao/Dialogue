package dialogue.core.controlled;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.actuator.*;
import dialogue.core.exception.SessionRunException;
import dialogue.utils.ProgressEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

    protected ControlledLookFileActuator controlledLookFileActuator;
    protected ControlledGetFileActuator controlledGetActuator;
    protected ControlledPutFileActuator controlledPutFileActuator;
    protected ControlledGetsDirActuator controlledGetsDirActuator;

    private final ProgressEvent<Socket, OutputStream, InputStream> INIT_ProgressEvent = new ProgressEvent<Socket, OutputStream, InputStream>() {
        private Socket tempSocket;
        private OutputStream tempOut;

        @Override
        public void function1(Socket type) {
            tempSocket = type;
        }

        @Override
        public void function2(OutputStream type) {
            tempOut = type;
        }

        @Override
        public void function3(InputStream type) {
            // 注册需要的组件
            controlledGetActuator = new ControlledGetFileActuator(tempSocket, type, tempOut);
            controlledLookFileActuator = new ControlledLookFileActuator(tempSocket, type, tempOut);
            controlledPutFileActuator = new ControlledPutFileActuator(tempSocket, type, tempOut);
            controlledGetsDirActuator = new ControlledGetsDirActuator(tempSocket, type, tempOut);
            ActuatorManager.registerControlledActuator(controlledGetActuator);
            ActuatorManager.registerControlledActuator(controlledLookFileActuator);
            ActuatorManager.registerControlledActuator(controlledPutFileActuator);
            ActuatorManager.registerControlledActuator(controlledGetsDirActuator);
        }
    };

    protected ControlledFileSession() {
    }

    public static ControlledSession getInstance() {
        return getInstance(CONTROLLED_FILE_SESSION);
    }


    /**
     * 启动主机，开始运行逻辑与程序，启动该主机对应的所有功能。
     * <p>
     * Start the host, start running logic and programs, and start all functions corresponding to the host.
     *
     * @param args 主机启动的参数
     */
    @Override
    public void start(String... args) {
        super.start(args, this.INIT_ProgressEvent);
    }

    /**
     * 终止主机，停止运行中的逻辑与程序，终止该主机对应的所有功能。
     * <p>
     * Terminate the host, stop the running logic and program, and terminate all functions corresponding to the host.
     *
     * @param args 主机关闭的参数
     */
    @Override
    public void stop(String... args) {
        super.stop(args);
        // 注销使用完毕的组件
        ActuatorManager.unControlledRegister(this.controlledGetActuator.getName());
        ActuatorManager.unControlledRegister(this.controlledLookFileActuator.getName());
        ActuatorManager.unControlledRegister(this.controlledPutFileActuator.getName());
        ActuatorManager.unControlledRegister(this.controlledGetsDirActuator.getName());
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
                    Actuator controlledActuator = ActuatorManager.getControlledActuatorOrNull(type);
                    if (controlledActuator != null && controlledActuator.getType() == Session.CONTROLLED_FILE_SESSION) {
                        return controlledActuator.runActuatorCommand(command, matcher);
                    } else {
                        // 如果不是我们的文件会话可以处理的命令，就直接提供给父类处理
                        return super.runCommand(command);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new SessionRunException("发生了未知编码错误，请修改配置文件中的 charset 对应的值。\nAn unknown encoding error has occurred. Please modify the value corresponding to charset in the configuration file.", e);
                } catch (IOException e) {
                    return e.toString();
                }
            }
        }
        return "null";
    }

    /**
     * 将当前会话克隆一个出来，使得一种会话可以提供给多个网络连接使用，需要注意的是，克隆出来的会话将不会被管理者所管理。
     * <p>
     * Clone the current session to make one session available to multiple network connections. Note that the cloned session will not be managed by the manager.
     *
     * @return 一个与当前会话功能一致的新会话对象，不会与原会话有任何的关系
     * <p>
     * A new session object with the same function as the current session will not have any relationship with the original session
     */
    @Override
    public ControlledSession cloneSession() {
        return new ControlledFileSession();
    }
}
