package org.fbi.ltf.domain.cbs.T6082Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;


import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6082 {
    @DataField(seq = 1)
    private String tip;
    @DataField(seq = 2)
    private String itemNum;
    @DataField(seq = 3)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6082Response.CbsToa6082Item", totalNumberField = "itemNum")
    private List<CbsToa6082Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6082Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6082Item> items) {
        this.items = items;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    @Override
    public String toString() {
        return "CbsToa6082{" +
                " itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}
