package org.fbi.ltf.domain.cbs.T6097Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * 特色平台缴款信息上报请求报文子报文
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsTia6097Item {
    @DataField(seq = 1)
    private String itemCode;
    @DataField(seq = 2)
    private String itemName;
    @DataField(seq = 3)
    private String amount;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CbsTia6010Item{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", amount=" + amount +
                '}';
    }
}