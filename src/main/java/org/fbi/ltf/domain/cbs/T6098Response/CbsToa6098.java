package org.fbi.ltf.domain.cbs.T6098Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6096Response.CbsToa6096Item;

import java.util.List;

/**
 * Created by zzp on 18-03-07.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6098 {
    /*

     */
    @DataField(seq = 1)
    private String itemNum;
    @DataField(seq = 2)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6098Response.CbsToa6098Item", totalNumberField = "itemNum")
    private List<CbsToa6098Item> items;

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6098Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6098Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CbsToa6098{" +
                " itemNum ='" + itemNum  + '\'' +
                '}';
    }
}
