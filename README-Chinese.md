# ![image](https://user-images.githubusercontent.com/113756063/226080848-2e26ca28-3840-43a9-a356-307fc41e0918.png) Dialogue

# 介绍

Dialogue 是一个针对远程控制而制造出来的工具，在该框架内，有很强大的主控功能与被控端容错机制，通过TCP进行远程控制，提供了启动函数，同时也可以手动进行API的调用！

## 如何获取API

您可以通过maven坐标获取到本工具库的依赖，当然也可以使用gradle连接该坐标。

- MAVEN依赖

```xml

<dependency>
    <groupId>io.github.BeardedManZhao</groupId>
    <artifactId>dialogue</artifactId>
    <version>1.0.3</version>
</dependency>
```

- Gradle依赖

```gradle
dependencies {
    implementation 'io.github.BeardedManZhao:dialogue:1.0.3'
}
```

## 什么是主控和被控

顾名思义，主控就是主动控制端设备，代表主动向被控设备发起控制请求的设备，该设备所属会话中能够通过TCP协议向被控设备发送命令。
被控就是被动控制端设备，代表被动接受主控端发送的数据，并进行相应的处理操作，在这类处理操作中，会发生各种各样的情况与结果，由被控端会话来对这些情况进行处理与事件回调，结果回复等操作。

### 主控和被控是如何进行管理的

主控与被控采用惰性实例化的方式，主控与被控皆属于会话对象，均已实现会话接口(dialogue.Session)，由用户决定需要使用的会话对象，当用户获取到一个会话对象之后才会启动实例化的进程，实例化之后就可以由用户进行下一步的调用。

## 什么是会话

会话，是一个设备中的控制台，用于交流的信息，"主控设备"与"被控设备" 成功连接之后就会产生一个唯一会话，这个会话不会被其它设备所干扰，当连接断开后，当前会话将自动关闭，而新会话将根据情况创建或终止。
不论是主控会话还是被控会话，都可以通过getInstance函数获取到，获取到之后需要进行start函数启动会话！。

### 会话列表

| 会话类型                                                 | 会话编号                          | 会话功能                                             |
|------------------------------------------------------|-------------------------------|--------------------------------------------------|
| dialogue.core.master.MasterSession                   | null                          | 主控会话的统一抽象，实现了主控会话生命周期的管理逻辑                       |
| dialogue.core.master.TCPSession                      | MASTER_TCP_SESSION            | 主控会话TCP会话，实现了主控远程执行命令的操作逻辑                       |
| dialogue.core.master.MasterFileSession               | MASTER_FILE_SESSION           | 主控会话文件传输会话，拓展于主控TCP会话，有着文件上传和下载的实现，且包含TCP会话的所有功能 |
| dialogue.core.master.MasterPersistentSession         | MASTER_PERSISTENT_SESSION     | 主控持久会话，能够进行具有交互需求的命令操作，需要注意的是该会话并不具备文件传输会话的功能    |
| dialogue.core.controlled.ControlledSession           | null                          | 被控会话的统一抽象，实现了被控会话的生命周期的管理逻辑                      |
| dialogue.core.controlled.ConsoleSession              | CONTROLLED_CONSOLE_SESSION    | 被控会话-终端会话，实现了将终端命令执行于处理的操作逻辑                     |
| dialogue.core.controlled.ControlledFileSession       | CONTROLLED_FILE_SESSION       | 被控会话文件传输会话，拓展于被控终端会话，有着文件的接受与传输的实现，包含终端会话的所有功能   |
| dialogue.core.controlled.ControlledPersistentSession | CONTROLLED_PERSISTENT_SESSION | 被控会话持久会话，能够实时的监听本地终端运行的所有数据并将数据实时传递给主控持久会话       |

## 什么是执行器

在会话的执行中，还有一个重要角色就是执行器，执行器是被会话管理的设备，执行器中包含命令的具体执行逻辑，会话将会根据通信内容选择执行器来执行对应的操作，所有的执行器被统一存储与管理，但是不同会话所支持的执行器是不同的，但是用户不需要担心这些，因为执行器由会话管理与使用，用户不需要考虑任何有关执行器的事情。
值得注意的是，执行器被执行器管理者统一存储与管理，为了灵活性，管理者(dialogue.core.actuator.ActuatorManager)中提供了注销与注册新执行器等函数，但这些函数的使用时有一定风险的，请慎重！

