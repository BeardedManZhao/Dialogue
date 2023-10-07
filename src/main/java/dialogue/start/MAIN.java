package dialogue.start;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledPersistentSession;
import dialogue.core.controlled.ControlledSession;
import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterPersistentSession;
import dialogue.core.master.MasterSession;
import dialogue.utils.ConsoleColor;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * 启动类
 *
 * @author 赵凌宇
 */
public final class MAIN {
    private final static Scanner SCANNER = new Scanner(System.in, ConfigureConstantArea.CHARSET);
    private static boolean status = true;

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0) {
            if (args.length > 1 && "master".equalsIgnoreCase(args[1])) {
                ConfigureConstantArea.LOGGER.log(Level.INFO, "dialogue 示例PC客户端版本：" + ConfigureConstantArea.VERSION);
                ConfigureConstantArea.LOGGER.log(Level.INFO, "Master主控会话配置.......");
                Thread.sleep(1024);
                System.out.print("* 操控设备的IP地址 >>> ");
                String ip = SCANNER.nextLine();
                System.out.print("* 操控设备的端口号 >>> ");
                String port = SCANNER.nextLine();
                Thread.sleep(1024);
                MasterSession instance = "false".equalsIgnoreCase(args[0]) ? MasterFileSession.getInstance() : MasterPersistentSession.getInstance();
                instance.start(ip, port);
                if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
                    String colorCommand = "* >>> " + ConsoleColor.COLOR_CYAN;
                    while (status) {
                        System.out.print(colorCommand);
                        String command = SCANNER.nextLine();
                        System.out.print(ConsoleColor.COLOR_DEF);
                        runCommand(instance, command);
                    }
                    instance.stop();
                } else {
                    while (status) {
                        System.out.print("* >>> ");
                        String command = SCANNER.nextLine();
                        runCommand(instance, command);
                    }
                    instance.stop();
                }
            } else {
                ConfigureConstantArea.LOGGER.log(Level.INFO, "默认被控会话系统启动.......");
                ConfigureConstantArea.LOGGER.log(Level.INFO, "根据配置文件打开被控端口：" + ConfigureConstantArea.CONTROLLED_PORT);
                Thread.sleep(1024);
                ControlledSession instance = "false".equalsIgnoreCase(args[0]) ? ControlledFileSession.getInstance() : ControlledPersistentSession.getInstance();
                new Thread(instance::start).start();
                while (status) {
                    String s = SCANNER.nextLine();
                    if ("exit".equalsIgnoreCase(s)) {
                        status = false;
                        instance.stop();
                        System.out.println("state >>> 被控会话运行状态布尔值\t:\t" + instance.isRunning());
                        instance.shutDown();
                        return;
                    } else if ("state".equalsIgnoreCase(s)) {
                        InetAddress inetAddress = instance.ConnectedMaster();
                        if (inetAddress != null) {
                            System.out.println("state >>> 被控会话运行状态布尔值\t:\t" + instance.isRunning());
                            System.out.println("state >>> 当前连接的主控主机名称\t:\t" + inetAddress.getHostName());
                            System.out.println("state >>> 当前连接的主控主机标识\t:\t" + inetAddress.getCanonicalHostName());
                            System.out.println("state >>> 当前会话已运行时长(MS)\t:\t" + instance.getRunTimeMS());
                            System.out.println("state >>> 当前连接的主控主机状态\t:\tActive");
                        } else {
                            System.out.println("state >>> 被控会话运行状态布尔值\t:\t" + instance.isRunning());
                            System.out.println("state >>> 当前连接的主控主机状态\t:\tNo connection");
                        }
                    }
                }
            }
        } else {
            System.out.println("您需要传递参数 【持久会话】 【会话身份】\n" +
                    "\t【持久会话】 ：true或false，代表您是否要使用持久会话，如果不需要进行持久会话的交互，您可以传递false。\n" +
                    "\t【会话身份】 ：master或其它，代表您是否要使用主控会话，如果您不想要使用主控会话，这里不需要传递参数。");
        }
    }

    private static void runCommand(MasterSession instance, String command) throws InterruptedException {
        if ("exit".equalsIgnoreCase(command)) {
            status = false;
            return;
        } else if ("state".equalsIgnoreCase(command)) {
            InetAddress inetAddress = instance.ConnectedControlled();
            if (inetAddress != null) {
                System.out.println("state >>> 主控会话运行状态布尔值\t:\t" + instance.isRunning());
                System.out.println("state >>> 当前连接的被控主机名称\t:\t" + inetAddress.getHostName());
                System.out.println("state >>> 当前连接的被控主机标识\t:\t" + inetAddress.getCanonicalHostName());
                System.out.println("state >>> 当前会话已运行时长(MS)\t:\t" + instance.getRunTimeMS());
                System.out.println("state >>> 当前连接的被控主机状态\t:\tActive");
            } else {
                System.out.println("state >>> 主控会话运行状态布尔值\t:\t" + instance.isRunning());
                System.out.println("state >>> 当前连接的被控主机状态\t:\tNo connection");
            }
            return;
        }
        System.out.println(instance.runCommand(command));
        Thread.sleep(512);
    }
}
