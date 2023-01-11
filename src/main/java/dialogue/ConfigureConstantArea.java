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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 配置常量区，其中存储的都是有关该工具的配置信息。
 * <p>
 * The configuration constant area stores the configuration information about the tool.
 *
 * @author 赵凌宇
 */
public final class ConfigureConstantArea {

    /**
     * 本库的版本编码 x.xx 的形式代表 x.x.x版本，不同版本之间有不同的兼容效果
     * <p>
     * The version code x.xx of this library represents the x.x.x version. Different versions have different compatibility effects
     */
    public final static float VERSION = 1.01f;

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

    /**
     * 文件在远程主机中运行的目录
     * <p>
     * Directory where the file runs on the remote host
     */
    public final static String REMOTE_RUNNING_DIRECTORY = "./exe/";

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(LogFormatter.CONSOLE_HANDLER_1);
    }

    static {
        File ConfFile = new File(CONF_FILE_PATH);
        // 准备初始化配置
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
            PROGRESS_REFRESH_THRESHOLD = Integer.parseInt(properties.getProperty("progress.refresh.threshold", "256"));
            PROGRESS_COMPATIBILITY_MODE = Boolean.parseBoolean(properties.getProperty("progress.compatibility.mode", "false"));
            FILE_PROGRESS_STRING = properties.getProperty("file.progress.event", "percentage");
            PROGRESS_COLOR_DISPLAY = Boolean.parseBoolean(properties.getProperty("progress.color.display", "true"));
            PERSISTENT_SESSION_CHANNEL_PORT = Integer.parseInt(properties.getProperty("persistent.session.channel.port", "10003"));
        }

        boolean levelIsOk = false;

        if ("bar".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_BAR;
        } else if ("number".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_NUMBER;
        } else if ("percentage".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_PERCENTAGE;
        } else if ("logger".equals(FILE_PROGRESS_STRING) && LOGGER_LEVEL.equalsIgnoreCase("info")) {
            FILE_PROGRESS = ProgressEvent.PROGRESS_FILE_LOG;
            LOGGER.setLevel(Level.INFO);
            levelIsOk = true;
        } else if ("null".equals(FILE_PROGRESS_STRING)) {
            FILE_PROGRESS = null;
        } else {
            throw new RuntimeException("Unknown File_Progress: " + LOGGER_LEVEL);
        }
        if (!levelIsOk) {
            // 如果在初始化进度条的时候顺便判断到了日志级别，就不在进行级别设置了
            loadLogger();
        }
        clearExe();
    }

    /**
     * 检查清理或创建远程运行目录，使得远程运行功能能够完美的运行。
     */
    private static void clearExe() {
        // 准备目录
        File file = new File(REMOTE_RUNNING_DIRECTORY);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LOGGER.warning("Initialization failed because the directory required by the project is occupied. Please prepare an empty directory named [./exe/] to store in the project.");
            }
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                LOGGER.warning(REMOTE_RUNNING_DIRECTORY + " Insufficient permissions or not a directory.");
            } else {
                for (File listFile : files) {
                    if (listFile.delete()) {
                        LOGGER.info("clear " + listFile.getPath());
                    }
                }
            }
        }
    }

    /**
     * 根据日志级别参数配置不同的级别配置
     */
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
