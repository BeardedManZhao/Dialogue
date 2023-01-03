package dialogue.client;

import dialogue.ConfigureConstantArea;
import dialogue.DialogueManager;
import dialogue.Host;
import dialogue.Session;
import dialogue.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

/**
 * 主控会话对象，其中包含的就是本对象的启动与关闭以及对被控设备的操作函数。
 * <p>
 * The main control session object includes the startup and shutdown of the object and the operation function of the controlled device.
 *
 * @author zhao
 */
public abstract class MasterSession implements Master, Host, Session {
    private final static String INIT_INFO = "Successfully initialized the Master";
    private final static String START_WARN = "The current session is already running";
    private final static String STOP_WARN = "The current session has stopped running";
    protected static Socket MasterSocket;
    // 传递命令给客户端
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected boolean Running = false;

    protected MasterSession() {
        ConfigureConstantArea.LOGGER.log(Level.INFO, INIT_INFO);
    }

    /**
     * 通过会话对象的编号，将会话对象从管理者中获取到。
     * <p>
     * Get the session object from the manager by its number.
     *
     * @param sessionNum 会话对象的编号，编号数值越大，会话功能越强悍，但是相对应的性能可能会有细微降低。
     *                   <p>
     *                   The number of the session object. The larger the number, the stronger the session function. However, the corresponding performance may be slightly degraded.
     * @return 主控会话对象，会话对象中包含针对被控的操作函数。
     */
    public static MasterSession getInstance(int sessionNum) {
        if (sessionNum - (sessionNum >> 1 << 1) == 0) {
            MasterSession session = DialogueManager.getSession(sessionNum);
            if (session == null) {
                if (sessionNum == DialogueManager.MASTER_TCP_SESSION) {
                    session = new TCPSession();
                    DialogueManager.registerSession(session, sessionNum);
                } else if (sessionNum == DialogueManager.MASTER_FILE_SESSION) {
                    session = new MasterFileSession();
                    DialogueManager.registerSession(session, sessionNum);
                }
            }
            return session;
        } else {
            throw new RuntimeException("您想要获取的会话组件不属于主控设备会话对象，因此无法获取到对应的设备。\nThe session component you want to obtain does not belong to the session object of the master device, so the corresponding device cannot be obtained.");
        }
    }

    /**
     * @return 判断该会话对象是否正在运行中，如果返回true，代表会话系统已启动，可以正常使用，如果返回false，代表需要调用start函数启动会话。
     * <p>
     * Judge whether the session object is running. If true is returned, the session system is started and can be used normally. If false is returned, the start function needs to be called to start the session.
     */
    @Override
    public boolean isRunning() {
        return Running;
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
        if (isRunning()) {
            ConfigureConstantArea.LOGGER.log(Level.WARNING, START_WARN);
        }
        if (args.length > 0) {
            try {
                MasterSocket = new Socket(args[0], Integer.parseInt(args[1]));
                try {
                    inputStream = MasterSocket.getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    outputStream = MasterSocket.getOutputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("An error occurred while preparing to start the Master session. The parameter cannot be parsed!\n" +
                    "Parameter example: arg[0]=[Operating Master IP]  arg[1]=[Operating Master Port]");
        }
        this.Running = true;
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
        if (!isRunning()) {
            ConfigureConstantArea.LOGGER.log(Level.WARNING, STOP_WARN);
            return;
        }
        if (MasterSocket != null) {
            IOUtils.close(this.inputStream);
            IOUtils.close(this.outputStream);
            try {
                MasterSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                this.Running = false;
            }
        }
    }

    /**
     * 获取到本主机的IP地址的字符串形式对象。
     * <p>
     * Get the string object of the IP address of this host.
     *
     * @return 本主机的IP地址，由数值组成的ip，例如192.168.0.1
     * <p>
     * The IP address of the host. The IP address consists of numeric values, such as 192.168.0.1
     */
    @Override
    public String getIP() {
        return MasterSocket.getInetAddress().getHostName();
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
    public abstract String runCommand(String command);
}
