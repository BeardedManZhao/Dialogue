package dialogue.core.controlled.task;

import dialogue.utils.ExceptionProgress;
import dialogue.utils.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 流拷贝线程，支持第三线程数据流拷贝的数据流拷贝组件，其中提供了需要拷贝的数据流构造函数形参，以及异常处理方案。
 * <p>
 * The stream copy thread is a data stream copy component that supports the data stream copy of the third thread. It provides the data stream constructor parameters that need to be copied and exception handling scheme.
 *
 * @author zhao
 */
public final class StreamCopyTask implements Runnable {

    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final boolean close;
    private final ExceptionProgress exceptionProgress;

    /**
     * 构造一个数据流拷贝的任务
     * <p>
     * The task of constructing a data stream copy
     *
     * @param inputStream       数据输入流，是数据来源数据流，是所有数据的来源。
     *                          <p>
     *                          The data input stream is the data source data stream and the source of all data.
     * @param outputStream      数据输出流，也是数据目标数据流，是所有数据的目标地点。
     *                          <p>
     *                          The data output stream is also the data target data stream, which is the target location of all data.
     * @param close             如果设置为true，代表使用数据流传输完毕之后将会关闭数据流
     *                          <p>
     *                          If set to true, the data stream will be closed after the data stream transmission is completed
     * @param exceptionProgress 数据流传输过程中发生了异常时的异常处理时间实现。
     */
    public StreamCopyTask(InputStream inputStream, OutputStream outputStream, boolean close, ExceptionProgress exceptionProgress) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.close = close;
        this.exceptionProgress = exceptionProgress;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        IOUtils.copy(inputStream, outputStream, close, exceptionProgress);
    }
}
