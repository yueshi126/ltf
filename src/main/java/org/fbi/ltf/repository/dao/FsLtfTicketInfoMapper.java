package org.fbi.ltf.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;

public interface FsLtfTicketInfoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int countByExample(FsLtfTicketInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int deleteByExample(FsLtfTicketInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int deleteByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int insert(FsLtfTicketInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int insertSelective(FsLtfTicketInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    List<FsLtfTicketInfo> selectByExample(FsLtfTicketInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    FsLtfTicketInfo selectByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int updateByExampleSelective(@Param("record") FsLtfTicketInfo record, @Param("example") FsLtfTicketInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int updateByExample(@Param("record") FsLtfTicketInfo record, @Param("example") FsLtfTicketInfoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int updateByPrimaryKeySelective(FsLtfTicketInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_INFO
     *
     * @mbggenerated Tue Mar 27 10:27:47 CST 2018
     */
    int updateByPrimaryKey(FsLtfTicketInfo record);
}