### 执行器列表

| 执行器类型                                                   | 执行器命令    | 支持版本   | 执行器所属会话                 | 执行器功能               |
|---------------------------------------------------------|----------|--------|-------------------------|---------------------|
| dialogue.core.actuator.MasterGetFileActuator            | get      | v1.0.0 | MASTER_FILE_SESSION     | 从被控设备接收文件           |
| dialogue.core.actuator.ControlledGetFileActuator        | get      | v1.0.0 | CONTROLLED_FILE_SESSION | 向主控设备发送文件           |
| dialogue.core.actuator.MasterGetsDirActuator            | gets     | v1.0.0 | MASTER_FILE_SESSION     | 从被控设备接收一批文件         |
| dialogue.core.actuator.ControlledGetsDirActuator        | gets     | v1.0.0 | CONTROLLED_FILE_SESSION | 向主控设备发送一批文件         |
| dialogue.core.actuator.MasterLookFileActuator           | look     | v1.0.0 | MASTER_FILE_SESSION     | 查看被控设备中的某个文件内容      |
| dialogue.core.actuator.ControlledLookFileActuator       | look     | v1.0.0 | CONTROLLED_FILE_SESSION | 将文件数据传递给主控设备        |
| dialogue.core.actuator.MasterPutFileActuator            | put      | v1.0.0 | MASTER_FILE_SESSION     | 向被控设备发送文件           |
| dialogue.core.actuator.ControlledPutFileActuator        | put      | v1.0.0 | CONTROLLED_FILE_SESSION | 接收来自主控设备的文件         |
| dialogue.core.actuator.MasterPutsDirActuator            | puts     | v1.0.0 | MASTER_FILE_SESSION     | 向被控设备发送一批文件         |
| dialogue.core.actuator.ControlledPutsDirActuator        | puts     | v1.0.0 | CONTROLLED_FILE_SESSION | 接收来自主控设备的一批文件       |
| dialogue.core.actuator.MasterRunningProgramActuator     | running  | v1.0.1 | MASTER_FILE_SESSION     | 将程序文件传递给远程主机运行并接收结果 |
| dialogue.core.actuator.ControlledRunningProgramActuator | running  | v1.0.1 | CONTROLLED_FILE_SESSION | 接收到程序文件并运行，然后返回结果   |
| dialogue.core.actuator.MasterSeeDirActuator             | see-dir  | v1.0.2 | MASTER_FILE_SESSION     | 查询被控设备中的目录结构        |
| dialogue.core.actuator.ControlledSeeDirActuator         | see-dir  | v1.0.2 | CONTROLLED_FILE_SESSION | 按照主控的需求解析目录结构并返回结果  |
| dialogue.core.actuator.MasterSeeDirNameActuator         | see-dirN | v1.0.2 | MASTER_FILE_SESSION     | 查询被控设备中的目录中的所有文件名   |
| dialogue.core.actuator.ControlledSeeDirNameActuator     | see-dirN | v1.0.2 | CONTROLLED_FILE_SESSION | 按照主控的需求解析目录文件名并返回结果 |

# 操作示例

使用方式有两种

- 第一个就是直接启动内置实现好的客户端启动类，将启动类启动之后，根据引导就可以实现通过主控会话远程操作被控设备.
- 第二个就是使用内部的API手动调用框架，根据API调用实现通过主控会话远程操作被控设备。

## 使用启动类

启动类（dialogue.start.MAIN）是一个简单的API调用实现，用于示例或快捷使用等需求，您可以直接在仓库中获取到已制作好的 Dialogue 的Java程序，也可以直接在源码中启动对应的启动类。
或者将下面的启动类源代码复制到已经导入了dialogue的项目工程中启动。

