package dialogue.core.result;

/**
 * 字符串结果数据封装类，在其中存储着字符串结果，能够获取到运行时构造的结果数据。
 * <p>
 * String result data encapsulation class, in which string results are stored, and the result data constructed at runtime can be obtained.
 *
 * @author zhao
 */
public class StringResult extends StatefulResults<String> {

    protected final String dataOrError;

    public StringResult(boolean successful, short sourceSession, String data) {
        super(successful, sourceSession);
        this.dataOrError = data;
    }

    /**
     * @return 本结果中的数据本身，其中包含的就是结果数据，需要注意的是，该函数往往搭配 isSuccessful 函数一起使用，用于判断函数返回的是正确数据还是错误数据。
     * <p>
     * The data in this result itself contains the result data. It should be noted that this function is often used with the isSuccessful function to determine whether the function returns correct data or wrong data.
     */
    @Override
    public String getDataOrError() {
        return this.dataOrError;
    }
}
