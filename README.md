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

### Session List

| Session Type                                   | Session number             | Conversation function                            |
|------------------------------------------------|----------------------------|--------------------------------------------------|
| dialogue.core.master.MasterSession             | null                       | 主控会话的统一抽象，实现了主控会话生命周期的管理逻辑                       |
| dialogue.core.master.TCPSession                | MASTER_TCP_SESSION         | 主控会话TCP会话，实现了主控远程执行命令的操作逻辑                       |
| dialogue.core.master.MasterFileSession         | MASTER_FILE_SESSION        | 主控会话文件传输会话，拓展于主控TCP会话，有着文件上传和下载的实现，且包含TCP会话的所有功能 |
| dialogue.core.controlled.ControlledSession     | null                       | 被控会话的统一抽象，实现了被控会话的生命周期的管理逻辑                      |
| dialogue.core.controlled.ConsoleSession        | CONTROLLED_CONSOLE_SESSION | 被控会话-终端会话，实现了将终端命令执行于处理的操作逻辑                     |
| dialogue.core.controlled.ControlledFileSession | CONTROLLED_FILE_SESSION    | 被控会话文件传输会话，拓展于被控终端会话，有着文件的接受与传输的实现，包含终端会话的所有功能   |

## What is an actuator

Another important role in the execution of a session is the executor. The executor is a device managed by the session.
The executor contains the specific execution logic of the command. The session will select the executor to execute the
corresponding operation according to the communication content. All executors are stored and managed uniformly. However,
the executors supported by different sessions are different, but users do not need to worry about these, because the
executor is managed and used by the session, The user does not need to consider anything about the actuator.

It is worth noting that the executor is uniformly stored and managed by the executor manager. For flexibility, the
manager (dialogue.core.actuator.ActuatorManager) provides functions such as logging off and registering new executors.
However, the use of these functions has certain risks. Please be careful!

### Actuator list

| Actuator type                                     | Actuator command | Session to which the actuator belongs | function                                                   |
|---------------------------------------------------|------------------|---------------------------------------|------------------------------------------------------------|
| dialogue.core.actuator.MasterGetFileActuator      | get              | MASTER_FILE_SESSION                   | Receive documents from controlled equipment                |
| dialogue.core.actuator.ControlledGetFileActuator  | get              | CONTROLLED_FILE_SESSION               | Sending files to the master control device                 |
| dialogue.core.actuator.MasterGetsDirActuator      | gets             | MASTER_FILE_SESSION                   | Receive a batch of documents from the controlled equipment |
| dialogue.core.actuator.ControlledGetsDirActuator  | gets             | CONTROLLED_FILE_SESSION               | Send a batch of files to the master control device         |
| dialogue.core.actuator.MasterLookFileActuator     | look             | MASTER_FILE_SESSION                   | View the contents of a file in the controlled device       |
| dialogue.core.actuator.ControlledLookFileActuator | look             | CONTROLLED_FILE_SESSION               | Transfer file data to the master device                    |
| dialogue.core.actuator.MasterPutFileActuator      | put              | MASTER_FILE_SESSION                   | Sending files to controlled equipment                      |
| dialogue.core.actuator.ControlledPutFileActuator  | put              | CONTROLLED_FILE_SESSION               | Receive files from the master control device               |
| dialogue.core.actuator.MasterPutsDirActuator      | puts             | MASTER_FILE_SESSION                   | Send a batch of documents to the controlled equipment      |
| dialogue.core.actuator.ControlledPutsDirActuator  | puts             | CONTROLLED_FILE_SESSION               | Receive a batch of files from the master control device    |

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

Or copy the following startup class source code to the project that has imported dialog to start.

