package dialogue.utils.progressEvent;

import dialogue.ConfigureConstantArea;
import dialogue.utils.ConsoleColor;
import dialogue.utils.ProgressEvent;

import static dialogue.utils.ConsoleColor.*;

/**
 * 文件传输事件对象，其中包含一个文件的读取大小显示的功能，可以在文件读取的时候进行已读取数据量的显示。
 * <p>
 * The file transfer event object contains the function of displaying the read size of a file. It can display the read data amount when the file is read.
 *
 * @author 赵凌宇
 */
public class ProgressFileNumber implements ProgressEvent<Integer, Integer, Integer> {
    protected static final StringBuilder stringBuilder = new StringBuilder(0x40);
    protected double maxSize = ConfigureConstantArea.TCP_BUFFER_MAX_SIZE;
    protected int batch = 0;
    protected long count = 0;
    private int count_Str_Size = COLOR_YELLOW.length();
    private String Fallback = "";
    private String temp;

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
        temp = "byte / " + maxSize + "byte";
    }

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，在这里的函数作为进度条的起始点
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Integer type) {

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
            if (ConfigureConstantArea.PROGRESS_COMPATIBILITY_MODE) {
                exc(((count / maxSize) * 100) + "%\n" + (count += type) + temp, COLOR_YELLOW);
            } else {
                exc((count += type) + temp, COLOR_YELLOW);
            }
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
        exc('\n' + ((count / maxSize) * 100) + "%\n" + (count += type) + temp, COLOR_GREEN);
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            System.out.println("\nSuccessfully read data of [\033[32m" + (count + type) + "\033[0m] bytes in total!");
        } else {
            System.out.println("\nSuccessfully read data of [" + (count + type) + "] bytes in total!");
        }
        clear();
    }

    protected void clear() {
        count = 0;
        count_Str_Size = 0;
        Fallback = COLOR_NO;
        batch = 0;
        setMaxSize(ConfigureConstantArea.TCP_BUFFER_MAX_SIZE);
        stringBuilder.delete(0, stringBuilder.length());
    }

    /**
     * 更新进度条数据
     *
     * @param type  当前进度条要显示的新数据
     * @param color 本次打印需要使用的颜色字符串
     * @see ConsoleColor 您可以在这里找到对应的颜色字符串
     */
    protected void exc(String type, String color) {
        String s;
        if (ConfigureConstantArea.PROGRESS_COLOR_DISPLAY) {
            s = Fallback + color + type + COLOR_DEF;
        } else {
            s = Fallback + type;
        }
        exc2(s, count_Str_Size);
        System.out.print(s);
    }

    private void exc2(String type, int back_size) {
        count_Str_Size = type.length();
        int i1 = count_Str_Size - back_size;
        if (i1 != 0) {
            for (int i = 0; i < i1 + 8; ++i) {
                stringBuilder.append('\b');
            }
            Fallback = stringBuilder.toString();
        }
    }
}