```java
package dialogue.start;

import dialogue.ConfigureConstantArea;
import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledPersistentSession;
import dialogue.core.controlled.ControlledSession;
import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterPersistentSession;
import dialogue.core.master.MasterSession;
import dialogue.utils.ConsoleColor;

import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * 1.0.1 启动类
 *
 * @author 赵凌宇
 */
public final class MAIN {
    private final static Scanner SCANNER = new Scanner(System.in, ConfigureConstantArea.CHARSET);
    private static boolean status = true;

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0) {
            if (args.length > 1 && "master".equalsIgnoreCase(args[1])) {
                ConfigureConstantArea.LOGGER.log(Level.INFO, "dialogue 示例PC客户端版本：" + ConfigureConstantArea.VERSION);
                ConfigureConstantArea.LOGGER.log(Level.INFO, "Master主控会话配置.......");
                Thread.sleep(1024);
                System.out.print("* 操控设备的IP地址 >>> ");
                String ip = SCANNER.nextLine();
                System.out.print("* 操控设备的端口号 >>> ");
                String port = SCANNER.nextLine();
                Thread.sleep(1024);
                MasterSession instance = "false".equalsIgnoreCase(args[0]) ? MasterFileSession.getInstance() : MasterPersistentSession.getInstance();
                instance.start(ip, port);
                if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
                    String colorCommand = "* >>> " + ConsoleColor.COLOR_CYAN;
                    while (status) {
                        System.out.print(colorCommand);
                        String command = SCANNER.nextLine();
                        System.out.print(ConsoleColor.COLOR_DEF);
                        runCommand(instance, command);
                    }
                    instance.stop();
                } else {
                    while (status) {
                        System.out.print("* >>> ");
                        String command = SCANNER.nextLine();
                        runCommand(instance, command);
                    }
                    instance.stop();
                }
            } else {
                ConfigureConstantArea.LOGGER.log(Level.INFO, "默认被控会话系统启动.......");
                ConfigureConstantArea.LOGGER.log(Level.INFO, "根据配置文件打开被控端口：" + ConfigureConstantArea.CONTROLLED_PORT);
                Thread.sleep(1024);
                ControlledSession instance = "false".equalsIgnoreCase(args[0]) ? ControlledFileSession.getInstance() : ControlledPersistentSession.getInstance();
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
        } else {
            System.out.println("""
                    您需要传递参数 【持久会话】 【会话身份】
                    \t【持久会话】 ：true或false，代表您是否要使用持久会话，如果不需要进行持久会话的交互，您可以传递false。
                    \t【会话身份】 ：master或其它，代表您是否要使用主控会话，如果您不想要使用主控会话，这里不需要传递参数。""");
        }
    }

    private static void runCommand(MasterSession instance, String command) throws InterruptedException {
        if ("exit".equalsIgnoreCase(command)) {
            status = false;
            return;
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
            return;
        }
        System.out.println(instance.runCommand(command));
        Thread.sleep(512);
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
 * @author 赵凌宇
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
 * @author 赵凌宇
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

## 会话克隆

当一个会话无法满足远程操作的需求的时候，您可以使用会话克隆技术将相同功能的会话克隆出很多个，这些会话的功能是相同的，但是之间的数据是不会互相有干扰的，使得每一个会话都可以单独连接一个新会话，能够进行新服务。

### 主控会话克隆

每一个主控会话只能连接一个被控会话，当主控与被控会话进行连接的时候，主控将无法为其它被控会话提供连接服务，因此您可以使用克隆，将一个主控会话克隆出来，使用克隆的新会话去连接新被控，就可以达到多个会话进行同时连接的效果了。
如下面所示，主控通过克隆，成功创建出了一个新的主控，并操作新的被控，实现了同时连接的需求。

```java
package dialogue.start;

import dialogue.core.master.MasterFileSession;
import dialogue.core.master.MasterSession;

import java.net.InetAddress;

/**
 * 测试用例
 *
 * @author 赵凌宇
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

### 被控会话克隆

当一个被控会话正在被使用的时候，其无法为其它主控提供执行服务，只能等到本次连接断开，才可以为新主控进行服务，因此需要使用到会话克隆，在多个端口同时开启被控服务，使得多个端口同时为多个主控进行服务，实现了同时连接的需求

```java
package dialogue.start;

import dialogue.core.controlled.ControlledFileSession;
import dialogue.core.controlled.ControlledSession;

/**
 * 测试用例
 *
 * @author 赵凌宇
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

### 创建持久会话

在运行终端命令和需求的时候，往往会涉及到一些持久同时需要交互的命令，这类命令的执行，需要一个适合的会话对象去进行结果与数据实时的传递，在库中有这样的一个会话，叫做持久会话
持久会话具有交互性命令的执行优势，例如进行“cmd”命令等需要交互的操作，在这里想要创建一个持久会话，您可以参照下面的例子进行调用。

#### 主控持久会话

主控持久会话担当了实时接收与实时监听被控持久会话的日志传递职责，持久会话的运行周期大致如下所示

- 获取到持久会话对象，此时将会使用 PERSISTENT_SESSION_CHANNEL_PORT 接收端口建立持久会话通信的服务
- 调用start函数启动持久会话对象，此时将会立刻与被控设备建立联系，同时启动好接收会话端口的服务，等待被控准备。
- 当被控准备就绪之后start函数结束，主控与被控已经连接，开始执行持久交互命令
- 命令被发送之后，进入持久会话界面，此时将可以实时的看到被控的终端页面，也可以实时操作
- 持久交互命令结束，持久会话也会结束，主控发送“::exit”命令结束持久会话，runCommand函数到此结束
- 调用主控持久会话的stop函数，终止主控持久会话，此时被控与主控治安的来凝结将断开

```java
package dialogue.start;

import dialogue.core.master.MasterPersistentSession;
import dialogue.core.master.MasterSession;

import java.io.IOException;

/**
 * 测试用例
 *
 * @author 赵凌宇
 */
