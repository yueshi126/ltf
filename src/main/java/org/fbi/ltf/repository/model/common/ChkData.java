package org.fbi.ltf.repository.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Thinkpad on 2015/12/9.
 */
@XStreamAlias("root")
public class ChkData {
    @XStreamAlias("jtwfgtjf")
    private BodyRecord Body = new BodyRecord();

    public BodyRecord getBody() {
        return Body;
    }

    public void setBody(BodyRecord body) {
        Body = body;
    }

    @XStreamAlias("jtwfgtjf")
    public static class BodyRecord implements Serializable {
        private String orderTotal;
        private String moneyTotal;
        private List<ChkOrder> orders;

        public List<ChkOrder> getOrders() {
            return orders;
        }

        public void setOrders(List<ChkOrder> orders) {
            this.orders = orders;
        }

        public String getOrderTotal() {
            return orderTotal;
        }

        public void setOrderTotal(String orderTotal) {
            this.orderTotal = orderTotal;
        }

        public String getMoneyTotal() {
            return moneyTotal;
        }

        public void setMoneyTotal(String moneyTotal) {
            this.moneyTotal = moneyTotal;
        }

    }
}
