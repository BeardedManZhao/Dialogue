# Dialogue

# Introduce

Dialogue is a tool made for remote control. Within this framework, there is a very powerful master control function and
a fault tolerance mechanism at the controlled end. Remote control is carried out through TCP, and a startup function is
provided. At the same time, API calls can be made manually!

## What are master control and controlled

As the name implies, the master is the active control terminal device, representing the device that actively sends
control requests to the controlled device. The session to which the device belongs can send commands to the controlled
device through TCP protocol.

The controlled device is the passive control terminal device, which represents the passive acceptance of the data sent
by the main control terminal, and carries out corresponding processing operations. In such processing operations,
various situations and results will occur, and the controlled terminal session will handle these situations, callback
events, and reply results.

### How the master and the controlled are managed

Inert instantiation is adopted for the master and the controlled. Both the master and the controlled belong to the
session object, and the session interface (dialog. Session) has been implemented. The user determines the session object
to use. The instantiation process will be started only after the user obtains a session object. After instantiation, the
user can call the next step.

## What is a session

A session is a console in a device. It is used to exchange information. After the "master device" and "controlled
device" are successfully connected, a unique session will be generated. This session will not be interfered by other
devices. When the connection is disconnected, the current session will be automatically closed, and new sessions will be
created or terminated according to the situation.

Whether it is a master session or a controlled session, it can be obtained through the getInstance function. After
obtaining it, you need to start the session with the start function!.

### What is an actuator

Another important role in the execution of a session is the executor. The executor is a device managed by the session.
The executor contains the specific execution logic of the command. The session will select the executor to execute the
corresponding operation according to the communication content. All executors are stored and managed uniformly. However,
the executors supported by different sessions are different, but users do not need to worry about these, because the
executor is managed and used by the session, The user does not need to consider anything about the actuator.

It is worth noting that the executor is uniformly stored and managed by the executor manager. For flexibility, the
manager (dialogue. core. actor. ActuarirManager) provides functions such as logging off and registering new executors.
However, the use of these functions has certain risks. Please be careful!

### Session List

| Session Type                                   | Session number             | Conversation function                            |
|------------------------------------------------|----------------------------|--------------------------------------------------|
| dialogue.core.master.MasterSession             | null                       | 主控会话的统一抽象，实现了主控会话生命周期的管理逻辑                       |
| dialogue.core.master.TCPSession                | MASTER_TCP_SESSION         | 主控会话TCP会话，实现了主控远程执行命令的操作逻辑                       |
| dialogue.core.master.MasterFileSession         | MASTER_FILE_SESSION        | 主控会话文件传输会话，拓展于主控TCP会话，有着文件上传和下载的实现，且包含TCP会话的所有功能 |
| dialogue.core.controlled.ControlledSession     | null                       | 被控会话的统一抽象，实现了被控会话的生命周期的管理逻辑                      |
| dialogue.core.controlled.ConsoleSession        | CONTROLLED_CONSOLE_SESSION | 被控会话-终端会话，实现了将终端命令执行于处理的操作逻辑                     |
| dialogue.core.controlled.ControlledFileSession | CONTROLLED_FILE_SESSION    | 被控会话文件传输会话，拓展于被控终端会话，有着文件的接受与传输的实现，包含终端会话的所有功能   |

# Example of operation

There are two ways to use it

- The first is to directly start the built-in client startup class. After the startup class is started, the controlled
  device can be remotely operated through the master session according to the boot
- The second is to use the internal API manual call framework to realize the remote operation of the controlled device
  by the master session.

## Use Startup Class

The startup class (dialog. start. MAIN) is a simple API call implementation for example or quick use. You can directly
obtain the prepared Java program of dialogue in the warehouse, or directly start the corresponding startup class in the
source code.

Or copy the following startup class source code to the project project that has imported dialog to start.

```java
package dialogue.start;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledSession;
import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterSession;

import java.util.Scanner;
import java.util.logging.Level;

/**
 * 这里是启动该类源代码
 * @author zhao
 */
public final class MAIN {
    private final static Scanner SCANNER = new Scanner(System.in, ConfigureConstantArea.CHARSET);
    private static boolean status = true;

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0 && "master".equalsIgnoreCase(args[0])) {
            ConfigureConstantArea.LOGGER.log(Level.INFO, "Master主控会话配置.......");
            Thread.sleep(1024);
            System.out.print("* 操控设备的IP地址 >>> ");
            String ip = SCANNER.nextLine();
            System.out.print("* 操控设备的端口号 >>> ");
            String port = SCANNER.nextLine();
            Thread.sleep(1024);
            MasterSession instance = MasterFileSession.getInstance();
            instance.start(ip, port);
            while (status) {
                System.out.print("* >>> ");
                String command = SCANNER.nextLine();
                if ("exit".equalsIgnoreCase(command)) {
                    status = false;
                    continue;
                }
                System.out.println(instance.runCommand(command));
                Thread.sleep(512);
            }
            instance.stop();
        } else {
            ConfigureConstantArea.LOGGER.log(Level.INFO, "默认被控会话系统启动.......");
            ConfigureConstantArea.LOGGER.log(Level.INFO, "根据配置文件打开被控端口：" + ConfigureConstantArea.CONTROLLED_PORT);
            Thread.sleep(1024);
            ControlledSession instance = ControlledFileSession.getInstance();
            new Thread(instance::start).start();
            while (status) {
                String s = SCANNER.nextLine();
                if ("exit".equalsIgnoreCase(s)) {
                    status = false;
                    instance.stop();
                }
            }
        }
    }
}
```

