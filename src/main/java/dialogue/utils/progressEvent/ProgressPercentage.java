package dialogue.utils.progressEvent;

import dialogue.ConfigureConstantArea;

/**
 * 文件进度条显示事件监听器，在这里就是针对文件读写进度的显示事件实现类。
 * <p>
 * The file progress bar displays the event listener, which is a display event implementation class for file read/write progress.
 *
 * @author zhao
 */
public class ProgressPercentage extends ProgressFileNumber {

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，在这里的函数作为进度条的起始点
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Integer type) {
        exc("%0", COLOR_YELLOW);
    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，在这里的函数作为进度条的过程事件函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(Integer type) {
        count += type;
        if (++batch == ConfigureConstantArea.PROGRESS_REFRESH_THRESHOLD) {
            batch = 0;
            if (type < maxSize) {
                exc((ConfigureConstantArea.PROGRESS_COMPATIBILITY_MODE ? "Getting data.......\n%" : "%") + ((count / (double) maxSize) * 100), COLOR_YELLOW);
            } else {
                exc("%100", COLOR_YELLOW);
            }
        }
    }

    /**
     * 事件监听逻辑实现三号函数，作为文件读取结束符号的处理，在这里的函数作为进度条的结束点
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function3(Integer type) {
        exc((ConfigureConstantArea.PROGRESS_COMPATIBILITY_MODE ? "Getting data ok !!!!\n%100" : "%100"), COLOR_GREEN);
        clear();
    }
}