```java
package dialogue.start;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledSession;
import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterSession;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * 启动类
 *
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
                    System.out.println("state >>> 被控会话运行状态布尔值\t:\t" + instance.isRunning());
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

## Session clone

When a session cannot meet the requirements of remote operation, you can use the session cloning technology to clone
many sessions with the same function. The functions of these sessions are the same, but the data between them will not
interfere with each other, so that each session can be connected to a new session separately for new services.

### Master Session clone

Each master session can only connect to one controlled session. When the master is connected to the controlled session,
the master cannot provide connection services for other controlled sessions. Therefore, you can use cloning to clone a
master session, and use the cloned new session to connect to the new controlled session, so that multiple sessions can
be connected at the same time.

As shown below, the master successfully created a new master through cloning, and operated the new master to achieve the
requirement of simultaneous connection.

```java
package dialogue.start;

import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterSession;

import java.net.InetAddress;

/**
 * 测试用例
 *
 * @author zhao
 */
public class Test {

    public static void main(String[] args) {
        // 获取到第一个主控会话对象
        MasterSession instance = MasterFileSession.getInstance();
        // 以第一个会话为原型，克隆出一个新会话，这里的新会话对象与原会话对象互不干扰
        MasterSession instance1 = instance.cloneSession();

        // 启动主控会话 同时提供被控设备的IP和被控会话端口
        // 端口在配置文件中可以进行设置，这里我们设置为了10003
        instance1.start("192.168.1.25", "10003");
        instance.start("192.168.1.15", "10003");

        // 获取到 与 instance 主控连接的被控会话信息
        InetAddress inetAddress = instance.ConnectedControlled();
        if (inetAddress != null) System.out.println(inetAddress);

        // 开始对两个不同的会话 执行打开记事本命令
        String s = instance.runCommand("cmd /c notepad");
        System.out.println("* 执行结果 >>> " + s);
        String s1 = instance1.runCommand("cmd /c notepad");
        System.out.println("* 执行结果 >>> " + s1);
        // 执行 put 命令 将一个文件 远程传输给其中的一个被控
        String s2 = instance.runCommand("put D:\\MyGitHub\\Dialogue\\out\\artifacts\\Dialogue_jar\\conf\\conf.properties conf\\conf.properties");
        System.out.println(s2);

        // 关闭主控会话
        instance.stop();
        instance1.stop();

        // 当主控会话关闭之后，与主控会话连接的被控会话信息也获取不到了
        InetAddress inetAddress1 = instance.ConnectedControlled();
        System.out.println(inetAddress1);
    }
}
```

### Controlled Session clone

When a controlled session is being used, it cannot provide execution services for other masters. It can only serve the
new master when the connection is disconnected this time. Therefore, session cloning needs to be used to enable
controlled services on multiple ports at the same time, so that multiple ports can serve multiple masters at the same
time, realizing the requirement of simultaneous connection

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
```

## configuration file

There is a conf.properties file in the conf directory under the current directory, which is the corresponding
configuration file. The configuration items will be indicated below, and you can refer to the required ones.

| Attribute Name              | Default value | Supported versions | function                                                                                                                 |
|-----------------------------|---------------|--------------------|--------------------------------------------------------------------------------------------------------------------------|
| controlled.port             | 10001         | v1.0               | The port opened when the controlled session starts                                                                       |
| logger.level                | INFO          | v1.0               | Output level of system log data                                                                                          |
| tcp.buffer.max.size         | 65536         | v1.0               | The maximum length of a data packet during data transmission                                                             |
| tcp.file.port               | 10002         | v1.0               | File transfer channel port                                                                                               |
| charset                     | utf-8         | v1.0               | Encoding set used for transmitting and parsing data                                                                      |
| progress.refresh.threshold  | 256           | v1.0               | The refresh threshold of the progress bar when transmitting data. The larger the threshold, the slower the refresh speed |
| progress.compatibility.mode | false         | v1.0               | The compatibility of progress bars during data transmission is set to true to cope with more incompatible progress bars  |
| file.progress.event         | percentage    | v1.0               | The type of progress bar when transferring data. The default is to display the transfer progress by percentage           |
| progress.color.display      | true          | v1.0               | Color display in progress bar. If it is set to true, it can render color for progress bar                                |

