package dialogue.core.controlled;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.task.StreamCopyTask;
import dialogue.core.exception.SessionStartException;
import dialogue.core.master.MasterPersistentSession;
import dialogue.utils.ExceptionProgress;
import dialogue.utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;

/**
 * 被控长会话终端，在该会话对象中，能够实时接收主控的长会话命令，同时可以将运行日志实时传输给对方。
 * <p>
 * The controlled long session terminal, in this session object, can receive the long session command from the master in real time, and can transmit the running log to the other party in real time.
 *
 * @author 赵凌宇
 */
public class ControlledPersistentSession extends ConsoleSession {

    protected final static byte[] NO_STATUS_PROMPT;

    static {
        try {
            NO_STATUS_PROMPT = "session >>> There is no data in this persistent session. Please use the \"::exit\" command to exit the session\n".getBytes(ConfigureConstantArea.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new SessionStartException(e);
        }
    }

    protected ControlledPersistentSession(int port) {
        super(port);
    }

    public static ControlledSession getInstance() {
        return getInstance(CONTROLLED_PERSISTENT_SESSION);
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
        ConfigureConstantArea.LOGGER.log(Level.INFO, "runSession -> " + command);
        try {
            // 打开持久会话连接，尝试与主控持久会话进行通信
            Socket socket = new Socket(this.accept.getInetAddress().getHostName(), ConfigureConstantArea.PERSISTENT_SESSION_CHANNEL_PORT);
            DataInputStream masterDataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream masterDataOutputStream = new DataOutputStream(socket.getOutputStream());
            final Process exec = runtime.exec(command);
            DataOutputStream exeOutputStream1 = new DataOutputStream(exec.getOutputStream());
            InputStream execInputStream = exec.getInputStream();
            InputStream execErrorStream = exec.getErrorStream();
            // 线程随时将命令产生的数据发送到主控
            Thread thread1 = new Thread(new StreamCopyTask(execInputStream, masterDataOutputStream, false, ExceptionProgress.NO_ACTION));
            Thread thread2 = new Thread(new StreamCopyTask(execErrorStream, masterDataOutputStream, false, ExceptionProgress.NO_ACTION));
            thread2.start();
            thread1.start();
            while (true) {
                // 然后，等待主控的回复
                String s = masterDataInputStream.readUTF().trim();
                if (!(thread1.isAlive() && thread2.isAlive())) {
                    ConfigureConstantArea.LOGGER.log(Level.INFO, "endSession -> " + command);
                    // 断开持久会话
                    break;
                } else {
                    // 如果不是要断开会话，就直接将主控的命令提供给终端任务
                    if (s.length() == 0) {
                        exeOutputStream1.write('\n');
                        exeOutputStream1.flush();
                    } else {
                        exeOutputStream1.write(s.getBytes(ConfigureConstantArea.CHARSET));
                        exeOutputStream1.flush();
                        ConfigureConstantArea.LOGGER.info(s);
                    }

                }
            }
            // 关闭exe的数据流
            IOUtils.close(execErrorStream);
            IOUtils.close(execInputStream);
            IOUtils.close(exeOutputStream1);
            ConfigureConstantArea.LOGGER.info("Open stateless");
            // 持久会话已经可以关闭了，现在开启无状态，直到主控关闭持久会话
            while (!MasterPersistentSession.MASTER_CLOSE_STRING.equals(masterDataInputStream.readUTF())) {
                masterDataOutputStream.write(NO_STATUS_PROMPT);
            }
            masterDataOutputStream.flush();
            IOUtils.close(masterDataInputStream);
            IOUtils.close(masterDataOutputStream);
            IOUtils.close(socket);
            return SEND_FILE_BYTE;
        } catch (RuntimeException | IOException e) {
            return "ERROR => " + e;
        }
    }

    /**
     * 将当前会话克隆一个出来，使得一种会话可以提供给多个网络连接使用，需要注意的是，克隆出来的会话将不会被管理者所管理。
     * <p>
     * Clone the current session to make one session available to multiple network connections. Note that the cloned session will not be managed by the manager.
     *
     * @param port 该被控会话所使用的新端口，当原会话不能够满足主控连接数量时，您可以在此处手动开启一个新的被控端口。
     *             <p>
     *             The new port used by the controlled session. When the original session cannot meet the number of master connections, you can manually open a new controlled port here.
     * @return 一个与当前会话功能一致的新会话对象，不会与原会话有任何的关系
     * <p>
     * A new session object with the same function as the current session will not have any relationship with the original session
     */
    @Override
    public ControlledSession cloneSession(int port) {
        return new ControlledPersistentSession(port);
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    @Override
    public short getSessionNum() {
        return CONTROLLED_PERSISTENT_SESSION;
    }
}
