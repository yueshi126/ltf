package org.fbi.ltf.repository.dao.common;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.fbi.ltf.repository.model.FsLtfAcctDeal;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.common.FsLtfTransAmt;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Administrator on 18-2-23.
 */
public interface CommonMapper {
    @Select("select nvl(sum(t.amount),0) amount from FS_LTF_TICKET_INFO t" +
            " where t.OPER_DATE = #{chkact}" +
            " and t.host_book_flag = '1'" )
    BigDecimal qryTotalAmtByDate(@Param("chkact" ) String chkact);

    @Select("select count(*) from FS_LTF_TICKET_INFO t" +
            " where t.OPER_DATE = #{chkact}" +
            " and t.host_book_flag = '1'" )
    Integer qryCbsTxnCnt(@Param("chkact" ) String chkact);


    /*
    取消与特色流水不一致的交易流水
     */
    @Update("UPDATE FS_LTF_TICKET_INFO INFO\n" +
            " SET HOST_BOOK_FLAG = '2'," +
            "     HOST_CHK_FLAG ='2' " +
            " WHERE OPER_DATE = #{chkact}" +
            "   AND NOT EXISTS (SELECT 1 " +
            "          FROM FS_LTF_CHK_TXN TXN " +
            "         WHERE TXN_DATE =#{chkact}" +
            "           AND INFO.CBS_ACT_SERIAL = TXN.MSG_SN)" )
    Integer cancalCbsTxnt(@Param("chkact" ) String chkact);


    /*
    查询待对账数据 100 条 查询未查询过的单据
     */
    @Select(" select " +
            "       PKID            as pkid,\n" +
            "       BANK_CODE       as bankCode,\n" +
            "       ORDER_NO        as orderNo,\n" +
            "       TRANS_TIME      as transTime,\n" +
            "       AMOUNT          as amount,\n" +
            "       OVERDUE_FINE    as overdueFine,\n" +
            "       BUS_CODE        as busCode,\n" +
            "       ORDER_CHARGES   as orderCharges,\n" +
            "       ACCOUNT_NO      as accountNo,\n" +
            "       BILL_NO         as billNo,\n" +
            "       TICKET_NO       as ticketNo,\n" +
            "       TICKET_TIME     as ticketTime,\n" +
            "       TICKET_AMOUNT   as ticketAmount,\n" +
            "       PAYER_NAME      as payerName,\n" +
            "       ID_CARD         as idCard,\n" +
            "       PHONE_NO        as phoneNo,\n" +
            "       HANDLE_DEPT     as handleDept,\n" +
            "       DRIVE_NO        as driveNo,\n" +
            "       RUN_NO          as runNo,\n" +
            "       PLATE_NO        as plateNo,\n" +
            "       PLATE_COLOR     as plateColor,\n" +
            "       CAR_TYPE        as carType,\n" +
            "       CAR_COLOR       as carColor,\n" +
            "       PARTY           as party,\n" +
            "       PARTY_CARD      as partyCard,\n" +
            "       SALES_NO        as salesNo,\n" +
            "       SALES_NAME      as salesName,\n" +
            "       HOST_BOOK_FLAG  as hostBookFlag,\n" +
            "       HOST_CHK_FLAG   as hostChkFlag,\n" +
            "       QDF_BOOK_FLAG   as qdfBookFlag,\n" +
            "       QDF_CHK_FLAG    as qdfChkFlag,\n" +
            "       BYZD1           as byzd1,\n" +
            "       BYZD2           as byzd2,\n" +
            "       BYZD3           as byzd3,\n" +
            "       OPER_DATE       as operDate,\n" +
            "       OPER_TIME       as operTime,\n" +
            "       BRANCH_ID       as branchId,\n" +
            "       OPERID          as operid,\n" +
            "       CBS_ACT_SERIAL  as cbsActSerial,\n" +
            "       CHK_ACT_DT      as chkActDt,\n" +
            "       BANK_TAKE       as bankTake,\n" +
            "       CALL_DEPT       as callDept,\n" +
            "       NODE            as node,\n" +
            "       AREA_CODE       as areaCode,\n" +
            "       PAY_TYPE        as payType,\n" +
            "       UPLOAD_RLT_FLAG as uploadRltFlag,\n" +
            "       UPLOAD_RLT_DESC as uploadRltDesc\n" +
            "  from FS_LTF_TICKET_INFO t\n" +
            " where t.host_chk_flag = '1'\n" +
            "   and t.qdf_chk_flag is  null\n" +
            "   and rownum < 11  " )
    List<FsLtfTicketInfo> qryTicket100();

