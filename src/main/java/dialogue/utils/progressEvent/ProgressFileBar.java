package dialogue.utils.progressEvent;

import dialogue.ConfigureConstantArea;

/**
 * 文件进度条显示事件监听器，在这里就是针对文件读写进度的显示事件实现类。
 * <p>
 * The file progress bar displays the event listener, which is a display event implementation class for file read/write progress.
 *
 * @author zhao
 */
public class ProgressFileBar extends ProgressFileNumber {
    private final static char s1 = '●';
    private final static char s2 = '─';
    private final static String s3 = "─●";

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Integer type) {
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            System.out.print("\033[32m" + s1);
        } else {
            System.out.println(s1);
        }
    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(Integer type) {
        if (++batch == ConfigureConstantArea.PROGRESS_REFRESH_THRESHOLD) {
            batch = 0;
            System.out.print(s2);
        }
    }

    /**
     * 事件监听逻辑实现三号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function3(Integer type) {
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            System.out.println(s3 + "\033[0m");
        } else {
            System.out.println(s3);
        }
    }
}
