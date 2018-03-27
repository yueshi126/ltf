package org.fbi.ltf.domain.cbs.T6081Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6080Request.CbsTia6080Item;

import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6081 {
    @DataField(seq = 1)
    private String itemNum;
    @DataField(seq = 2)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6081Response.CbsToa6081Item", totalNumberField = "itemNum")
    private List<CbsToa6081Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6081Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6081Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CbsToa6081{" +
                " itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}
