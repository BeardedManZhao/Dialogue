package dialogue;

import dialogue.core.exception.SessionRunException;

import java.util.regex.Pattern;

public interface Session {

    /**
     * 会话没有启动，却执行的会话逻辑的情况下会抛出该异常信息
     */
    SessionRunException SESSION_NOT_STARTED = new SessionRunException("Session not started");
    Pattern COMMAND_PATTERN = Pattern.compile("\\s*?(\\S+)\\s*");

    /**
     * 主控会话-TCP命令会话编号，该对象能够实现大部分命令的处理，通过TCP协议将操作在远程设备中执行，并获取到回显结果
     * <p>
     * The master session - TCP command session number. This object can process most commands, execute operations in remote devices through TCP protocol, and obtain echo results
     */
    short MASTER_TCP_SESSION = 0;

    /**
     * 主控会话-二进制数据传输会话编号，在该会话中，能够将二进制数据进行查看与传输，是TCP会话的子类实现与拓展，具备TCP会话的所有功能，在TCP会话的基础上新增了针对二进制数据的传递处理器。
     * <p>
     * The master session - binary data transmission session number. In this session, binary data can be viewed and transmitted. It is the implementation and expansion of subclasses of TCP sessions. It has all the functions of TCP sessions. Based on TCP sessions, a transmission processor for binary data is added.
     */
    short MASTER_FILE_SESSION = 2;

    /**
     * 被控会话-控制台会话编号，在该会话中，能够将命令在终端中执行，并获取到执行结果。
     * <p>
     * Controlled session - console session number. In this session, commands can be executed in the terminal and execution results can be obtained.
     */
    short CONTROLLED_CONSOLE_SESSION = 1;

    /**
     * 被控会话-二进制传递会话编号，在该会话中，能够从磁盘获取到二进制数据，并返还给上一次向被控发送控制请求的IP主控方。
     * <p>
     * The controlled session binary transfers the session number. In this session, the binary data can be obtained from the disk and returned to the IP master who sent the control request to the controlled last time.
     */
    short CONTROLLED_FILE_SESSION = 3;

    /**
     * 会话系统中的会话种类数量，每一种会话有不同的功能与作用，一般来说，会话编号越高，会话功能越强大，但是对应的性能可能会稍微降低，因为会话之间是职责链设计，如果当前会话无法执行命令时，还会将命令传递给父类去实现。
     * <p>
     * The number of session types in the session system. Each session has different functions and functions. Generally speaking, the higher the session number is, the stronger the session function is. However, the corresponding performance may be slightly reduced. Because of the responsibility chain design between sessions, if the current session cannot execute the command, the command will be passed to the parent class for implementation.
     */
    short SESSION_LENGTH = 4;

    /**
     * 获取到一个session对象。
     * <p>
     * Get a session object.
     *
     * @param SessionNum 需要获取的session对象的编号，在Session接口中可以查询到。
     *                   <p>
     *                   The number of the session object to be obtained can be queried in the Session interface.
     * @return session编号对应的会话对象，不同的会话对象由不同的功能与特征
     */
    static Session getInstance(int SessionNum) {
        return DialogueManager.getSession(SessionNum);
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
    Session cloneSession();

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
}
