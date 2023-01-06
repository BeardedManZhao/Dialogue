package dialogue.utils;

import java.io.IOException;

/**
 * 异常事件监听器，该类可以用于异常的监听与回复和获取等操作。
 * <p>
 * Exception event listener, which can be used for monitoring, replying and obtaining exceptions.
 *
 * @author zhao
 */
public class ExceptionProgress implements ProgressEvent<Exception, RuntimeException, IOException> {

    public final static ExceptionProgress NO_ACTION = new ExceptionProgress();

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     * 针对Exception的异常进行捕获并进行处理的函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Exception type) {

    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     * 针对RuntimeException的异常进行捕获并进行处理的函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(RuntimeException type) {

    }

    /**
     * 事件监听逻辑实现三号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     * 针对IO类型的异常进行捕获并进行处理的函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function3(IOException type) {

    }
}
