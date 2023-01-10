package dialogue.start;

import dialogue.core.controlled.ControlledPersistentSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author 赵凌宇
 */
public class Test2 {
    public static void main(String[] args) throws InterruptedException {
        // 获取到被控持久会话对象
        ControlledSession instance1 = ControlledPersistentSession.getInstance();
        // 启动持久会话对象，与普通会话的操作方式相同，也是会在这里进入阻塞状态
        new Thread(instance1::start).start();
        // 等待运行
        Thread.sleep(10240);
        // 运行结束之后关闭持久会话对象
        instance1.stop();
    }
}
