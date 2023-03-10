package dialogue;

import dialogue.core.exception.SessionExtractionException;
import dialogue.core.exception.SessionRunException;

import java.util.regex.Pattern;

/**
 * 会话对象，是直接与用户进行交互的类。
 * <p>
 * Session objects are classes that interact directly with users.
 */
public interface Session {

    /**
     * 会话没有启动，却执行的会话逻辑的情况下会抛出该异常信息
     */
    SessionRunException SESSION_NOT_STARTED = new SessionRunException("Session not started");
    Pattern COMMAND_PATTERN = Pattern.compile("\\s*?(\\S+)\\s*");

    /**
     * 预置会话，是库中不可被管理的会话，这些会话的存在是被库中的每一个组件所依赖的。
     * <p>
     * Preset sessions are unmanageable sessions in the library. The existence of these sessions is dependent on each component in the library.
     */
    short PRESET_SESSION = 0b10000000000;

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
     * 主控会话-持久会话编号，在该会话中，能够实时接收来自对方终端传递的数据，针对一些需要长时间保持连接的命令，该会话将可以实现实时处理功能。
     * <p>
     * Master session - persistent session number. In this session, the data transmitted from the opposite terminal can be received in real time. For some commands that need to be connected for a long time, the session can realize real-time processing function.
     */
    short MASTER_PERSISTENT_SESSION = 4;

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
     * 被控会话-持久会话编号，在该会话中，能够实时获取到被控终端数据，是专注于任何终端命令执行的优化实现。
     * <p>
     * Controlled session - persistent session number. In this session, the controlled terminal data can be obtained in real time, which is an optimized implementation focusing on the execution of any terminal command.
     */
    short CONTROLLED_PERSISTENT_SESSION = 5;

    /**
     * 会话系统中的会话种类数量，每一种会话有不同的功能与作用，一般来说，会话编号越高，会话功能越强大，但是对应的性能可能会稍微降低，因为会话之间是职责链设计，如果当前会话无法执行命令时，还会将命令传递给父类去实现。
     * <p>
     * The number of session types in the session system. Each session has different functions and functions. Generally speaking, the higher the session number is, the stronger the session function is. However, the corresponding performance may be slightly reduced. Because of the responsibility chain design between sessions, if the current session cannot execute the command, the command will be passed to the parent class for implementation.
     */
    short SESSION_LENGTH = 6;

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
        if (SessionNum == PRESET_SESSION) {
            throw new SessionExtractionException("Preset sessions cannot be extracted and used separately.\nERROR SessionNum = " + PRESET_SESSION);
        }
        return DialogueManager.getSession(SessionNum);
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    short getSessionNum();

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
