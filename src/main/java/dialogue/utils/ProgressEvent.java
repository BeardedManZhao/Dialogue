package dialogue.utils;

import dialogue.utils.progressEvent.ProgressFileBar;
import dialogue.utils.progressEvent.ProgressFileNumber;
import dialogue.utils.progressEvent.ProgressPercentage;

/**
 * 事件接口，目前作为进度事件处理，在远程下载文件等需要进度显示的操作时，该类的逻辑可以被每一批数据启动。
 * <p>
 * The event interface is currently handled as a progress event. When remote downloading files and other operations require progress display, this type of logic can be started by each batch of data.
 *
 * @param <type1> 一号事件函数的形参类型
 * @param <type2> 二号事件函数的形参类型
 * @param <type3> 三号事件函数的形参类型
 * @author 赵凌宇
 */
public interface ProgressEvent<type1, type2, type3> {

    /**
     * 进度条事件监听实现类，在其中第一个函数是进度条的起始操作函数
     * 第二个函数是进度条的过程操作函数
     * 第三个函数是进度条的结束操作函数
     */
    ProgressFileNumber PROGRESS_FILE_BAR = new ProgressFileBar();
    /**
     * 进度条事件监听实现类，在其中第一个函数是进度条的起始操作函数
     * 第二个函数是进度条的过程操作函数
     * 第三个函数是进度条的结束操作函数
     */
    ProgressFileNumber PROGRESS_FILE_NUMBER = new ProgressFileNumber();
    /**
     * 百分比事件监听实现类，在其中第一个函数是进度条的起始操作函数
     * 第二个函数是进度条的过程操作函数
     * 第三个函数是进度条的结束操作函数
     */
    ProgressFileNumber PROGRESS_FILE_PERCENTAGE = new ProgressPercentage();

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    void function1(type1 type);

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    void function2(type2 type);

    /**
     * 事件监听逻辑实现三号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    void function3(type3 type);
}
