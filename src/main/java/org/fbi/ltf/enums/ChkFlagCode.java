package org.fbi.ltf.enums;

import java.util.Hashtable;

/**
 * ҵ���׷�����
 */
public enum ChkFlagCode implements EnumApp {
    flag_1("1", "1-���˳ɹ�"),
    flag_2("2", "2-�ظ�����"),
    flag_3("3", "3-�޴˴���������"),
    flag_4("4", "4-�������"),
    flag_5("5", "5-������������"),
    flag_6("6", "6-����ȷ"),
    flag_7("7", "7-���ɽ���"),
    flag_8("8", "8-��48Сʱ��¼��"),
    flag_9("9", "9-����ƽ̨��¼��"),
    flag_10("10", "10-����ƽ̨�ظ��ɷ�");

    private String code = null;
    private String title = null;
    private static Hashtable<String, ChkFlagCode> aliasEnums;

    ChkFlagCode(String code, String title) {
        this.init(code, title);
    }

    @SuppressWarnings("unchecked")
    private void init(String code, String title) {
        this.code = code;
        this.title = title;
        synchronized (this.getClass()) {
            if (aliasEnums == null) {
                aliasEnums = new Hashtable();
            }
        }
        aliasEnums.put(code, this);
        aliasEnums.put(title, this);
    }

    public static ChkFlagCode valueOfAlias(String alias) {
        return aliasEnums.get(alias);
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public String toRtnMsg() {
        return this.code + "|" + this.title;
    }
}
