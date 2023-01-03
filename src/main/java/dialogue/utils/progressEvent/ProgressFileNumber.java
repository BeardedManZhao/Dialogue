package dialogue.utils.progressEvent;

import dialogue.ConfigureConstantArea;
import dialogue.utils.ProgressEvent;

/**
 * 文件传输事件对象，其中包含一个文件的读取大小显示的功能，可以在文件读取的时候进行已读取数据量的显示。
 * <p>
 * The file transfer event object contains the function of displaying the read size of a file. It can display the read data amount when the file is read.
 *
 * @author zhao
 */
public class ProgressFileNumber implements ProgressEvent<Integer, Integer, Integer> {
    protected final StringBuilder stringBuilder = new StringBuilder(0x40);
    protected int maxSize = ConfigureConstantArea.TCP_BUFFER_MAX_SIZE;
    protected int batch = 0;
    private int count = 0;
    private int count_Str_Size;
    private String Fallback = "";

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 事件监听逻辑实现一号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function1(Integer type) {

    }

    /**
     * 事件监听逻辑实现二号函数，在类中有很多需要实现的函数，这些函数的提供是为了兼顾很多事件的监听，您可以将这些函数放到不同的地方调用
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function2(Integer type) {
        if (batch - (batch++ >> 4 << 4) == 0) {
            exc(type);
        } else {
            count += type;
        }
    }

    /**
     * 事件监听逻辑实现三号函数，作为文件读取结束符号的处理，在这里的函数
     *
     * @param type 来自外界提供的参数，一般是作为数据读取偏移量
     */
    @Override
    public void function3(Integer type) {
        System.out.println("\nSuccessfully read data of [\033[32m" + (count + type) + "\033[0m] bytes in total!");
        count = 0;
        count_Str_Size = 0;
        Fallback = "";
        batch = 0;
        stringBuilder.delete(0, stringBuilder.length());
    }

    /**
     * 更新进度条数据
     *
     * @param type 当前进度条数值中要累加的数值
     */
    private void exc(Integer type) {
        int back_size = count_Str_Size;
        String s = "\033[33m" + (count += type) + " Byte";
        count_Str_Size = s.length();
        int i1 = count_Str_Size - back_size;
        if (i1 != 0) {
            for (int i = 0; i < i1 + 4; ++i) {
                stringBuilder.append('\b');
            }
            Fallback = stringBuilder.toString();
            System.out.print(' ');
        }
        System.out.print(Fallback + s + " \033[0m");
    }
}
