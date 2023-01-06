package dialogue.start;

import dialogue.core.controlled.ControlledPersistentSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test2 {
    public static void main(String[] args) {
        // 获取到被控持久会话对象
        ControlledSession instance1 = ControlledPersistentSession.getInstance();
        instance1.start();
        instance1.stop();
    }
}
