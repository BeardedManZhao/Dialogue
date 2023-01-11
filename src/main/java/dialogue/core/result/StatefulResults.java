package dialogue.core.result;

/**
 * 有状态的结果数据封装类，其中的 isSuccessful 函数是生效的，能够起到判断结果中的构造是否运行成功的效果。
 * <p>
 * Stateful result data encapsulation class, in which the isSuccessful function is effective, can judge whether the construction in the result runs successfully.
 *
 * @param <R> 结果对象中封装的结果数据类型
 *            <p>
 *            The result data type encapsulated in the result object
 * @author zhao
 */
public abstract class StatefulResults<R> implements Result<R> {

    protected final boolean Successful;
    protected final short SourceSessionNum;
    protected final long GenerationTimeMS;

    protected StatefulResults(boolean successful, short sourceSession) {
        Successful = successful;
        SourceSessionNum = sourceSession;
        GenerationTimeMS = System.currentTimeMillis();
    }

    /**
     * @return 如果本结果数据中包含是数据符合预期的成功结果，那么返回true，如果此数据封装的是错误信息，那么该函数返回false
     * <p>
     * If the result data contains the expected successful result, then return true. If the data encapsulates error information, then this function returns false
     */
    @Override
    public boolean isSuccessful() {
        return this.Successful;
    }

    /**
     * @return 本结果的产生来源，也是获取到本结果的会话对象编号。
     * <p>
     * The source of this result is also the session object number from which this result is obtained.
     */
    @Override
    public short getSourceSession() {
        return this.SourceSessionNum;
    }

    /**
     * @return 本结果的执行时间，结果对象的产生时间毫秒是从结果对象被封装的一刻开始就已经将此参数获取到了，您可以根据该函数返回对应的日期
     * <p>
     * The execution time of this result is the generation time of the result object in milliseconds. This parameter has been obtained from the moment the result object is encapsulated. You can return the corresponding date according to this function
     */
    @Override
    public long getGenerationMS() {
        return this.GenerationTimeMS;
    }
}
