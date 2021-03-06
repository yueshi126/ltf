package org.fbi.ltf.repository.model;

import java.util.ArrayList;
import java.util.List;

public class FsLtfTpdeptExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public FsLtfTpdeptExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andOrgdeptIsNull() {
            addCriterion("ORGDEPT is null");
            return (Criteria) this;
        }

        public Criteria andOrgdeptIsNotNull() {
            addCriterion("ORGDEPT is not null");
            return (Criteria) this;
        }

        public Criteria andOrgdeptEqualTo(String value) {
            addCriterion("ORGDEPT =", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptNotEqualTo(String value) {
            addCriterion("ORGDEPT <>", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptGreaterThan(String value) {
            addCriterion("ORGDEPT >", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptGreaterThanOrEqualTo(String value) {
            addCriterion("ORGDEPT >=", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptLessThan(String value) {
            addCriterion("ORGDEPT <", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptLessThanOrEqualTo(String value) {
            addCriterion("ORGDEPT <=", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptLike(String value) {
            addCriterion("ORGDEPT like", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptNotLike(String value) {
            addCriterion("ORGDEPT not like", value, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptIn(List<String> values) {
            addCriterion("ORGDEPT in", values, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptNotIn(List<String> values) {
            addCriterion("ORGDEPT not in", values, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptBetween(String value1, String value2) {
            addCriterion("ORGDEPT between", value1, value2, "orgdept");
            return (Criteria) this;
        }

        public Criteria andOrgdeptNotBetween(String value1, String value2) {
            addCriterion("ORGDEPT not between", value1, value2, "orgdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptIsNull() {
            addCriterion("NEWDEPT is null");
            return (Criteria) this;
        }

        public Criteria andNewdeptIsNotNull() {
            addCriterion("NEWDEPT is not null");
            return (Criteria) this;
        }

        public Criteria andNewdeptEqualTo(String value) {
            addCriterion("NEWDEPT =", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptNotEqualTo(String value) {
            addCriterion("NEWDEPT <>", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptGreaterThan(String value) {
            addCriterion("NEWDEPT >", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptGreaterThanOrEqualTo(String value) {
            addCriterion("NEWDEPT >=", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptLessThan(String value) {
            addCriterion("NEWDEPT <", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptLessThanOrEqualTo(String value) {
            addCriterion("NEWDEPT <=", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptLike(String value) {
            addCriterion("NEWDEPT like", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptNotLike(String value) {
            addCriterion("NEWDEPT not like", value, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptIn(List<String> values) {
            addCriterion("NEWDEPT in", values, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptNotIn(List<String> values) {
            addCriterion("NEWDEPT not in", values, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptBetween(String value1, String value2) {
            addCriterion("NEWDEPT between", value1, value2, "newdept");
            return (Criteria) this;
        }

        public Criteria andNewdeptNotBetween(String value1, String value2) {
            addCriterion("NEWDEPT not between", value1, value2, "newdept");
            return (Criteria) this;
        }

        public Criteria andDeptNameIsNull() {
            addCriterion("DEPT_NAME is null");
            return (Criteria) this;
        }

        public Criteria andDeptNameIsNotNull() {
            addCriterion("DEPT_NAME is not null");
            return (Criteria) this;
        }

        public Criteria andDeptNameEqualTo(String value) {
            addCriterion("DEPT_NAME =", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameNotEqualTo(String value) {
            addCriterion("DEPT_NAME <>", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameGreaterThan(String value) {
            addCriterion("DEPT_NAME >", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameGreaterThanOrEqualTo(String value) {
            addCriterion("DEPT_NAME >=", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameLessThan(String value) {
            addCriterion("DEPT_NAME <", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameLessThanOrEqualTo(String value) {
            addCriterion("DEPT_NAME <=", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameLike(String value) {
            addCriterion("DEPT_NAME like", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameNotLike(String value) {
            addCriterion("DEPT_NAME not like", value, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameIn(List<String> values) {
            addCriterion("DEPT_NAME in", values, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameNotIn(List<String> values) {
            addCriterion("DEPT_NAME not in", values, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameBetween(String value1, String value2) {
            addCriterion("DEPT_NAME between", value1, value2, "deptName");
            return (Criteria) this;
        }

        public Criteria andDeptNameNotBetween(String value1, String value2) {
            addCriterion("DEPT_NAME not between", value1, value2, "deptName");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("REMARK is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("REMARK is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("REMARK =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("REMARK <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("REMARK >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("REMARK >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("REMARK <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("REMARK <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("REMARK like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("REMARK not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("REMARK in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("REMARK not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("REMARK between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("REMARK not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andIscanclIsNull() {
            addCriterion("ISCANCL is null");
            return (Criteria) this;
        }

        public Criteria andIscanclIsNotNull() {
            addCriterion("ISCANCL is not null");
            return (Criteria) this;
        }

        public Criteria andIscanclEqualTo(String value) {
            addCriterion("ISCANCL =", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclNotEqualTo(String value) {
            addCriterion("ISCANCL <>", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclGreaterThan(String value) {
            addCriterion("ISCANCL >", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclGreaterThanOrEqualTo(String value) {
            addCriterion("ISCANCL >=", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclLessThan(String value) {
            addCriterion("ISCANCL <", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclLessThanOrEqualTo(String value) {
            addCriterion("ISCANCL <=", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclLike(String value) {
            addCriterion("ISCANCL like", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclNotLike(String value) {
            addCriterion("ISCANCL not like", value, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclIn(List<String> values) {
            addCriterion("ISCANCL in", values, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclNotIn(List<String> values) {
            addCriterion("ISCANCL not in", values, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclBetween(String value1, String value2) {
            addCriterion("ISCANCL between", value1, value2, "iscancl");
            return (Criteria) this;
        }

        public Criteria andIscanclNotBetween(String value1, String value2) {
            addCriterion("ISCANCL not between", value1, value2, "iscancl");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated do_not_delete_during_merge Wed Dec 16 16:48:05 CST 2015
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table FIS.FS_LTF_TPDEPT
     *
     * @mbggenerated Wed Dec 16 16:48:05 CST 2015
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}