public class Test1 {

    public static void main(String[] args) {
        // 获取到持久会话对象
        MasterSession instance = MasterPersistentSession.getInstance();
        // 设置持久会话对象在运行长命令时，数据实时传递的数据流，这里我们设置终端数据流
        // 代表只将持久命令运行时数据与传递数据的位置设置在终端中 TODO 是1.0.1版本中的新功能
        instance.setInputStream(System.in);
        instance.setOutputStream(System.out);
        // 开始启动持久会话对象，启动之后就可以像其它会话一样输入命令了
        // 在执行命令的时候，往返的数据会实时的传递给您传入的数据IO流
        instance.start("127.0.0.1", "10001");
        if (instance.isRunning()) {
            // 执行一个长会话命令 打开cmd终端 注意这个时候会被阻塞，开启持久会话的信息传递
            String s = instance.runCommand("cmd");
            System.out.println(s);
        }
        instance.stop();
    }
}
```

#### 被控持久会话

被控持久会话担当了命令执行时的数据实时获取与发送，交互命令子进程的生命周期管理，主控命令的实时接收与子进程传递，能够实时的接收主控服务 有关被控持久会话的操作方式与生命周期如下所示。

- 获取到被控持久会话对象，此时控制请求接收服务也会被创建
- 调用start函数启动被控会话，会话此时将启动接收服务，同时进入阻塞状态，等待主控的连接
- 连接成功之后会接收来自主控的持久命令，并将命令传递给子进程去处理，同时开始实时数据流传输服务
- 持久交互结束之后，将会进入无状态模式，等待主控发送“::exit”命令关闭本次持久会话
- 持久会话断开之后，会继续接收下一个持久命令，直到调用stop函数
- stop函数的调用将会断开被控持久会话的请求接收服务，关闭请求接收端口，终止被控会话的生命周期。

```java
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
```

## 配置文件

在当前目录下的 conf 目录中有一个 conf.properties 文件，该文件就是对应的配置文件，配置项将在下面指出，需要的各位可以参考。

| 属性名称                            | 默认数值       | 支持版本 | 功能                                    |
|---------------------------------|------------|------|---------------------------------------|
| controlled.port                 | 10001      | v1.0 | 被控会话在启动的时候所打开的端口                      |
| logger.level                    | INFO       | v1.0 | 系统日志数据的输出级别                           |
| tcp.buffer.max.size             | 65536      | v1.0 | 数据传输过程中，一个数据包的最大长度                    |
| tcp.file.port                   | 10002      | v1.0 | 文件传输通道端口                              |
| charset                         | utf-8      | v1.0 | 传输数据与解析数据使用的编码集                       |
| progress.refresh.threshold      | 256        | v1.0 | 传输数据时进度条的刷新阈值，阈值越大，刷新速度越慢             |
| progress.compatibility.mode     | false      | v1.0 | 传输数据时进度条的兼容情况，设置为true，可以应对更多不兼容进度条的情况 |
| file.progress.event             | percentage | v1.0 | 传输数据时进度条的类型，默认是按照百分比显示传输进度            |
| progress.color.display          | true       | v1.0 | 进度条中的颜色显示，如果设置为true可以为进度条渲染颜色         |
| persistent.session.channel.port | 10003      | v1.0 | 持久会话传输端口，是持久会话中数据交互的主要端口              |

<hr>

- 切换到 [English document](https://github.com/BeardedManZhao/Dialogue)
