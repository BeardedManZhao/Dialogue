package dialogue;

import dialogue.utils.ProgressEvent;
import dialogue.utils.progressEvent.ProgressFileNumber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 配置常量区，其中存储的都是有关该工具的配置信息。
 * <p>
 * The configuration constant area stores the configuration information about the tool.
 *
 * @author zhao
 */
public final class ConfigureConstantArea {
    /**
     * 配置文件目录
     */
    public final static String CONF_FILE_PATH = "./conf/conf.properties";

    /**
     * 被控设备端口
     */
    public final static int Controlled_PORT;

    /**
     * 主控设备端口
     */
    public final static int MASTER_PORT;

    /**
     * 工具全局日志对象
     */
    public final static Logger LOGGER = Logger.getLogger("dialogue");

    /**
     * 工具全局日志界别对象
     */
    public final static String LOGGER_LEVEL;

    /**
     * 主控设备向被控设发送的数据包最大值
     */
    public final static int TCP_BUFFER_MAX_SIZE;

    /**
     * 通信文字编码
     */
    public final static String CHARSET;

    /**
     * 文件传输端口，默认是10002
     */
    public final static int FILE_PORT;

    /**
     * 文件传输时的事件处理器，如果这里设置为null，代表不需要任何事件操作
     */
    public final static ProgressFileNumber FILE_PROGRESS;
    public final static String FILE_PROGRESS_STRING;

    static {
        File ConfFile = new File(CONF_FILE_PATH);
        FileReader fileReader = null;
        Properties properties = new Properties();
        try {
            fileReader = new FileReader(ConfFile);
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "The configuration file [" + ConfFile.getAbsolutePath() + "] does not exist. Use the default configuration.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Configuration file load error.");
            throw new RuntimeException(e);
        } finally {
            Controlled_PORT = Integer.parseInt(properties.getProperty("controlled.port", "10001"));
            LOGGER_LEVEL = properties.getProperty("logger.level", "INFO");
            MASTER_PORT = Integer.parseInt(properties.getProperty("master.port", "38243"));
            TCP_BUFFER_MAX_SIZE = Integer.parseInt(properties.getProperty("tcp.buffer.max.size", "65535"));
            FILE_PORT = Integer.parseInt(properties.getProperty("tcp.file.port", "10002"));
            CHARSET = properties.getProperty("charset", "utf-8");
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        FILE_PROGRESS_STRING = properties.getProperty("file.progress.event", "bar");
        if ("bar".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_BAR;
        } else if ("number".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_NUMBER;
        } else if ("null".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = null;
        } else {
            throw new RuntimeException("Unknown File_Progress: " + LOGGER_LEVEL);
        }
        loadLogger();
    }

    private static void loadLogger() {
        if ("info".equalsIgnoreCase(LOGGER_LEVEL)) {
            LOGGER.setLevel(Level.INFO);
        } else if ("all".equalsIgnoreCase(LOGGER_LEVEL)) {
            LOGGER.setLevel(Level.ALL);
        } else if ("warn".equalsIgnoreCase(LOGGER_LEVEL)) {
            LOGGER.setLevel(Level.WARNING);
        } else if ("error".equalsIgnoreCase(LOGGER_LEVEL)) {
            LOGGER.setLevel(Level.SEVERE);
        } else {
            throw new RuntimeException("Unknown log level: " + LOGGER_LEVEL);
        }
    }
}
