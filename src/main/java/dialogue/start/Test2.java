package dialogue.start;

import dialogue.core.controlled.ConsoleSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author 赵凌宇
 */
public class Test2 {
    public static void main(String[] args) throws InterruptedException {
        ControlledSession instance = ConsoleSession.getInstance();
        new Thread(instance::start).start();
        instance.stop();
        new Thread(instance::start).start();
    }
}
