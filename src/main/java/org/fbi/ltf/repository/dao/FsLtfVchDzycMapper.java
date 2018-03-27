package org.fbi.ltf.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.ltf.repository.model.FsLtfVchDzyc;
import org.fbi.ltf.repository.model.FsLtfVchDzycExample;

public interface FsLtfVchDzycMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int countByExample(FsLtfVchDzycExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int deleteByExample(FsLtfVchDzycExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int insert(FsLtfVchDzyc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int insertSelective(FsLtfVchDzyc record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    List<FsLtfVchDzyc> selectByExample(FsLtfVchDzycExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int updateByExampleSelective(@Param("record") FsLtfVchDzyc record, @Param("example") FsLtfVchDzycExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_VCH_DZYC
     *
     * @mbggenerated Fri Mar 11 14:21:38 CST 2016
     */
    int updateByExample(@Param("record") FsLtfVchDzyc record, @Param("example") FsLtfVchDzycExample example);
}