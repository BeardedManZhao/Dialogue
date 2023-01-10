package dialogue.core.master;

import dialogue.ConfigureConstantArea;
import dialogue.core.exception.SessionRunException;
import dialogue.core.exception.SessionStartException;
import dialogue.utils.ConsoleColor;
import dialogue.utils.ExceptionProgress;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * 持久会话实现类，在该会话对象中，支持持久运行的命令，使得在终端命令上的灵活性达到最高。
 *
 * @author 赵凌宇
 */
public class MasterPersistentSession extends TCPSession {

    public static final String MASTER_CLOSE_STRING = "::exit";

    protected final static ServerSocket PERSISTENT_SESSION_SOCKET;

    static {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ConfigureConstantArea.PERSISTENT_SESSION_CHANNEL_PORT);
            ConfigureConstantArea.LOGGER.info("The persistent session service is ready!!!");
        } catch (IOException e) {
            throw new SessionStartException("persistent session service  channel was not initialized successfully! Therefore, the file transfer command cannot be used!", e);
        }
        PERSISTENT_SESSION_SOCKET = serverSocket;
    }

    protected OutputStream udfOutputStream;
    protected Scanner udfInputStreamScanner;

    MasterPersistentSession() {
    }

    /**
     * @return 本库中唯一的持久会话对象，在该对象中，您可以执行一个长会话命令。
     * <p>
     * The only persistent session object in this library. In this object, you can execute a long session command.
     */
    public static MasterPersistentSession getInstance() {
        return (MasterPersistentSession) getInstance(MASTER_PERSISTENT_SESSION);
    }

    /**
     * 启动主机，开始运行逻辑与程序，启动该主机对应的所有功能。
     * <p>
     * Start the host, start running logic and programs, and start all functions corresponding to the host.
     *
     * @param args 主机启动的参数 在这里需要传入被控设备的IP与端口
     */
    @Override
    public void start(String... args) {
        super.start(args);
        // 开始初始化数据流
        if (udfOutputStream == null) udfOutputStream = System.out;
        if (udfInputStreamScanner == null)
            udfInputStreamScanner = new Scanner(System.in, ConfigureConstantArea.CHARSET);
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
        this.udfInputStreamScanner = null;
        this.udfOutputStream = null;
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
        try {
            outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
            outputStream.flush();
            // 等待被控连接
            final Socket accept = PERSISTENT_SESSION_SOCKET.accept();
            final DataInputStream masterDataInputStream = new DataInputStream(accept.getInputStream());
            final DataOutputStream masterDataOutputStream = new DataOutputStream(accept.getOutputStream());
            boolean status = true;
            ConfigureConstantArea.LOGGER.info("+==========================Enter persistent session===============================+");
            ConfigureConstantArea.LOGGER.info("| * >>> Current session connection time   :" + new Date());
            ConfigureConstantArea.LOGGER.info("| * >>> Current session connection command:" + command);
            if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
                System.out.print(ConsoleColor.COLOR_YELLOW);
            }
            // 不断的监听输入流，向被控设备传递持久会话的命令，直到持久会话断开
            new Thread(() -> IOUtils.copy(masterDataInputStream, udfOutputStream, false, ExceptionProgress.NO_ACTION)).start();
            while (status) {
                // 就等待输入命令，并传递给被控
                String s = this.udfInputStreamScanner.nextLine();
                masterDataOutputStream.writeUTF(s);
                masterDataOutputStream.flush();
                if (MASTER_CLOSE_STRING.equalsIgnoreCase(s)) {
                    // 代表退出持久会话，如果连接没有关闭，代表不能进行关闭操作
                    if (accept.isConnected()) {
                        status = false;
                        masterDataInputStream.close();
                        masterDataOutputStream.close();
                        accept.close();
                    } else {
                        System.out.println("session >>> The persistent session cannot exit until it is allowed to end.");
                    }
                }
            }
            // 当会话断开时候返回持久会话已结束
            if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
                System.out.print(ConsoleColor.COLOR_DEF);
            }
            ConfigureConstantArea.LOGGER.info("| * >>> This session is very smooth and will end soon.");
            ConfigureConstantArea.LOGGER.info("+==========================Ending persistent session==============================+");
            return "The session ended successfully.";
        } catch (IOException e) {
            throw new SessionRunException(e);
        } catch (NullPointerException e) {
            throw SESSION_NOT_STARTED;
        }
    }

    /**
     * 设置数据输出流，该函数存在是为了设置负责传递本地数据到远程被控设备服务中的数据流，能够实现用户的各种需求。
     * <p>
     * Set the data output stream. This function exists to set the data stream that is responsible for transferring local data to the remote controlled equipment service, and can meet the various needs of users.
     *
     * @param outputStream 负责将实时命令传递给外界函数的实时数据流，该数据流在持久会话对象启动之后便初始化完成，请确保该数据流在会话结束之前不会被关闭，持久会话只会使用该数据流传递数据，不会有过多的操作。
     *                     <p>
     *                     The real-time data stream that is responsible for transmitting real-time commands to external functions. The data stream is initialized after the persistent session object is started. Please ensure that the data stream will not be closed before the session ends. The persistent session will only use the data stream to transfer data without too many operations.
     */
    public void setOutputStream(OutputStream outputStream) {
        this.udfOutputStream = outputStream;
    }

    /**
     * 设置数据输入流，该函数存在是为了将来自被控的实时数据同步到自己指定的目标位置，可以是终端也可以是其它的数据输入流。
     * <p>
     * Set the data input stream. This function exists to synchronize the real-time data from the controlled system to the designated target location. It can be a terminal or other data input streams.
     *
     * @param inputStreamScanner 负责实时接收来自被控持久会话运行中的一个命令。并将运行时产生的所有数据实时提供到该数据流中，该数据流在持久会话对象启动之后便初始化完成，请确保该数据流在会话结束之前不会被关闭，持久会话只会使用该数据流传递数据，不会有过多的操作。
     *                           <p>
     *                           It is responsible for receiving a command from the running of the controlled persistent session in real time. All data generated at runtime will be provided to the data stream in real time. The data stream will be initialized after the persistent session object is started. Please ensure that the data stream will not be closed before the session ends. The persistent session will only use the data stream to transfer data without too many operations.
     */
    public void setInputStream(InputStream inputStreamScanner) {
        this.udfInputStreamScanner = new Scanner(inputStreamScanner, ConfigureConstantArea.CHARSET);
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
        return new MasterPersistentSession();
    }
}
