package dialogue.core.master;

import dialogue.ConfigureConstantArea;
import dialogue.core.exception.SessionRunException;
import dialogue.core.exception.SessionStartException;
import dialogue.utils.ConsoleColor;
import dialogue.utils.ExceptionProgress;
import dialogue.utils.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 持久会话实现类，在该会话对象中，支持持久运行的命令，使得在终端命令上的灵活性达到最高。
 *
 * @author zhao
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

    MasterPersistentSession() {
    }

    public static MasterSession getInstance() {
        return getInstance(MASTER_PERSISTENT_SESSION);
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
            new Thread(() -> IOUtils.copy(masterDataInputStream, System.out, false, ExceptionProgress.NO_ACTION)).start();
            while (status) {
                // 就等待输入命令，并传递给被控
                String s = ConfigureConstantArea.SCANNER.nextLine();
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
