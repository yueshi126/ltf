package org.fbi.ltf.domain.cbs.T6022Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020Item;

import java.util.List;

/**
 * 特色平台网络票据打印响应报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6022 {
    @DataField(seq = 1)
    private String remark;
    @DataField(seq = 2)
    private String itemNum;
    @DataField(seq = 3)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6022Response.CbsToa6022Item", totalNumberField = "itemNum")
    private List<CbsToa6022Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6022Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6022Item> items) {
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
        return "CbsToa6022{" +
                ", itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}