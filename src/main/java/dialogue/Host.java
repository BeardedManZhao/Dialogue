package dialogue;

import java.util.Date;

/**
 * 主机抽象类，其中提供了获取主机的信息与运行命令的函数
 * <p>
 * Host abstract class, which provides functions for obtaining host information and running commands
 */
public interface Host {

    /**
     * 启动主机，开始运行逻辑与程序，启动该主机对应的所有功能。
     * <p>
     * Start the host, start running logic and programs, and start all functions corresponding to the host.
     *
     * @param args 主机启动的参数
     */
    void start(String... args);

    /**
     * 终止主机，停止运行中的逻辑与程序，终止该主机对应的所有功能。
     * <p>
     * Terminate the host, stop the running logic and program, and terminate all functions corresponding to the host.
     *
     * @param args 主机关闭的参数
     */
    void stop(String... args);

    /**
     * @return 主机的启动日期，当主机启动之后这里将会返回日期时间，如果主机没有启动，这里将会返回 null
     * <p>
     * The start date of the host. When the host is started, the date and time will be returned here. If the host is not started, null will be returned here
     */
    Date getStartDate();

    /**
     * @return 运行时长，从上一次启动开始到当下的时间间隔，注意，如果停止，该参数返回0
     * <p>
     * The running time is long. The time interval from the last startup to the current one. Note that if it is stopped, this parameter returns 0
     */
    long getRunTimeMS();

    /**
     * 获取到本主机的IP地址的字符串形式对象。
     * <p>
     * Get the string object of the IP address of this host.
     *
     * @return 本主机的IP地址，由数值组成的ip，例如192.168.0.1
     * <p>
     * The IP address of the host. The IP address consists of numeric values, such as 192.168.0.1
     */
    String getIP();

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
    String runCommand(String command);
}
