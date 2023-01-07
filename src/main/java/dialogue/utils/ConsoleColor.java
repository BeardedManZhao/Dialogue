package dialogue.utils;

/**
 * 颜色字体常量区，其中存储的就是有关所有颜色的数据字符串，在某些终端中可以有不一样的效果。
 * <p>
 * The color font constant area stores data strings related to all colors, which can have different effects in some terminals.
 *
 * @author zhao
 */
public final class ConsoleColor {

    // ANSI escape code
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public final static String COLOR_YELLOW = "\033[33m";
    public final static String COLOR_GREEN = "\033[32m";
    public final static String COLOR_DEF = "\033[0m";
    public final static String COLOR_NO = "";

}
