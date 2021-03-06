package org.fbi.ltf.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.ltf.repository.model.FsLtfVchOutItem;
import org.fbi.ltf.repository.model.FsLtfVchOutItemExample;

public interface FsLtfVchOutItemMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int countByExample(FsLtfVchOutItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int deleteByExample(FsLtfVchOutItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int deleteByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int insert(FsLtfVchOutItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int insertSelective(FsLtfVchOutItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    List<FsLtfVchOutItem> selectByExample(FsLtfVchOutItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    FsLtfVchOutItem selectByPrimaryKey(String pkid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int updateByExampleSelective(@Param("record") FsLtfVchOutItem record, @Param("example") FsLtfVchOutItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int updateByExample(@Param("record") FsLtfVchOutItem record, @Param("example") FsLtfVchOutItemExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int updateByPrimaryKeySelective(FsLtfVchOutItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_OUT_ITEM
     *
     * @mbggenerated Wed Apr 11 17:49:28 CST 2018
     */
    int updateByPrimaryKey(FsLtfVchOutItem record);
}