    /*
    查询待对账数据 100 条 查询之前查询结果失败的数据
     */
    @Select(" select " +
            "       PKID            as pkid,\n" +
            "       BANK_CODE       as bankCode,\n" +
            "       ORDER_NO        as orderNo,\n" +
            "       TRANS_TIME      as transTime,\n" +
            "       AMOUNT          as amount,\n" +
            "       OVERDUE_FINE    as overdueFine,\n" +
            "       BUS_CODE        as busCode,\n" +
            "       ORDER_CHARGES   as orderCharges,\n" +
            "       ACCOUNT_NO      as accountNo,\n" +
            "       BILL_NO         as billNo,\n" +
            "       TICKET_NO       as ticketNo,\n" +
            "       TICKET_TIME     as ticketTime,\n" +
            "       TICKET_AMOUNT   as ticketAmount,\n" +
            "       PAYER_NAME      as payerName,\n" +
            "       ID_CARD         as idCard,\n" +
            "       PHONE_NO        as phoneNo,\n" +
            "       HANDLE_DEPT     as handleDept,\n" +
            "       DRIVE_NO        as driveNo,\n" +
            "       RUN_NO          as runNo,\n" +
            "       PLATE_NO        as plateNo,\n" +
            "       PLATE_COLOR     as plateColor,\n" +
            "       CAR_TYPE        as carType,\n" +
            "       CAR_COLOR       as carColor,\n" +
            "       PARTY           as party,\n" +
            "       PARTY_CARD      as partyCard,\n" +
            "       SALES_NO        as salesNo,\n" +
            "       SALES_NAME      as salesName,\n" +
            "       HOST_BOOK_FLAG  as hostBookFlag,\n" +
            "       HOST_CHK_FLAG   as hostChkFlag,\n" +
            "       QDF_BOOK_FLAG   as qdfBookFlag,\n" +
            "       QDF_CHK_FLAG    as qdfChkFlag,\n" +
            "       BYZD1           as byzd1,\n" +
            "       BYZD2           as byzd2,\n" +
            "       BYZD3           as byzd3,\n" +
            "       OPER_DATE       as operDate,\n" +
            "       OPER_TIME       as operTime,\n" +
            "       BRANCH_ID       as branchId,\n" +
            "       OPERID          as operid,\n" +
            "       CBS_ACT_SERIAL  as cbsActSerial,\n" +
            "       CHK_ACT_DT      as chkActDt,\n" +
            "       BANK_TAKE       as bankTake,\n" +
            "       CALL_DEPT       as callDept,\n" +
            "       NODE            as node,\n" +
            "       AREA_CODE       as areaCode,\n" +
            "       PAY_TYPE        as payType,\n" +
            "       UPLOAD_RLT_FLAG as uploadRltFlag,\n" +
            "       UPLOAD_RLT_DESC as uploadRltDesc\n" +
            " from  FS_LTF_TICKET_INFO   t\n" +
            " where t.host_chk_flag='1'" +
            " and t.qdf_chk_flag  is  not  null  \n" +
            " and t.qdf_chk_flag not in ('1','8') \n" +
            " and rownum < 11   " )
    List<FsLtfTicketInfo> qryTicketFail100();

    // 网银对账1 根据海博数据更新本地网银的对账日期
    @Update(" update fs_ltf_vch_out lvo\n" +
            "set\n" +
            "lvo.chk_flag='1' ,\n" +
            "lvo.chk_act_dt=#{chkActDt}\n" +
            "lvo.pre_act_serial=#{preActSerial}\n" +
            "where " +
            " exists\n" +
            "(select 1 from Fs_Ltf_Vch_Dzwc lvd \n" +
            "where lvd.order_no = lvo.order_no" +
            " and  lvd.chk_act_dt=#{chkActDt}) " )
    int updateLVOChkActDt(@Param("chkActDt" ) String chkActDt,@Param("preActSerial" ) String preActSerial);


    // 柜面转账
    @Update(" update fs_ltf_ticket_info lti\n" +
            "    set lti.pre_act_serial = #{preActSerial} \n" +
            "  where lti.chk_act_dt = #{chkActDt} \n" +
            "    and lti.qdf_chk_flag in ('1', '8') " )
    int updateTickActDt(@Param("chkActDt" ) String chkActDt,@Param("preActSerial" ) String preActSerial);



    // 获取综合平台对账平之后 的需要转账数据
    @Select("  select t1.area_code as areaCode,\n" +
            "       sum(nvl(t1.payment, 0) + nvl(t1.overdue_fine, 0)) as totalamt\n" +
            " from FS_LTF_VCH_OUT t1\n" +
            " where t1.chk_act_dt = #{chkActDt} \n" +
            " group by t1.area_code \n ")
    public List<FsLtfTransAmt> selectNetAmt(@Param("chkActDt" ) String chkActDt);

    // 获取柜面需要转账数据
    @Select("    select area_code, sum(amount)\n" +
            "    from fs_ltf_ticket_info t\n" +
            "   where t.qdf_chk_flag in ('1', '8')\n" +
            "    and  t.chk_act_dt =  #{chkActDt} \n " +
            "   group by area_code \n")
    public List<FsLtfTransAmt> selectCounterAmt(@Param("chkActDt" ) String chkActDt);
    /*

     */
    @Update("update FS_LTF_VCH_OUT a\n" +
            "   set a.chk_flag = #{chkFlag},\n" +
            "       a.ser_Num  = #{serNum},\n" +
            "       a.bat_date = to_char(sysdate, 'yyyyMMdd')\n" +
            " where chk_act_dt = #{chkActDt}\n" +
            "   and chk_flag = '1'\n ")
    public int updateOutBydateOrderNo(@Param("orderNo") String orderNo, @Param("serNum") String serNum, @Param("chkFlag") String chkFlag);

}
