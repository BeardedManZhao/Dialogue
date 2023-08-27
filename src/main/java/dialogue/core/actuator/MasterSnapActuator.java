package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import zhao.algorithmMagic.exception.OperatorOperationException;
import zhao.algorithmMagic.operands.matrix.ColorMatrix;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 主控端口 让被控拍照并返回对应的序列化数据。
 *
 * @author zhao
 */
public class MasterSnapActuator implements Actuator {

    protected final ServerSocket fileSocket;
    protected final OutputStream outputStream;
    protected final InputStream inputStream;

    public MasterSnapActuator(ServerSocket fileSocket, OutputStream outputStream, InputStream inputStream) {
        this.fileSocket = fileSocket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    /**
     * @return 能够调用该执行器的会话对象编号。
     * <p>
     * The number of the session object that can call the executor.
     */
    @Override
    public int getType() {
        return Session.MASTER_FILE_SESSION;
    }

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "snap";
    }

    /**
     * 返回会话当前运行状态，当一个会话没有在运行的时候，该函数将返回false，一个没有运行中的会话将不具备执行命令与回显数据的能力
     * <p>
     * Returns the current running state of the session. When a session is not running, this function will return false. A session that is not running will not have the ability to execute commands and echo data
     *
     * @return 如果返回true，代表当前会话正在运行中
     * <p>
     * If true is returned, the current session is running
     */
    @Override
    public boolean isRunning() {
        return ActuatorManager.containMasterActuator(this.getName());
    }

    /**
     * @param command 需要执行的命令参数
     *                <p>
     *                Command parameters to be executed
     * @param matcher 命令匹配器，通过该匹配器获取到命令中的所需参数
     *                <p>
     *                Command matcher, through which the required parameters in the command can be obtained
     * @return 运行之后的结果的字符串形式
     * <p>
     * String form of the result after running
     * @throws IOException 异常抛出主要为当运行执行器的过程中，出现了无法提供给对方的异常信息时，异常将会被抛出。
     *                     <p>
     *                     Exception throwing is mainly used to throw exceptions when there is exception information that cannot be provided to the other party during the execution.
     */
    @Override
    public String runActuatorCommand(String command, Matcher matcher) throws IOException {
        // 首先将命令拆开解析
        if (matcher.find()) {
            // 代表命令后面有参数 在这里我们针对第二个参数进行提取
            final String group = matcher.group();
            if ("help".equalsIgnoreCase(group)) {
                // 代表打印当前命令的帮助信息
                return "snap  [可选：p1]\n" +
                        "\tp1   [help：打印帮助信息]\n" +
                        "\t     [path：拍照文件保存路径 ]\n";
            }

            // 其它情况代表被控端 可以开始执行命令了 将命令发送给被控
            outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
            // 接受对方的文件请求
            Socket accept = fileSocket.accept();
            // 启动数据流 接收来自被控的数据 这里应该是 AS 库的序列化数据
            final ObjectInputStream objectInputStream = new ObjectInputStream(accept.getInputStream());
            try {
                // 将序列化数据开始反序列化并转换为图像矩阵
                final ColorMatrix o = (ColorMatrix) objectInputStream.readObject();
                // 保存到指定的路径
                o.save(group);
            } catch (ClassNotFoundException | OperatorOperationException | InvalidClassException e) {
                return e.toString();
            } finally {
                accept.close();
            }
            return "ok!!!!";
        } else {
            // 代表命令后面没有其它参数 因此在这里还是反序列化 但是不进行保存，而是直接展示
            // 其它情况代表被控端 可以开始执行命令了 将命令发送给被控
            outputStream.write(command.getBytes(ConfigureConstantArea.CHARSET));
            // 接受对方的文件请求
            Socket accept = fileSocket.accept();
            // 启动数据流 接收来自被控的数据 这里应该是 AS 库的序列化数据
            final ObjectInputStream objectInputStream = new ObjectInputStream(accept.getInputStream());
            try {
                // 将序列化数据开始反序列化并转换为图像矩阵
                ((ColorMatrix) objectInputStream.readObject()).show("readImage");
                return "ok!!!!";
            } catch (ClassNotFoundException | OperatorOperationException e) {
                return e.toString();
            }
        }
    }
}
