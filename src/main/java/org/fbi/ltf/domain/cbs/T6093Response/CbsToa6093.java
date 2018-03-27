package org.fbi.ltf.domain.cbs.T6093Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6093 {
    @DataField(seq = 1)
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "CbsToa6093{" +
                " remark='" + remark + '\'' +
                '}';
    }
}
