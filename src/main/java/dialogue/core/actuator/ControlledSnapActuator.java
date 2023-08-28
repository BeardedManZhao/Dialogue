package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import zhao.algorithmMagic.exception.OperatorOperationException;
import zhao.algorithmMagic.io.InputCamera;
import zhao.algorithmMagic.io.InputCameraBuilder;
import zhao.algorithmMagic.io.InputComponent;
import zhao.algorithmMagic.operands.matrix.ColorMatrix;
import zhao.algorithmMagic.operands.table.SingletonCell;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * @author zhao
 */
public class ControlledSnapActuator implements Actuator {

    protected final Socket accept;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;
    protected final InputComponent inputCamera;

    {
        // 初始化相机组件
        this.inputCamera = InputCamera.builder()
                // 设置需要调用的摄像头索引数值
                .addInputArg(InputCameraBuilder.Camera_Index, SingletonCell.$(0))
                // 设置拍摄图像数据时需要使用的格式
                .addInputArg(InputCameraBuilder.Image_Format, SingletonCell.$("JPG"))
                // 设置拍摄图像的尺寸比例
                .addInputArg(InputCameraBuilder.CUSTOM_VIEW_SIZES, SingletonCell.$("VGA"))
                .create();
    }

    public ControlledSnapActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        this.accept = accept;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * @return 能够调用该执行器的会话对象编号。
     * <p>
     * The number of the session object that can call the executor.
     */
    @Override
    public int getType() {
        return Session.CONTROLLED_FILE_SESSION;
    }

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
        return ActuatorManager.containControlledActuator(this.getName());
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
        // 准备数据流
        Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileSocket.getOutputStream());
        // 首先判断相机组件初始化是否成功
        if (!this.inputCamera.open()) {
            // 若不成功就返回错误数据
            return "inputCamera.isOpen() == false";
        }
        // 在这里直接进行拍照
        try {
            final ColorMatrix parse = ColorMatrix.parse(inputCamera);
            // 成功就开始序列化
            objectOutputStream.writeObject(parse);
            return "ok!!!!";
        } catch (OperatorOperationException e) {
            // 若不成功就返回错误数据
            objectOutputStream.writeUTF(e.toString());
            return e.toString();
        } finally {
            this.inputCamera.close();
        }
    }
}
