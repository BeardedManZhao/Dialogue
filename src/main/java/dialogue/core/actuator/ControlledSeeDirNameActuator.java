package dialogue.core.actuator;

import dialogue.ConfigureConstantArea;

import java.io.*;
import java.net.Socket;

/**
 * 与父类功能一样，但是只会将文件名称记录下来，减少不必要信息的查询。
 * <p>
 * The function of the parent class is the same, but only the file name will be recorded to reduce the query of unnecessary information.
 *
 * @author zhao
 */
public class ControlledSeeDirNameActuator extends ControlledSeeDirActuator {

    protected final static byte[] SPLIT;

    static {
        try {
            SPLIT = "----------------\n".getBytes(ConfigureConstantArea.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown code: " + ConfigureConstantArea.CHARSET);
        }
    }

    public ControlledSeeDirNameActuator(Socket accept, InputStream inputStream, OutputStream outputStream) {
        super(accept, inputStream, outputStream);
    }

    /**
     * @return 该执行器的名称，一般是该执行器能够解析的命令标识。
     * <p>
     * The name of the executor is generally the command ID that the executor can resolve.
     */
    @Override
    public String getName() {
        return "see-dirN";
    }

    @Override
    protected boolean resolveDirectory(File file, String path, OutputStream outputStream) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            return true;
        } else {
            outputStream.write(FILE_PATH);
            outputStream.write((path + '\n').getBytes(ConfigureConstantArea.CHARSET));
            outputStream.write(SPLIT);
            for (File listFile : files) {
                outputStream.write(listFile.isDirectory() ? DIR : FILE);
                outputStream.write((listFile.getName() + '\n').getBytes(ConfigureConstantArea.CHARSET));
                outputStream.flush();
            }
            outputStream.write(SPLIT);
            outputStream.write(("Number of files read: " + files.length).getBytes(ConfigureConstantArea.CHARSET));
            outputStream.flush();
        }
        return false;
    }
}
