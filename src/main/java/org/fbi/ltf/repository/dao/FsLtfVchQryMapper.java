package org.fbi.ltf.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.ltf.repository.model.FsLtfVchQry;
import org.fbi.ltf.repository.model.FsLtfVchQryExample;

public interface FsLtfVchQryMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int countByExample(FsLtfVchQryExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int deleteByExample(FsLtfVchQryExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int deleteByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int insert(FsLtfVchQry record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int insertSelective(FsLtfVchQry record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    List<FsLtfVchQry> selectByExample(FsLtfVchQryExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    FsLtfVchQry selectByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByExampleSelective(@Param("record") FsLtfVchQry record, @Param("example") FsLtfVchQryExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByExample(@Param("record") FsLtfVchQry record, @Param("example") FsLtfVchQryExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByPrimaryKeySelective(FsLtfVchQry record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_QRY
     *
     * @mbggenerated Wed Dec 09 22:01:28 CST 2015
     */
    int updateByPrimaryKey(FsLtfVchQry record);
}