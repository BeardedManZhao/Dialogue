package dialogue.start;

import dialogue.core.master.MasterPersistentSession;
import dialogue.core.master.MasterSession;

import java.io.IOException;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test1 {

    public static void main(String[] args) throws InterruptedException, IOException {
        // 获取到持久会话对象
        MasterSession instance = MasterPersistentSession.getInstance();
        instance.start("127.0.0.1", "10001");
        if (instance.isRunning()) {
            // 执行一个长会话命令 打开cmd终端
            String s = instance.runCommand("cmd");
            System.out.println(s);
        }
        instance.stop();
    }
}
