package org.fbi.ltf.repository.dao.common;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.fbi.ltf.repository.model.FsLtfAcctDeal;
import org.fbi.ltf.repository.model.common.FsLtfOutAcct;

import java.util.List;

/**
 * �������.
 * User: zhanrui
 * Date: 20150423
 */
//  ��������sql �����Сд���ڳ�����orderNo ��sql ����orderno ��ʾ����not found
public interface FsLtfOutAcctInfoMapper {


    // ��ȡ����ƽ֮�� ����Ҫת������
    @Select("  select t3.area_code as inOrgCode  ," +
            "       t3.area_bnak_no  as inAcctNo , " +
            "       nvl(sum(bill_money ),0) as acctMoney ," +
            "       count ��*��as  billnum " +
            "  from  " +
            "   FS_LTF_VCH_OUT t1   ," +
            "   FS_LTF_POLICE_ORG_COMP t2 ," +
            "   FS_LTF_AREA_CONFIG t3    " +
            "   where" +
            "       t1.dept = t2.org_code " +   // out �� ���ش����� polic ����ش������ ���������
            "       and  t2.area_code  = t3.area_code " +
            "       and  t1.ser_Num=#{serNum}" +
            "       and  t1.chk_flag='0'" +
            "      group by t3.area_code,t3.area_bnak_no  ")
    public List<FsLtfAcctDeal> selectAcctResult(@Param("serNum") String serNum);

    // ���ݶ������޸Ķ��˳ɹ�out�����ݱ�ʶ
    @Update(" update FS_LTF_VCH_OUT a set a.chk_flag=#{chkFlag} , a.ser_Num =#{serNum} ," +
            "   a.bat_date=to_char(sysdate,'yyyyMMdd') " +
            "   where a.order_no=#{orderNo} and nvl(a.chk_flag,'1') !='3'  ")
    public int updateOutBydateOrderNo(@Param("orderNo") String orderNo, @Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // ���ݶ������޸Ķ��˳ɹ�pos�����ݱ�ʶ
    @Update(" update FS_LTF_ACCT_INFO a set a.chk_flag=#{chkFlag} ,a.ser_Num =#{serNum} " +
            "   where a.order_no=#{orderNo} and nvl(a.chk_flag,'1') !='3'  ")
    public int updatePOSBydateOrderno(@Param("orderNo") String orderNo, @Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // ��������ת������ �޸ı�ʶ
    @Update(" update FS_LTF_VCH_OUT a set a.chk_flag=#{chkFlag} " +
            "   where   nvl(a.chk_flag,'2')='0'  and a.ser_Num =#{serNum} ")
    public int updateVchOut_ChkFlag(@Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // ��������ת������ �޸ı�ʶ
    @Update(" update FS_LTF_ACCT_INFO a set a.chk_flag=#{chkFlag} " +
            "   where   nvl(a.chk_flag,'2')='0'  and a.ser_Num =#{serNum} ")
    public int updateActInfo_ChkFlag(@Param("serNum") String serNum, @Param("chkFlag") String chkFlag);
    // ����ת�����ڣ�Ϊ����������
    @Update(" update FS_LTF_VCH_OUT a set a.BAT_DATE=to_char(sysdate,'yyyymmdd')  " +
            "   where   nvl(a.chk_flag,'1')='3'  and a.ser_Num =#{serNum} and a.AREA_CODE =#{area_code} ")
    public int updateOutbySerNum(@Param("serNum") String serNum, @Param("area_code") String area_code);

    //  todo ODSBȡ���� ��dblink
    @Update(" insert into  FS_LTF_ACCT_INFO_ODSB (" +
            "    cr_tx_dt, " +        // CR_TX_DT, POS_REF_NO, CR_POS_TX_SQ_NO, EC_FLG, TX_LOG_NO ��������
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
//            "   from  FS_LTF_ACCT_INFO_ODSB  t " +   // ��odsb�ı�  BF_EVT_CHN_POS_TRAD@odsbltf
            "   from  BF_EVT_CHN_POS_TRAD@odsbltf  t " +
            "   where t.cr_tx_dt=#{oprDate10}  and t.ec_flg='0'and CR_CONF_FLG='Y' " +
            "   and  POS_REF_NO='1053702601200190006' ")
    // todo pos ���Ƕ��٣�  bf_evt_chn_pos_tradpos
    public int insertFromPos(@Param("oprDate10") String oprDate10);

    @Update(" insert into  FS_LTF_ACCT_INFO (" +
            "    cr_tx_dt, " +        // CR_TX_DT, POS_REF_NO, CR_POS_TX_SQ_NO, EC_FLG, TX_LOG_NO ��������
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
            "    where ��nvl(t2.CHK_FLAG,'1')='1' or nvl(t2.CHK_FLAG,'1') ='2' ��" +  // 0 -����ƽ��3 -����ת����Ϣ��
            "     and t2.CR_TX_DT between #{operDate10Back7} and #{oprDate10} )  ")
    public int insertFromPosTemp(@Param("oprDate10") String oprDate10,@Param("operDate10Back7") String operDate10Back7);


    @Select("  select  " +
            "  order_no as orderno ," +
            "  nvl(sum(cr_tx_amt ),0)   as totalamt ," +
            "    count(*) as orderNum" +
            "  from fs_ltf_acct_info t" +
            "  where " +
            "      ( nvl(t.CHK_FLAG,'1')= '1' or nvl(t.CHK_FLAG,'1')= '2' )" +  // null+1 ״̬���� ��Ҫ���¶��� �����û��2״̬
//            "    and t.ec_flg ='0' " +     ��odsbȡ���ݵ�ʱ���Ѿ������ˣ���Ͳ�����
//            "    and t.CR_CONF_FLG='Y'" +
            "      and t.cr_tx_dt=#{oprDate10}  " +
            "    group by order_no  ")
    public List<FsLtfOutAcct> seletAcctInfo(@Param("oprDate10") String oprDate10);

    // ��ѯδ���ˡ����˲�ƽ�����˶�������out �����ٴζ���
    @Select("  select  " +
            "   order_no as orderno ," +
            "  nvl(sum(bill_money),0) as totalamt ," +    // bill_money �п�����null  Ʊ�ݴ���out��ʼ������null���ڻ�ȡƱ�ݳɳɲŻ���bill_money
            "  count(*) as orderNum" +
            "  from fs_ltf_vch_out t" +
            "  where " +
            "     order_no =#{orderNo} " +
            " and   (nvl(t.CHK_FLAG,'1') ='1' or  nvl(t.CHK_FLAG,'1') ='2')" +   // ״̬1,2 �Ϳ�--��ƽ�������ڡ�δ����
            "    group by order_no  ")
    public List<FsLtfOutAcct> selectVchOutByorderNo(@Param("orderNo") String orderNo);
}

