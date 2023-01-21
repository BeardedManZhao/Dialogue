package dialogue.core.actuator;

import java.io.OutputStream;
import java.net.ServerSocket;

/**
 * @author zhao
 */
public class MasterSeeDirNameActuator extends MasterSeeDirActuator {

    public MasterSeeDirNameActuator(ServerSocket fileSocket, OutputStream outputStream) {
        super(fileSocket, outputStream);
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
}
