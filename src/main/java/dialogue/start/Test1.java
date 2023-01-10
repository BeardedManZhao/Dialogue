package dialogue.start;

import dialogue.core.master.MasterPersistentSession;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 测试用例
 *
 * @author 赵凌宇
 */
public class Test1 {

    public static void main(String[] args) throws InterruptedException, IOException {
        // 获取到持久会话对象
        MasterPersistentSession instance = MasterPersistentSession.getInstance();
        // 设置持久会话对象在运行长命令时，数据实时传递的数据流
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("E:\\MyProject\\Dialogue_jar\\res.txt"));
        instance.setInputStream(System.in);
        instance.setOutputStream(bufferedOutputStream);
        // 开始启动持久会话对象
        instance.start("127.0.0.1", "10001");
        if (instance.isRunning()) {
            // 执行一个长会话命令 打开cmd终端 注意这个时候会被阻塞，开启持久会话的信息传递
            String s = instance.runCommand("cmd");
            System.out.println(s);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        instance.stop();
    }
}
