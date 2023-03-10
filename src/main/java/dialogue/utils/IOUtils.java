package dialogue.utils;

import dialogue.ConfigureConstantArea;
import dialogue.utils.progressEvent.ProgressFileNumber;

import java.io.*;

/**
 * IO工具包
 *
 * @author 赵凌宇
 */
public final class IOUtils {

    /**
     * 获取到输入流中的所有字符串数据
     *
     * @param inputStream 需要被提取的输入流
     * @return 输入流中的所有字符串数据
     * @throws IOException 加载异常
     */
    public static String getStringByStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[65535];
        int offset;
        while ((offset = inputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, offset);
        }
        return byteArrayOutputStream.toString(ConfigureConstantArea.CHARSET);
    }

    /**
     * 关闭一个数据流
     *
     * @param closeable 需要被关闭的目标数据流对象
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将两个数据流中的数据进行拷贝
     *
     * @param inputStream  源数据流
     * @param outputStream 目标数据输出流
     * @param CloseStream  输入流被提取完成后是否需要关闭，如果设置为true，代表提取之后自动关闭数据流
     * @throws IOException 数据拷贝异常！
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean CloseStream) throws IOException {
        copy(inputStream, outputStream, CloseStream, null);
    }

    /**
     * 指定数据量通知具备进度条展示的方式进行数据流的拷贝，的放肆将两个数据流中的数据进行拷贝
     *
     * @param localFileSize       本次拷贝的数据量
     * @param bufferedInputStream 源数据流
     * @param dataOutputStream    目标数据流
     * @param fileProgress        文件传输进度条对象
     * @throws IOException 数据里拷贝是出现异常的异常对象抛出
     */
    public static void copy(long localFileSize, InputStream bufferedInputStream, OutputStream dataOutputStream, ProgressFileNumber fileProgress) throws IOException {
        if (fileProgress != null) {
            fileProgress.function1(0);
            byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            int offset;
            while ((offset = bufferedInputStream.read(buffer)) > 0) {
                fileProgress.function2(offset);
                dataOutputStream.write(buffer, 0, offset);
                if ((localFileSize -= offset) <= 0) {
                    break;
                }
            }
            fileProgress.function3(0);
        } else {
            byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            int offset;
            while ((offset = bufferedInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, offset);
                if ((localFileSize -= offset) <= 0) {
                    break;
                }
            }
        }
    }

    /**
     * 将两个数据流中的数据进行拷贝，这个是带有异常处理能力的数据传输方式。
     *
     * @param inputStream       源数据流
     * @param outputStream      目标数据输出流
     * @param CloseStream       输入流被提取完成后是否需要关闭，如果设置为true，代表提取之后自动关闭数据流
     * @param exceptionProgress 出错逻辑实现，当数据流操作时出现了异常，异常将会传递给事件类中对应的函数内进行处理
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean CloseStream, ExceptionProgress exceptionProgress) {
        try {
            byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            int offset;
            while ((offset = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, offset);
            }
            if (CloseStream) {
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            if (exceptionProgress != null) {
                exceptionProgress.function3(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将两个数据流中的数据进行拷贝，这个是带有异常处理能力的数据传输方式。
     *
     * @param inputStream       源数据流
     * @param outputStream      目标数据输出流
     * @param CloseStream       输入流被提取完成后是否需要关闭，如果设置为true，代表提取之后自动关闭数据流
     * @param exceptionProgress 出错逻辑实现，当数据流操作时出现了异常，异常将会传递给事件类中对应的函数内进行处理
     * @param StartColor        每一批数据的ANSI颜色
     * @param EndColor          每一批数据的ANSI结束颜色
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean CloseStream, ExceptionProgress exceptionProgress, String StartColor, String EndColor) {
        try {
            byte[] buffer = new byte[ConfigureConstantArea.TCP_BUFFER_MAX_SIZE];
            byte[] bytes1 = StartColor.getBytes(ConfigureConstantArea.CHARSET);
            byte[] bytes2 = EndColor.getBytes(ConfigureConstantArea.CHARSET);
            int offset;
            while ((offset = inputStream.read(buffer)) > 0) {
                outputStream.write(bytes1);
                outputStream.write(buffer, 0, offset);
                outputStream.write(bytes2);
            }
            if (CloseStream) {
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            if (exceptionProgress != null) {
                exceptionProgress.function3(e);
            } else {
                e.printStackTrace();
            }
        }
    }
}
