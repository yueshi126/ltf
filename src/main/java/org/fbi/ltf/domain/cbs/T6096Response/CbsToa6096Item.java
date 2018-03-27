package org.fbi.ltf.domain.cbs.T6096Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * ��ɫƽ̨�ɿ���Ϣ�ϱ��������ӱ���
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6096Item {
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