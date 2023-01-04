package dialogue;

import dialogue.core.exception.SessionExtractionException;

/**
 * 主机管理者类，针对每一个IP的主机对象，该管理者中都会存储对应的主机对象。
 * <p>
 * The host manager class stores the corresponding host objects for each IP host object.
 *
 * @author zhao
 */
public class DialogueManager implements Session {
    /**
     * 所有设备的存储数组，其中偶数索引对应的组件是主控设备对象，奇数是被控设备对象
     * <p>
     * Storage array of all devices, in which the component corresponding to the even index is the master device object and the odd index is the controlled device object
     */
    private final static Session[] sessions = new Session[SESSION_LENGTH];

    @SuppressWarnings("unchecked")
    public static <sessionType> sessionType getSession(int sessionNum) {
        if (sessionNum < SESSION_LENGTH) {
            return (sessionType) sessions[sessionNum];
        } else {
            throw new SessionExtractionException("提取会话对象错误，该会话对象不存在。\nError fetching session object, the session object does not exist.\n => " + sessionNum);
        }
    }

    /**
     * 将一个会话对象注册到管理者中，一般是由框架内部进行管理和注册。
     *
     * @param session    需要被注册的会话对象
     * @param sessionNum 需要将该会话对象注册到哪个索引位置。
     */
    public static void registerSession(Session session, int sessionNum) {
        if (sessionNum >= 0 && sessionNum < SESSION_LENGTH) {
            sessions[sessionNum] = session;
        } else {
            throw new SessionExtractionException("注册的会话编号不正确。\nThe session number registered is incorrect.\nERROR => " + sessionNum);
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
    public Session cloneSession() {
        return this;
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
