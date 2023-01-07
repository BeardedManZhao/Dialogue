package dialogue.utils.loggerFormatter;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 日志格式对象，其中包含最基本的日志格式信息
 * <p>
 * Log format object, which contains the most basic log format information
 */
public final class LogFormatter extends Formatter {

    public final static char LB = '[';
    public final static char RB = ']';
    public final static char TAB = '\t';
    public final static char line = '\n';
    public final static ConsoleHandler CONSOLE_HANDLER_1 = new ConsoleHandler();
    private final static Date date = new Date();

    static {
        CONSOLE_HANDLER_1.setFormatter(new LogFormatter());
    }

    @Override
    public String format(LogRecord record) {
        date.setTime(record.getMillis());
        return LB + record.getLoggerName() + RB +
                LB + date + RB +
                LB + record.getLevel() + RB +
                TAB + record.getMessage() + line;
    }
}