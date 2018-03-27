package org.fbi.ltf.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.ltf.repository.model.FsLtfTicketItem;
import org.fbi.ltf.repository.model.FsLtfTicketItemExample;

public interface FsLtfTicketItemMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int countByExample(FsLtfTicketItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int deleteByExample(FsLtfTicketItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int deleteByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int insert(FsLtfTicketItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int insertSelective(FsLtfTicketItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    List<FsLtfTicketItem> selectByExample(FsLtfTicketItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    FsLtfTicketItem selectByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByExampleSelective(@Param("record") FsLtfTicketItem record, @Param("example") FsLtfTicketItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByExample(@Param("record") FsLtfTicketItem record, @Param("example") FsLtfTicketItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByPrimaryKeySelective(FsLtfTicketItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TICKET_ITEM
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByPrimaryKey(FsLtfTicketItem record);
}