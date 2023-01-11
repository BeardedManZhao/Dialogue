package dialogue.core.result;

/**
 * 结果数据封装类接口，是Dialogue库中所有结果数据的抽象，其中提供了数据结果布尔验证，数据来源，数据产生时间那么几个等待实现的抽象函数
 * <p>
 * The result data encapsulation class interface is the abstraction of all the result data in the Dialog library, which provides several abstract functions waiting to be implemented, such as data result Boolean verification, data source, and data generation time
 *
 * @param <R> 结果对象中封装的结果数据类型
 *            <p>
 *            The result data type encapsulated in the result object
 */
public interface Result<R> {

    /**
     * @return 如果本结果数据中包含是数据符合预期的成功结果，那么返回true，如果此数据封装的是错误信息，那么该函数返回false
     * <p>
     * If the result data contains the expected successful result, then return true. If the data encapsulates error information, then this function returns false
     */
    boolean isSuccessful();

    /**
     * @return 本结果的产生来源，也是获取到本结果的会话对象编号。
     * <p>
     * The source of this result is also the session object number from which this result is obtained.
     */
    short getSourceSession();

    /**
     * @return 本结果的执行时间，结果对象的产生时间毫秒是从结果对象被封装的一刻开始就已经将此参数获取到了，您可以根据该函数返回对应的日期
     * <p>
     * The execution time of this result is the generation time of the result object in milliseconds. This parameter has been obtained from the moment the result object is encapsulated. You can return the corresponding date according to this function
     */
    long getGenerationMS();

    /**
     * @return 本结果中的数据本身，其中包含的就是结果数据，需要注意的是，该函数往往搭配 isSuccessful 函数一起使用，用于判断函数返回的是正确数据还是错误数据。
     * <p>
     * The data in this result itself contains the result data. It should be noted that this function is often used with the isSuccessful function to determine whether the function returns correct data or wrong data.
     */
    R getDataOrError();
}
