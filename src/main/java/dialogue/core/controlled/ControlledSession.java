package dialogue.core.controlled;

import dialogue.ConfigureConstantArea;
import dialogue.DialogueManager;
import dialogue.Host;
import dialogue.Session;
import dialogue.core.exception.SessionExtractionException;
import dialogue.utils.ProgressEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * 服务端会话对象，在会话对象中可以进行命令的传递与解析操作，不同的会话适用不同的操作，每一个会话的实现类都必须要进行该类的处理。
 * <p>
 * The server side session object can transfer and parse commands in the session object. Different sessions apply to different operations. The implementation class of each session must be processed by this class.
 *
 * @author zhao
 */
public abstract class ControlledSession implements Controlled, Host, Session {

    /**
     * 这三个是状态词，如果运行之后返回的是这三个字符串，那么代表本次会话操作完成，不需要进行额外的信息传递。
     * <p>
     * These three are status words. If these three strings are returned after running, the session operation is completed and no additional information transmission is required.
     */
    public final static String SEND_TEXT = "send/text";
    public final static String SEND_FILE_BYTE = "send/bit";
    public final static String SEND_FILE_ERROR = "send/error";


    protected final static Runtime runtime = Runtime.getRuntime();
    private final static String INIT_INFO = "Successfully initialized the server";
    private final static String INIT_ERROR = "Controlled initialization failed";
    private final static String STOP_WARN = "The current session has stopped running";
    private static ServerSocket serverSocket;
    protected Socket accept = null;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    protected boolean Running = false;

    protected ControlledSession() {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(ConfigureConstantArea.CONTROLLED_PORT);
            }
            ConfigureConstantArea.LOGGER.log(Level.INFO, INIT_INFO);
        } catch (IOException e) {
            ConfigureConstantArea.LOGGER.log(Level.SEVERE, INIT_ERROR);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取到一个被控设备对象。
     * <p>
     * Get a controlled device object.
     *
     * @param sessionNum 被控设备编号，在库中的所有被控设备编号都是奇数
     * @return 被控编号对应的被控设备会话对象，该对象提供了命令解析逻辑实现。
     */
    public static ControlledSession getInstance(int sessionNum) {
        if (sessionNum - (sessionNum >> 1 << 1) != 0) {
            ControlledSession session = DialogueManager.getSession(sessionNum);
            if (session == null) {
                if (sessionNum == DialogueManager.CONTROLLED_CONSOLE_SESSION) {
                    session = new ConsoleSession();
                    DialogueManager.registerSession(session, sessionNum);
                } else if (sessionNum == DialogueManager.CONTROLLED_FILE_SESSION) {
                    session = new ControlledFileSession();
                    DialogueManager.registerSession(session, sessionNum);
                }
            }
            return session;
        } else {
            throw new SessionExtractionException("您想要获取的会话组件不属于被控设备会话对象，因此无法获取到对应的设备。\nThe session component you want to obtain does not belong to the controlled device session object, so the corresponding device cannot be obtained.");
        }
    }

    /**
     * @return 判断该会话对象是否正在运行中，如果返回true，代表会话系统已启动，可以正常使用，如果返回false，代表需要调用start函数启动会话。
     * <p>
     * Judge whether the session object is running. If true is returned, the session system is started and can be used normally. If false is returned, the start function needs to be called to start the session.
     */
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
        start(args, null);
    }

    /**
     * 启动主机，开始运行逻辑与程序，启动该主机对应的所有功能。
     * <p>
     * Start the host, start running logic and programs, and start all functions corresponding to the host.
     *
     * @param args          主机启动的参数
     * @param progressEvent 启动之后，在正式开始服务之前，需要的额外启动操作事件实现类，该形参是用于子类进行拓展实现的。
     *                      <p>
     *                      After startup, the additional startup operation event implementation class is required before the service is officially started. This parameter is used for the extension implementation of subclasses.
     */
    protected void start(String[] args, ProgressEvent<Socket, OutputStream, InputStream> progressEvent) {
        if (!(args.length > 0 && "reboot".equalsIgnoreCase(args[0]))) {
            if (!this.Running) {
                this.Running = true;
            } else {
                ConfigureConstantArea.LOGGER.warning("The accused session has been started, so you do not need to start it!");
                return;
            }
        }
        try {
            ConfigureConstantArea.LOGGER.log(Level.INFO, "The accused is ready to start connection.");
            accept = serverSocket.accept();
            inputStream = accept.getInputStream();
            outputStream = accept.getOutputStream();
            final String send_file_byte = SEND_FILE_BYTE + " ok!";
            final String send_file_error = SEND_FILE_ERROR + " error.....";
            final String send_text = SEND_TEXT + " ok!";
            final String return_to_text_data = "return to text data";
            final byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            if (progressEvent != null) {
                progressEvent.function1(accept);
                progressEvent.function2(outputStream);
                progressEvent.function3(inputStream);
            }
            while (this.Running) {
                int offset = inputStream.read(buffer);
                if (offset > 0) {
                    String s = runCommand(new String(buffer, 0, offset, ConfigureConstantArea.CHARSET));
                    if (SEND_FILE_BYTE.equals(s)) {
                        ConfigureConstantArea.LOGGER.info(send_file_byte);
                    } else if (SEND_FILE_ERROR.equals(s)) {
                        ConfigureConstantArea.LOGGER.warning(send_file_error);
                    } else if (SEND_TEXT.equals(s)) {
                        ConfigureConstantArea.LOGGER.info(send_text);
                    } else {
                        ConfigureConstantArea.LOGGER.info(return_to_text_data);
                        outputStream.write(s.getBytes(ConfigureConstantArea.CHARSET));
                        outputStream.flush();
                    }
                } else {
                    outputStream.write("ok!!".getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (accept != null) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    accept.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 如果是外界主动调用的stop，就直接退出，反之是主控断开连接，需要进行重新接受连接操作
            if (!this.Running) {
                ConfigureConstantArea.LOGGER.log(Level.INFO, "Accused to actively stop operation...");
            } else {
                ConfigureConstantArea.LOGGER.log(Level.WARNING, "The master controller disconnects this session, and the accused prepares to accept a new connection again.");
                start("reboot");
            }
        }
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
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ConfigureConstantArea.LOGGER.info("The operation of terminating the controlled session has been completed. You can manually close the current session. Of course, if you do not close the session, the session will be automatically closed when the next master control request is sent.");
        this.Running = false;
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
        return "127.0.0.1";
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
    public abstract ControlledSession cloneSession();
}
