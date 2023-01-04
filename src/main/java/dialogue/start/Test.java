package dialogue.start;

import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterSession;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test {

    public static void main(String[] args) {
        // 获取到第一个主控会话对象
        MasterSession instance = MasterFileSession.getInstance();
        // 以第一个会话为原型，克隆出一个新会话
        MasterSession instance1 = instance.cloneSession();
        // 启动主控会话 同时提供被控设备的IP和被控会话端口
        instance1.start("192.168.1.25", "10003");
        instance.start("192.168.1.15", "10003");
        // 开始执行打开记事本命令
        String s = instance.runCommand("cmd /c notepad");
        System.out.println("* 执行结果 >>> " + s);
        String s1 = instance1.runCommand("cmd /c notepad");
        System.out.println("* 执行结果 >>> " + s1);
        // 执行 put 命令 将一个文件 远程传输给被控
        String s2 = instance.runCommand("put D:\\MyGitHub\\Dialogue\\out\\artifacts\\Dialogue_jar\\conf\\conf.properties conf\\conf.properties");
        System.out.println(s2);
        // 关闭主控会话
        instance.stop();
        instance1.stop();
    }
}
