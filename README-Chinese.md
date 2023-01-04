# Dialogue

# 介绍

Dialogue 是一个针对远程控制而制造出来的工具，在该框架内，有很强大的主控功能与被控端容错机制，通过TCP进行远程控制，提供了启动函数，同时也可以手动进行API的调用！

## 什么是主控和被控

顾名思义，主控就是主动控制端设备，代表主动向被控设备发起控制请求的设备，该设备所属会话中能够通过TCP协议向被控设备发送命令。
被控就是被动控制端设备，代表被动接受主控端发送的数据，并进行相应的处理操作，在这类处理操作中，会发生各种各样的情况与结果，由被控端会话来对这些情况进行处理与事件回调，结果回复等操作。

### 主控和被控是如何进行管理的

主控与被控采用惰性实例化的方式，主控与被控皆属于会话对象，均已实现会话接口(dialogue.Session)，由用户决定需要使用的会话对象，当用户获取到一个会话对象之后才会启动实例化的进程，实例化之后就可以由用户进行下一步的调用。

## 什么是会话

会话，是一个设备中的控制台，用于交流的信息，"主控设备"与"被控设备" 成功连接之后就会产生一个唯一会话，这个会话不会被其它设备所干扰，当连接断开后，当前会话将自动关闭，而新会话将根据情况创建或终止。
不论是主控会话还是被控会话，都可以通过getInstance函数获取到，获取到之后需要进行start函数启动会话！。

### 什么是执行器

在会话的执行中，还有一个重要角色就是执行器，执行器是被会话管理的设备，执行器中包含命令的具体执行逻辑，会话将会根据通信内容选择执行器来执行对应的操作，所有的执行器被统一存储与管理，但是不同会话所支持的执行器是不同的，但是用户不需要担心这些，因为执行器由会话管理与使用，用户不需要考虑任何有关执行器的事情。
值得注意的是，执行器被执行器管理者统一存储与管理，为了灵活性，管理者(dialogue.core.actuator.ActuatorManager)中提供了注销与注册新执行器等函数，但这些函数的使用时有一定风险的，请慎重！

### 会话列表

| 会话类型                                           | 会话编号                       | 会话功能                                             |
|------------------------------------------------|----------------------------|--------------------------------------------------|
| dialogue.core.master.MasterSession             | null                       | 主控会话的统一抽象，实现了主控会话生命周期的管理逻辑                       |
| dialogue.core.master.TCPSession                | MASTER_TCP_SESSION         | 主控会话TCP会话，实现了主控远程执行命令的操作逻辑                       |
| dialogue.core.master.MasterFileSession         | MASTER_FILE_SESSION        | 主控会话文件传输会话，拓展于主控TCP会话，有着文件上传和下载的实现，且包含TCP会话的所有功能 |
| dialogue.core.controlled.ControlledSession     | null                       | 被控会话的统一抽象，实现了被控会话的生命周期的管理逻辑                      |
| dialogue.core.controlled.ConsoleSession        | CONTROLLED_CONSOLE_SESSION | 被控会话-终端会话，实现了将终端命令执行于处理的操作逻辑                     |
| dialogue.core.controlled.ControlledFileSession | CONTROLLED_FILE_SESSION    | 被控会话文件传输会话，拓展于被控终端会话，有着文件的接受与传输的实现，包含终端会话的所有功能   |

# 操作示例

使用方式有两种

- 第一个就是直接启动内置实现好的客户端启动类，将启动类启动之后，根据引导就可以实现通过主控会话远程操作被控设备.
- 第二个就是使用内部的API手动调用框架，实现主控会话远程操作被控设备。

## 使用启动类

启动类（dialogue.start.MAIN）是一个简单的API调用实现，用于示例或快捷使用等需求，您可以直接在仓库中获取到已制作好的 Dialogue 的Java程序，也可以直接在源码中启动对应的启动类。
或者将下面的启动类源代码复制到已经导入了dialogue的项目工程中启动。

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

## 使用API

这个方式也就是核心操作，API能够让该技术快速的集成到各类程序中，API操作分为主控和被控两种操作。

### 主控API调用

主控API的调用并不复杂，大致就是获取到主控会话，使用IP和端口启动主控会话，然后就可以直接调用程序了！当程序使用之后，您可以调用stop关闭会话。

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

### 被控API调用

被控会话相较于主控来说要更简单，只是需要注意被控会话启动之后是一个阻塞式的客户端会话，建议使用一个线程单独启动被控会话。 值得注意的是，”主控会话“的stop函数调用之后，”被控会话“
也会一起被stop，这就是会话之间的生命周期管理机制，保持当前连接的稳定性，”被控“ 会随着 ”主控“ 一起被stop，然后 ”被控“ 会重新等待新主控的连接，直到用户显式的调用stop

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

## 配置文件

在当前目录下的 conf 目录中有一个 conf.properties 文件，该文件就是对应的配置文件，配置项将在下面指出，需要的各位可以参考。

| 属性名称                                | 默认数值       | 支持版本 | 功能                                           |
|-------------------------------------|------------|------|----------------------------------------------|
| controlled.port                     | 10001      | v1.0 | 被控会话在启动的时候所打开的端口                             |
| logger.level                        | INFO       | v1.0 | 系统日志数据的输出级别                                  |
| tcp.buffer.max.size                 | 65536      | v1.0 | 数据传输过程中，一个数据包的最大长度                           |
| tcp.file.port                       | 10002      | v1.0 | 文件传输通道端口                                     |
| charset                             | utf-8      | v1.0 | 传输数据与解析数据使用的编码集                              |
| progress.refresh.threshold          | 256        | v1.0 | 传输数据时进度条的刷新阈值，阈值越大，刷新速度越慢                    |
| progress.compatibility.mode         | false      | v1.0 | 传输数据时进度条的兼容情况，设置为true，可以应对更多不兼容进度条的情况        |
| file.progress.event                 | percentage | v1.0 | 传输数据时进度条的类型，默认是按照百分比显示传输进度                   |
| progress.color.display              | true       | v1.0 | 进度条中的颜色显示，如果设置为true可以为进度条渲染颜色                |
