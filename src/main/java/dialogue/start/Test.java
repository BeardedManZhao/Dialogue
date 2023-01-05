package dialogue.start;

import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        // 获取到第一个被控会话 该会话将会在配置文件中指定的端口开启服务
        ControlledSession instance1 = ControlledFileSession.getInstance();
        // 启动第一个会话
        new Thread(instance1::start).start();
        // 克隆出一个新的被控，同时指定新端口
        ControlledSession instance2 = instance1.cloneSession(10241);
        // 启动第二个会话
        new Thread(instance2::start).start();
        // 保持被控会话的运行，避免被立刻关闭
        Thread.sleep(102400);
        // 获取到两个被控会话的连接信息 从这里可以看到，两个会话被不同的设备连接了
        System.out.println("会话1所连接的主控设备名称：" + instance1.ConnectedMaster().getHostName());
        System.out.println("会话2所连接的主控设备名称：" + instance2.ConnectedMaster().getHostName());
        // 执行完毕之后可以关闭会话
        instance1.stop();
        instance2.stop();
    }
}
