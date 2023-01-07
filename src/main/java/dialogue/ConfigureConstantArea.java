package dialogue;

import dialogue.utils.IOUtils;
import dialogue.utils.ProgressEvent;
import dialogue.utils.loggerFormatter.LogFormatter;
import dialogue.utils.progressEvent.ProgressFileNumber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
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
    public final static int CONTROLLED_PORT;
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
    public final static Scanner SCANNER;
    /**
     * 文件传输端口，默认是10002
     */
    public final static int FILE_PORT;
    /**
     * 文件传输时的事件处理器，如果这里设置为null，代表不需要任何事件操作
     */
    public final static ProgressFileNumber FILE_PROGRESS;
    public final static String FILE_PROGRESS_STRING;
    /**
     * 进度条刷新阈值，也称之为刷新批次，当数据包传递次数达到这个数值的时候，将会刷新一次进度条数据
     * <p>
     * The progress bar refresh threshold, also known as the refresh batch, will refresh the progress bar data once when the number of packet transfers reaches this value
     */
    public final static int PROGRESS_REFRESH_THRESHOLD;
    public final static boolean PROGRESS_COMPATIBILITY_MODE;
    public final static boolean PROGRESS_COLOR_DISPLAY;
    /**
     * 持久会话通道端口，在进行交互式的命令时，需要长时间的交互式命令，这个时候需要持久会话，该会话将会开启单独的一个端口进行服务。
     * <p>
     * The persistent session channel port requires a long-term interactive command when conducting interactive commands. In this case, a persistent session is required, and a separate port will be opened for service.
     */
    public final static int PERSISTENT_SESSION_CHANNEL_PORT;

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(LogFormatter.CONSOLE_HANDLER_1);
    }

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
            IOUtils.close(fileReader);
            CONTROLLED_PORT = Integer.parseInt(properties.getProperty("controlled.port", "10001"));
            LOGGER_LEVEL = properties.getProperty("logger.level", "INFO");
            TCP_BUFFER_MAX_SIZE = Integer.parseInt(properties.getProperty("tcp.buffer.max.size", "65535"));
            FILE_PORT = Integer.parseInt(properties.getProperty("tcp.file.port", "10002"));
            CHARSET = properties.getProperty("charset", "utf-8");
            SCANNER = new Scanner(System.in, ConfigureConstantArea.CHARSET);
            PROGRESS_REFRESH_THRESHOLD = Integer.parseInt(properties.getProperty("progress.refresh.threshold", "256"));
            PROGRESS_COMPATIBILITY_MODE = Boolean.parseBoolean(properties.getProperty("progress.compatibility.mode", "false"));
            FILE_PROGRESS_STRING = properties.getProperty("file.progress.event", "percentage");
            PROGRESS_COLOR_DISPLAY = Boolean.parseBoolean(properties.getProperty("progress.color.display", "true"));
            PERSISTENT_SESSION_CHANNEL_PORT = Integer.parseInt(properties.getProperty("persistent.session.channel.port", "10003"));
        }
        if ("bar".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_BAR;
        } else if ("number".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_NUMBER;
        } else if ("percentage".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_PERCENTAGE;
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
