package org.fbi.ltf.enums;

import java.util.Hashtable;

/**
 * 业务交易返回码
 */
public enum ChkFlagCode implements EnumApp {
    flag_1("1", "1-对账成功"),
    flag_2("2", "2-重复对账"),
    flag_3("3", "3-无此处罚决定书"),
    flag_4("4", "4-罚款金额不足"),
    flag_5("5", "5-超过对账期限"),
    flag_6("6", "6-不明确"),
    flag_7("7", "7-滞纳金不足"),
    flag_8("8", "8-民警48小时无录入"),
    flag_9("9", "9-服务平台无录入"),
    flag_10("10", "10-服务平台重复缴费");

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
