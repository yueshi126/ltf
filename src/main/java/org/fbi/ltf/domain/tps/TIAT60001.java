package org.fbi.ltf.domain.tps;

import org.fbi.ltf.repository.model.FsLtfVchOut;

/**  票据分配回传报文
 * 1	orderNo	订单编号	是	35
 2	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 3	orderDetail	订单主键	是	33
 4	orderCharges	收费项目（多个用逗号隔开）	否	35
 5	bankMare	领票地点标识（各银行提供）	是	20
 6	payment	收费金额	是	最大：99999.99
 7	ticketNo	处罚决定书编号	是	50
 8	ticketCode	违章代码（多个用逗号隔开）	否	35
 9	ticketTime	违章时间	否	yyyy-MM-dd HH:mm:ss
 10	driveNo	证件号码	否	20
 11	payerName	付款人姓名	是	64
 12	dept	机关代码	是	20
 */
public class TIAT60001  extends FsLtfVchOut{

    @Override
    public String toString() {
        return "MsgBody{" +
                "   orderNo='" + super.getOrderNo() + '\'' +
                ",  transTime='" + super.getTransTime() + '\'' +
                ",  orderDetail='" + super.getOrderDetail() + '\'' +
                ",  orderCharges='" + super.getOrderCharges() + '\'' +
                ",  bankMare='" + super.getBankMare() + '\'' +
                ",  payment='" + super.getPayment() + '\'' +
                ",  ticketNo='" + super.getTicketNo() + '\'' +
                ",  ticketCode='" + super.getTicketCode() + '\'' +
                ",  ticketTime='" + super.getTicketTime() + '\'' +
                ",  driveNo='" + super.getDriveNo() + '\'' +
                ",  payerName='" + super.getPayerName() + '\'' +
                ",  dept='" + super.getDept() + '\'' +
                '}';
    }

}
