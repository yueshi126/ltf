package org.fbi.ltf.domain.cbs.T6020Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * 特色平台网络票据打印响应报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6020 {
    @DataField(seq = 1)
    private String remark;
    @DataField(seq = 2)
    private String itemNum;
    @DataField(seq = 3)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020Item", totalNumberField = "itemNum")
    private List<CbsToa6020Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6020Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6020Item> items) {
        this.items = items;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "CbsToa6020{" +
                ", itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}