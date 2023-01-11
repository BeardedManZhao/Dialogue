package dialogue.core.master;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.actuator.*;
import dialogue.core.exception.SessionRunException;
import dialogue.core.result.StringResult;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * 主控文件会话对象，支持将对方的返回数据存储到文件中，能够实现被控文件的查阅与获取。
 * <p>
 * It controls the file session object, supports the storage of the returned data of the other party in the file, and can realize the query and acquisition of the controlled file.
 *
 * @author 赵凌宇
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

    protected MasterLookFileActuator masterLookFileActuator;
    protected MasterGetFileActuator masterGetFileActuator;
    protected MasterPutFileActuator masterPutFileActuator;
    protected MasterGetsDirActuator masterGetsDirActuator;
    protected MasterPutsDirActuator masterPutsDirActuator;
    protected MasterRunningProgramActuator masterRunningProgramActuator;

    protected MasterFileSession() {
    }

    public static MasterFileSession getInstance() {
        return (MasterFileSession) getInstance(MASTER_FILE_SESSION);
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
        super.start(args);
        // 注册需要的组件
        this.masterLookFileActuator = new MasterLookFileActuator(fileSocket, outputStream);
        this.masterGetFileActuator = new MasterGetFileActuator(fileSocket, outputStream);
        this.masterPutFileActuator = new MasterPutFileActuator(fileSocket, outputStream, inputStream);
        this.masterGetsDirActuator = new MasterGetsDirActuator(fileSocket, outputStream);
        this.masterPutsDirActuator = new MasterPutsDirActuator(fileSocket, outputStream);
        this.masterRunningProgramActuator = new MasterRunningProgramActuator(fileSocket, outputStream, inputStream);
        ActuatorManager.registerMasterActuator(masterLookFileActuator);
        ActuatorManager.registerMasterActuator(masterGetFileActuator);
        ActuatorManager.registerMasterActuator(masterPutFileActuator);
        ActuatorManager.registerMasterActuator(masterGetsDirActuator);
        ActuatorManager.registerMasterActuator(masterPutsDirActuator);
        ActuatorManager.registerMasterActuator(masterRunningProgramActuator);
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
        ActuatorManager.unMasterRegister(this.masterLookFileActuator.getName());
        ActuatorManager.unMasterRegister(this.masterLookFileActuator.getName());
        ActuatorManager.unMasterRegister(this.masterPutFileActuator.getName());
        ActuatorManager.unMasterRegister(this.masterGetsDirActuator.getName());
        ActuatorManager.unMasterRegister(this.masterPutsDirActuator.getName());
        ActuatorManager.unMasterRegister(this.masterRunningProgramActuator.getName());
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
                        Actuator actuator = ActuatorManager.getMasterActuatorOrNull(type.trim());
                        if (actuator != null && actuator.getType() == Session.MASTER_FILE_SESSION) {
                            return actuator.runActuatorCommand(command, matcher);
                        } else {
                            // 如果不是我们的文件会话可以处理的命令，就直接提供给父类处理
                            return super.runCommand(command);
                        }
                    } catch (IOException e) {
                        throw new SessionRunException("An error occurred while running the command: " + command, e);
                    }
                }
            } else {
                // 如果文件通道没有打开，就直接传递给父类执行
                return super.runCommand(command);
            }
        }
        return "null";
    }

    /**
     * 运行一个命令，并返回运行结果。
     * <p>
     * Run a command and return the running result.
     *
     * @param command 需要在主机上运行的命令
     *                <p>
     *                Commands that need to be run on the host
     * @return 运行命令之后的结果数据，该函数与普通的runCommand的最大区别在于该函数不会轻易的抛出异常信息，更多的是将异常信息记录再结果对象中
     * <p>
     * The result data after running the command. The biggest difference between this function and the common runCommand is that this function does not easily throw exception information, and more importantly, it records the exception information in the result object
     */
    @Override
    public StringResult runCommandGetResult(String command) {
        try {
            return new StringResult(true, Session.MASTER_FILE_SESSION, runCommand(command));
        } catch (Exception e) {
            return new StringResult(false, Session.MASTER_FILE_SESSION, e.toString());
        }
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
    public MasterSession cloneSession() {
        return new MasterFileSession();
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    @Override
    public short getSessionNum() {
        return MASTER_FILE_SESSION;
    }
}
