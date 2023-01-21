package dialogue.utils.progressEvent;

import dialogue.ConfigureConstantArea;
import dialogue.utils.ConsoleColor;

/**
 * 日志进度条展示，在该进度显示器中，能够通过日志实时的展示出当前读取文件的效果，需要注意的是，本进度器需要在日志级别为 info 的情况下使用，否则将不会生效。
 * <p>
 * Log progress bar display. In this progress display, the effect of the current read file can be displayed in real time through the log. It should be noted that this scheduler needs to be used when the log level is info, otherwise it will not take effect.
 *
 * @author zhao
 */
public class ProgressFileLog extends ProgressPercentage {

    private final static String CURRENT_READ_FILE_BYTES = "\tCurrent read file bytes: ";

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，在这里的函数作为进度条的起始点
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Integer type) {
        ConfigureConstantArea.LOGGER.info(getLog("0%", count, ConsoleColor.ANSI_YELLOW));
    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，在这里的函数作为进度条的过程事件函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(Integer type) {
        if (++batch == ConfigureConstantArea.PROGRESS_REFRESH_THRESHOLD) {
            batch = 0;
            ConfigureConstantArea.LOGGER.info(getLog(percentageString(count += type), count, ConsoleColor.ANSI_YELLOW));
        } else {
            count += type;
        }
    }

    /**
     * 事件监听逻辑实现三号函数，作为文件读取结束符号的处理，在这里的函数作为进度条的结束点
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function3(Integer type) {
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            ConfigureConstantArea.LOGGER.info(getLog("100%", count, ConsoleColor.ANSI_GREEN));
        }
        clear();
    }

    /**
     * 获取到进度条数据
     *
     * @param percentageString 百分比字符串
     * @param size             本次要显示的字节数
     * @param color1           本条日志的颜色
     * @return 符合要求的字符串数据对象
     */
    protected final String getLog(String percentageString, long size, String color1) {
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            return color1 + percentageString + CURRENT_READ_FILE_BYTES + size + " / " + maxSize + " byte" + ConsoleColor.ANSI_RESET;
        } else {
            return percentageString + CURRENT_READ_FILE_BYTES + size + " / " + maxSize + " byte";
        }
    }
}
