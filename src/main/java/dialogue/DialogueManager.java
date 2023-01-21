package dialogue;

import dialogue.core.exception.SessionExtractionException;

/**
 * 主机管理者类，针对每一个IP的主机对象，该管理者中都会存储对应的主机对象。
 * <p>
 * The host manager class stores the corresponding host objects for each IP host object.
 *
 * @author 赵凌宇
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
     * 将一个会话从管理者中注销，一般是由框架内部进行管理的
     *
     * @param sessionNum 需要被注销的内存占用位。
     */
    public static void unRegisterSession(int sessionNum) {
        if (sessionNum < Session.SESSION_LENGTH) {
            sessions[sessionNum] = null;
        } else {
            ConfigureConstantArea.LOGGER.warning("The session number does not exist in the manager, so it cannot be logged off.\nERROR SessionNum = " + sessionNum);
        }
    }

    /**
     * @return 当前会话对象对应的会话编号，从1.0.1版本开始，该函数支持调用。
     * <p>
     * The session number does not exist in the manager, so it cannot be logged off. The session number corresponding to the current session object. Starting from version 1.0.1, this function supports calling.
     */
    @Override
    public short getSessionNum() {
        return PRESET_SESSION;
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
