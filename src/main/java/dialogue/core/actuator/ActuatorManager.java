package dialogue.core.actuator;

import dialogue.Session;

import java.util.HashMap;

/**
 * 执行器管理类，其中是针对所有执行器的管理者，包含针对执行器对象的注册等功能。
 * <p>
 * Executor management class, which refers to the manager of all executors, including functions such as registration of executor objects.
 *
 * @author zhao
 */
public final class ActuatorManager implements Session {

    private final static HashMap<String, Actuator> MASTER_ACTUATOR_HASH_MAP = new HashMap<>();
    private final static HashMap<String, Actuator> CONTROLLED_ACTUATOR_HASH_MAP = new HashMap<>();

    public static Actuator getMasterActuator(String name) {
        Actuator actuator = getMasterActuatorOrNull(name);
        if (actuator != null) {
            return actuator;
        } else {
            throw new RuntimeException("发生了错误，在进行执行器获取的时候，发现没有您指定的执行器!\nAn error occurred. When getting the executor, it was found that there was no integrator you specified!\nERROR => " + name);
        }
    }

    public static Actuator getMasterActuatorOrNull(String name) {
        return MASTER_ACTUATOR_HASH_MAP.get(name);
    }

    /**
     * 判断某一个名称的执行器是否已经被注册了
     * <p>
     * Determine whether an actuator with a name has been registered
     *
     * @param name 需要被判断的执行器的名称
     *             <p>
     *             The name of the actuator to be judged
     * @return 如果返回true 代表被注册了，反之代表没有被注册过。
     * <p>
     * If true is returned, it indicates that it has been registered, otherwise, it indicates that it has not been registered.
     */
    public static boolean containMasterActuator(String name) {
        return MASTER_ACTUATOR_HASH_MAP.containsKey(name);
    }


    /**
     * 判断某一个名称的执行器是否已经被注册了
     * <p>
     * Determine whether an actuator with a name has been registered
     *
     * @param actuator 需要被判断的执行器的对象
     *                 <p>
     *                 The name of the actuator to be judged
     * @return 如果返回true 代表被注册了，反之代表没有被注册过。
     * <p>
     * If true is returned, it indicates that it has been registered, otherwise, it indicates that it has not been registered.
     */
    public static boolean containMasterActuator(Actuator actuator) {
        return MASTER_ACTUATOR_HASH_MAP.containsValue(actuator);
    }

    /**
     * 将一个执行器注册到系统中，使得整个系统可以获取到该执行器。
     * <p>
     * Register an actuator to the system so that the entire system can obtain the actuator.
     *
     * @param actuator 需要被注册的执行器对象。
     *                 <p>
     *                 Integrator object to be registered.
     */
    public static void registerMasterActuator(Actuator actuator) {
        MASTER_ACTUATOR_HASH_MAP.put(actuator.getName(), actuator);
    }

    public static Actuator getControlledActuator(String name) {
        Actuator actuator = getControlledActuatorOrNull(name);
        if (actuator != null) {
            return actuator;
        } else {
            throw new RuntimeException("发生了错误，在进行执行器获取的时候，发现没有您指定的执行器!\nAn error occurred. When getting the executor, it was found that there was no integrator you specified!\nERROR => " + name);
        }
    }

    public static Actuator getControlledActuatorOrNull(String name) {
        return CONTROLLED_ACTUATOR_HASH_MAP.get(name);
    }

    /**
     * 判断某一个名称的执行器是否已经被注册了
     * <p>
     * Determine whether an actuator with a name has been registered
     *
     * @param name 需要被判断的执行器的名称
     *             <p>
     *             The name of the actuator to be judged
     * @return 如果返回true 代表被注册了，反之代表没有被注册过。
     * <p>
     * If true is returned, it indicates that it has been registered, otherwise, it indicates that it has not been registered.
     */
    public static boolean containControlledActuator(String name) {
        return CONTROLLED_ACTUATOR_HASH_MAP.containsKey(name);
    }


    /**
     * 判断某一个名称的执行器是否已经被注册了
     * <p>
     * Determine whether an actuator with a name has been registered
     *
     * @param actuator 需要被判断的执行器的对象
     *                 <p>
     *                 The name of the actuator to be judged
     * @return 如果返回true 代表被注册了，反之代表没有被注册过。
     * <p>
     * If true is returned, it indicates that it has been registered, otherwise, it indicates that it has not been registered.
     */
    public static boolean containControlledActuator(Actuator actuator) {
        return CONTROLLED_ACTUATOR_HASH_MAP.containsValue(actuator);
    }

    /**
     * 将一个执行器注册到系统中，使得整个系统可以获取到该执行器。
     * <p>
     * Register an actuator to the system so that the entire system can obtain the actuator.
     *
     * @param actuator 需要被注册的执行器对象。
     *                 <p>
     *                 Integrator object to be registered.
     */
    public static void registerControlledActuator(Actuator actuator) {
        CONTROLLED_ACTUATOR_HASH_MAP.put(actuator.getName(), actuator);
    }

    /**
     * 将一个执行器从系统中注销，注意，该操作十分危险，如果将依赖组件注销，会导致某些功能瘫痪。
     *
     * @param name 需要被注销的组件名称
     * @return 成功注销的组件，如果这里不是null，代表注销成功。
     */
    public static Actuator unMasterRegister(String name) {
        return CONTROLLED_ACTUATOR_HASH_MAP.remove(name);
    }

    /**
     * 将一个执行器从系统中注销，注意，该操作十分危险，如果将依赖组件注销，会导致某些功能瘫痪。
     *
     * @param name 需要被注销的组件名称
     * @return 成功注销的组件，如果这里不是null，代表注销成功。
     */
    public static Actuator unControlledRegister(String name) {
        return CONTROLLED_ACTUATOR_HASH_MAP.remove(name);
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

    /**
     * 返回会话当前运行状态，当一个会话没有在运行的时候，该函数将返回false，一个没有运行中的会话将不具备执行命令与回显数据的能力
     * <p>
     * Returns the current running state of the session. When a session is not running, this function will return false. A session that is not running will not have the ability to execute commands and echo data
     *
     * @return 如果返回true，代表当前会话正在运行中
     * <p>
     * If true is returned, the current session is running
     */
    @Override
    public boolean isRunning() {
        return MASTER_ACTUATOR_HASH_MAP.size() == 0 && CONTROLLED_ACTUATOR_HASH_MAP.size() == 0;
    }
}
