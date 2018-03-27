package org.fbi.ltf.repository.dao.common;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.fbi.ltf.repository.model.FsLtfAcctDeal;
import org.fbi.ltf.repository.model.common.FsLtfOutAcct;

import java.util.List;

/**
 * 账务管理.
 * User: zhanrui
 * Date: 20150423
 */
//  参数传入sql 区别大小写，期初我用orderNo ，sql 中用orderno 提示参数not found
public interface FsLtfOutAcctInfoMapper {


    // 获取对账平之后 的需要转账数据
    @Select("  select t3.area_code as inOrgCode  ," +
            "       t3.area_bnak_no  as inAcctNo , " +
            "       nvl(sum(bill_money ),0) as acctMoney ," +
            "       count （*）as  billnum " +
            "  from  " +
            "   FS_LTF_VCH_OUT t1   ," +
            "   FS_LTF_POLICE_ORG_COMP t2 ," +
            "   FS_LTF_AREA_CONFIG t3    " +
            "   where" +
            "       t1.dept = t2.org_code " +   // out 表 机关代码与 polic 表机关代码关联 行政区域号
            "       and  t2.area_code  = t3.area_code " +
            "       and  t1.ser_Num=#{serNum}" +
            "       and  t1.chk_flag='0'" +
            "      group by t3.area_code,t3.area_bnak_no  ")
    public List<FsLtfAcctDeal> selectAcctResult(@Param("serNum") String serNum);

