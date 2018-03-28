package org.fbi.ltf.domain.cbs.T6099Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * Created by zzp on 18-03-21.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6099 {
    /*
 itemNum
      */
    @DataField(seq = 1)
    private String itemNum;
    @DataField(seq = 2)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6099Response.CbsToa6099Item", totalNumberField = "itemNum")
    private java.util.List<CbsToa6099Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6099Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6099Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CbsToa6099{" +
                " itemNum ='" + itemNum  + '\'' +
                '}';
    }
}
