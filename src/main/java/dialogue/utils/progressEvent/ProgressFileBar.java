package dialogue.utils.progressEvent;

/**
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
        System.out.print("\033[32m" + s1);
    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(Integer type) {
        if (batch - (batch++ >> 4 << 4) == 0) {
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
        System.out.println(s3 + "\033[0m");
    }
}
