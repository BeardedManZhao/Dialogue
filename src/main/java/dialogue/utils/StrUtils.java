package dialogue.utils;

import java.util.ArrayList;

/**
 * @author zhao
 */
public final class StrUtils {

    /**
     * 将一个字符串按照某个字符进行拆分
     *
     * @param string 需要被拆分的字符串
     * @param split  拆分时的分隔符
     * @return 按照 split 字符拆分之后的字符串组
     */
    public static String[] splitBy(String string, char split) {
        ArrayList<String> arrayList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c != split) {
                stringBuilder.append(c);
            } else {
                arrayList.add(stringBuilder.toString());
                stringBuilder.delete(0, string.length());
            }
        }
        return arrayList.toArray(new String[0]);
    }

    /**
     * 将一个字符串按照某个字符进行拆分
     *
     * @param string 需要被拆分的字符串
     * @param split  拆分时的分隔符
     * @param length 指定本次拆分出来的结果数组的最大长度
     * @return 按照 split 字符拆分之后的字符串组
     */
    public static String[] splitBy(String string, char split, int length) {
        int length1 = length - 1;
        ArrayList<String> arrayList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c != split) {
                stringBuilder.append(c);
            } else {
                arrayList.add(stringBuilder.toString());
                stringBuilder.delete(0, string.length());
            }
            if (arrayList.size() == length1) {
                arrayList.add(string.substring(i + 1));
                break;
            }
        }
        return arrayList.toArray(new String[0]);
    }

}
