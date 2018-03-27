package org.fbi.ltf.helper;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Created by ZZP_YY on 2018-03-16.
 */
public class MybatisManager {

    private SqlSessionFactory sessionFactory;

    public MybatisManager(){
        sessionFactory = MybatisFactory.ORACLE.getInstance();
    }

    public SqlSessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