    // 根据订单号修改对账成功out的数据标识
    @Update(" update FS_LTF_VCH_OUT a set a.chk_flag=#{chkFlag} , a.ser_Num =#{serNum} ," +
            "   a.bat_date=to_char(sysdate,'yyyyMMdd') " +
            "   where a.order_no=#{orderNo} and nvl(a.chk_flag,'1') !='3'  ")
    public int updateOutBydateOrderNo(@Param("orderNo") String orderNo, @Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // 根据订单号修改对账成功pos的数据标识
    @Update(" update FS_LTF_ACCT_INFO a set a.chk_flag=#{chkFlag} ,a.ser_Num =#{serNum} " +
            "   where a.order_no=#{orderNo} and nvl(a.chk_flag,'1') !='3'  ")
    public int updatePOSBydateOrderno(@Param("orderNo") String orderNo, @Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // 更新生成转账数据 修改标识
    @Update(" update FS_LTF_VCH_OUT a set a.chk_flag=#{chkFlag} " +
            "   where   nvl(a.chk_flag,'2')='0'  and a.ser_Num =#{serNum} ")
    public int updateVchOut_ChkFlag(@Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // 更新生成转账数据 修改标识
    @Update(" update FS_LTF_ACCT_INFO a set a.chk_flag=#{chkFlag} " +
            "   where   nvl(a.chk_flag,'2')='0'  and a.ser_Num =#{serNum} ")
    public int updateActInfo_ChkFlag(@Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // 增加转账日期，为财政发数据
    @Update(" update FS_LTF_VCH_OUT a set a.BAT_DATE=to_char(sysdate,'yyyymmdd')  " +
            "   where   nvl(a.chk_flag,'1')='3'  and a.ser_Num =#{serNum} and a.AREA_CODE =#{area_code} ")
    public int updateOutbySerNum(@Param("serNum") String serNum, @Param("area_code") String area_code);

    //  todo ODSB取数据 用dblink
    @Update(" insert into  FS_LTF_ACCT_INFO_ODSB (" +
            "    cr_tx_dt, " +        // CR_TX_DT, POS_REF_NO, CR_POS_TX_SQ_NO, EC_FLG, TX_LOG_NO 联合主键
            "    pos_ref_no, " +
            "    cr_pos_tx_sq_no, " +
            "    ec_flg, " +
            "   TX_LOG_NO,  " +
            "    cr_dsc_amt1, " +
            "    cr_dsc_amt2 ,  " +
            "    cr_tx_amt, " +
            "    order_no," +
            "   CR_CONF_FLG ) " +
            " select" +
            "   t.cr_tx_dt, " +
            "   t.pos_ref_no, " +
            "   t.cr_pos_tx_sq_no, " +
            "   t.ec_flg, " +
            "   t.TX_LOG_NO, " +
            "   t.cr_dsc_amt1, " +
            "   t.cr_dsc_amt2 ,  " +
            "   t.cr_tx_amt, " +
            "   t.order_no , " +
            "   CR_CONF_FLG   " +
//            "   from  FS_LTF_ACCT_INFO_ODSB  t " +   // 用odsb的表  BF_EVT_CHN_POS_TRAD@odsbltf
            "   from  BF_EVT_CHN_POS_TRAD@odsbltf  t " +
            "   where t.cr_tx_dt=#{oprDate10}  and t.ec_flg='0'and CR_CONF_FLG='Y' " +
            "   and  POS_REF_NO='1053702601200190006' ")
    // todo pos 号是多少？  bf_evt_chn_pos_tradpos
    public int insertFromPos(@Param("oprDate10") String oprDate10);

    @Update(" insert into  FS_LTF_ACCT_INFO (" +
            "    cr_tx_dt, " +        // CR_TX_DT, POS_REF_NO, CR_POS_TX_SQ_NO, EC_FLG, TX_LOG_NO 联合主键
            "    pos_ref_no, " +
            "    cr_pos_tx_sq_no, " +
            "    ec_flg, " +
            "   TX_LOG_NO,  " +
            "    cr_dsc_amt1, " +
            "    cr_dsc_amt2 ,  " +
            "    cr_tx_amt, " +
            "    order_no," +
            "   CR_CONF_FLG ) " +
            " select" +
            "   t.cr_tx_dt, " +
            "   t.pos_ref_no, " +
            "   t.cr_pos_tx_sq_no, " +
            "   t.ec_flg, " +
            "   t.TX_LOG_NO, " +
            "   t.cr_dsc_amt1, " +
            "   t.cr_dsc_amt2 ,  " +
            "   t.cr_tx_amt, " +
            "   t.order_no , " +
            "   CR_CONF_FLG   " +
            "   from  FS_LTF_ACCT_INFO_ODSB  t " +
            "   where t.cr_tx_dt=#{oprDate10}  " +
            "   and t.order_no not in (" +
            "    select t2.order_no  from  FS_LTF_ACCT_INFO t2 " +
            "    where （nvl(t2.CHK_FLAG,'1')='1' or nvl(t2.CHK_FLAG,'1') ='2' ）" +  // 0 -对账平，3 -生成转账信息了
            "     and t2.CR_TX_DT between #{operDate10Back7} and #{oprDate10} )  ")
    public int insertFromPosTemp(@Param("oprDate10") String oprDate10,@Param("operDate10Back7") String operDate10Back7);


    @Select("  select  " +
            "  order_no as orderno ," +
            "  nvl(sum(cr_tx_amt ),0)   as totalamt ," +
            "    count(*) as orderNum" +
            "  from fs_ltf_acct_info t" +
            "  where " +
            "      ( nvl(t.CHK_FLAG,'1')= '1' or nvl(t.CHK_FLAG,'1')= '2' )" +  // null+1 状态都是 需要重新对账 ，这个没有2状态
//            "    and t.ec_flg ='0' " +     从odsb取数据的时候已经限制了，这就不用了
//            "    and t.CR_CONF_FLG='Y'" +
            "      and t.cr_tx_dt=#{oprDate10}  " +
            "    group by order_no  ")
    public List<FsLtfOutAcct> seletAcctInfo(@Param("oprDate10") String oprDate10);

    // 查询未对账、对账不平、对账订单不在out 数据再次对账
    @Select("  select  " +
            "   order_no as orderno ," +
            "  nvl(sum(bill_money),0) as totalamt ," +    // bill_money 有可能是null  票据传到out初始数据是null后期获取票据成成才会有bill_money
            "  count(*) as orderNum" +
            "  from fs_ltf_vch_out t" +
            "  where " +
            "     order_no =#{orderNo} " +
            " and   (nvl(t.CHK_FLAG,'1') ='1' or  nvl(t.CHK_FLAG,'1') ='2')" +   // 状态1,2 和空--不平、不存在、未对账
            "    group by order_no  ")
    public List<FsLtfOutAcct> selectVchOutByorderNo(@Param("orderNo") String orderNo);
}

