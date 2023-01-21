package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;
import dialogue.Session;
import dialogue.core.controlled.ControlledSession;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;

/**
 * 查看一个目录的解析函数，其中包含获取到被控端文件目录数据的函数实现逻辑。
 * <p>
 * View the parsing function of a directory, which contains the function implementation logic to obtain the file directory data of the controlled end.
 *
 * @author zhao
 */
public class ControlledSeeDirActuator implements Actuator {

    protected final static byte[] FILE_PATH;
    protected final static byte[] SPLIT;
    protected final static byte[] FILE_SIZE;
    protected final static byte[] FILE_WRITABLE;
    protected final static byte[] FILE_NAME;
    protected final static byte[] ERROR_1;
    protected final static byte[] FILE_TYPE;
    protected final static byte[] FILE;
    protected final static byte[] DIR;

    static {
        try {
            FILE_PATH = "filePath: ".getBytes(ConfigureConstantArea.CHARSET);
            SPLIT = "--------------------------------------------------------------------------\n".getBytes(ConfigureConstantArea.CHARSET);
            FILE_SIZE = "file size: ".getBytes(ConfigureConstantArea.CHARSET);
            FILE_WRITABLE = "file writable: ".getBytes(ConfigureConstantArea.CHARSET);
            FILE_NAME = "file Name: ".getBytes(ConfigureConstantArea.CHARSET);
            ERROR_1 = "The directory was obtained, but an error occurred while reading : ".getBytes(ConfigureConstantArea.CHARSET);
            FILE_TYPE = "type: ".getBytes(ConfigureConstantArea.CHARSET);
            FILE = "file\t".getBytes(ConfigureConstantArea.CHARSET);
            DIR = "dir\t".getBytes(ConfigureConstantArea.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown code: " + ConfigureConstantArea.CHARSET);
        }
    }

    protected final Socket accept;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;

    public ControlledSeeDirActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        this.accept = accept;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    protected boolean resolveDirectory(File file, String path, OutputStream outputStream) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            return true;
        } else {
            outputStream.write(FILE_PATH);
            outputStream.write((path + '\n').getBytes(ConfigureConstantArea.CHARSET));
            outputStream.write(SPLIT);
            for (File listFile : files) {
                outputStream.write(FILE_WRITABLE);
                outputStream.write((listFile.canWrite() + "\t").getBytes(ConfigureConstantArea.CHARSET));
                outputStream.write(FILE_TYPE);
                outputStream.write(listFile.isDirectory() ? DIR : FILE);
                outputStream.write(FILE_SIZE);
                outputStream.write((listFile.length() + " byte\t").getBytes(ConfigureConstantArea.CHARSET));
                outputStream.write(FILE_NAME);
                outputStream.write((listFile.getName() + '\n').getBytes(ConfigureConstantArea.CHARSET));
                outputStream.flush();
            }
            outputStream.write(SPLIT);
            outputStream.write(("Number of files read: " + files.length).getBytes(ConfigureConstantArea.CHARSET));
            outputStream.flush();
        }
        return false;
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

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "see-dir";
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
        // look 目标文件，首先要获取到目标文件的数据，并将数据返回
        Socket fileSocket = new Socket(accept.getInetAddress().getHostName(), ConfigureConstantArea.FILE_PORT);
        OutputStream outputStream1 = fileSocket.getOutputStream();
        // 获取到下一个目录
        if (matcher.find()) {
            File file = new File(matcher.group());
            String path = file.getPath();
            ConfigureConstantArea.LOGGER.info(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    // 获取到目录中所有文件的数据，并添加到缓冲区中
                    if (ex(fileSocket, outputStream1, file, path)) return ControlledSession.SEND_TEXT;
                } else {
                    outputStream1.write(("The target is a file, not a directory: " + path).getBytes(ConfigureConstantArea.CHARSET));
                }
            } else {
                outputStream1.write(("No files or directories: " + path).getBytes(ConfigureConstantArea.CHARSET));
            }
        } else {
            // 如果获取不到代表获取当下目录
            File file = new File("./");
            String path = file.getAbsolutePath();
            ConfigureConstantArea.LOGGER.info(path);
            // 获取到目录中所有文件的数据，并添加到缓冲区中
            if (ex(fileSocket, outputStream1, file, path)) return ControlledSession.SEND_TEXT;
        }
        outputStream1.flush();
        outputStream1.close();
        fileSocket.close();
        return ControlledSession.SEND_FILE_ERROR;
    }

    private boolean ex(Socket fileSocket, OutputStream outputStream1, File file, String path) throws IOException {
        try {
            if (resolveDirectory(file, path, outputStream1)) {
                outputStream1.write(ERROR_1);
                outputStream1.write(path.getBytes(ConfigureConstantArea.CHARSET));
            } else {
                outputStream1.flush();
                outputStream1.close();
                fileSocket.close();
                return true;
            }
        } catch (IOException e) {
            outputStream1.write(e.toString().getBytes(ConfigureConstantArea.CHARSET));
        }
        return false;
    }
}