## Use API

This method is also called core operation. API can enable the technology to be quickly integrated into various programs.
API operation is divided into two types: master control and controlled operation.

### Master API call

The calling of the master API is not complicated. It is roughly to obtain the master session, start the master session
using the IP and port, and then directly call the program! After the program is used, you can call stop to close the
session.

```java
package dialogue.start;

import dialogue.Session;
import dialogue.core.master.MasterSession;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test {

    public static void main(String[] args) {
        // 获取到主控会话对象
        MasterSession instance = MasterSession.getInstance(Session.MASTER_FILE_SESSION);
        // 启动会话 指定被控IP与指令处理端口，被控端口默认是10001
        instance.start("192.168.0.101", "10003");
        if (instance.isRunning()) {
            // 执行cmd命令，并获取到执行的结果数据
            String s1 = instance.runCommand("cmd /c dir");
            System.out.println(s1);
            System.out.println();
            // 运行一个python脚本
            String s2 = instance.runCommand("python test.py");
            System.out.println(s2);
            System.out.println();
            // 执行 cmd 命令查看python脚本的代码
            String s3 = instance.runCommand("cmd /c type test.py");
            System.out.println(s3);
            System.out.println();
            // 执行 look 命令，使用文件通道 查看python脚本的代码
            String s4 = instance.runCommand("look ./test.py");
            System.out.println(s4);
            // 执行 get 命令，获取到被控设备的文件数据 这里的进度显示可以在配置文件中 file.progress.event 属性进行设置
            String s5 = instance.runCommand("get ./EB1.ppt ./EB2.ppt");
            System.out.println(s5);
        }
        // 终止当前会话
        instance.stop();
    }
}
```

### Controlled API call

Compared with the master control, the controlled session is simpler. Just note that after the controlled session is
started, it is a blocked client session. It is recommended to use a thread to start the controlled session separately.
It is worth noting that after the stop function of the "master session" is called, the "controlled session"“

It will also be stopped together. This is the lifecycle management mechanism between sessions to maintain the stability
of the current connection. The "charged" will be stopped together with the "master", and then the "charged" will wait
for the connection of the new master again until the user explicitly calls stop

```java
package dialogue.start;

import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test {

    public static void main(String[] args) {
        // 获取到被控会话对象
        ControlledSession instance = ControlledFileSession.getInstance();
        // 启动被控会话 TODO 注意这里会阻塞，同时也可以被主控连接，这里建议使用线程单独启动
        new Thread(instance::start).start();
        // 当运行完毕之后，可以在这里进行显式的stop调用
        instance.stop();
        // 然后就可以终止程序或是其它操作了
        System.exit(0);
    }
}
```

## configuration file

There is a conf.properties file in the conf directory under the current directory, which is the corresponding
configuration file. The configuration items will be indicated below, and you can refer to the required ones.

| Attribute Name              | Default value | Supported versions | function                              |
|-----------------------------|---------------|--------------------|---------------------------------------|
| controlled.port             | 10001         | v1.0               | 被控会话在启动的时候所打开的端口                      |
| logger.level                | INFO          | v1.0               | 系统日志数据的输出级别                           |
| tcp.buffer.max.size         | 65536         | v1.0               | 数据传输过程中，一个数据包的最大长度                    |
| tcp.file.port               | 10002         | v1.0               | 文件传输通道端口                              |
| charset                     | utf-8         | v1.0               | 传输数据与解析数据使用的编码集                       |
| progress.refresh.threshold  | 256           | v1.0               | 传输数据时进度条的刷新阈值，阈值越大，刷新速度越慢             |
| progress.compatibility.mode | false         | v1.0               | 传输数据时进度条的兼容情况，设置为true，可以应对更多不兼容进度条的情况 |
| file.progress.event         | percentage    | v1.0               | 传输数据时进度条的类型，默认是按照百分比显示传输进度            |
| progress.color.display      | true          | v1.0               | 进度条中的颜色显示，如果设置为true可以为进度条渲染颜色         |
