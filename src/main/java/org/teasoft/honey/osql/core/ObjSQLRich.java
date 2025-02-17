/*
 * Copyright 2013-2022 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.FunctionType;
import org.teasoft.bee.osql.IncludeType;
import org.teasoft.bee.osql.MapSql;
import org.teasoft.bee.osql.MapSqlKey;
import org.teasoft.bee.osql.MapSqlSetting;
import org.teasoft.bee.osql.MapSuid;
import org.teasoft.bee.osql.ObjSQLException;
import org.teasoft.bee.osql.ObjToSQLRich;
import org.teasoft.bee.osql.OrderType;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.exception.BeeErrorGrammarException;
import org.teasoft.bee.osql.exception.BeeIllegalParameterException;
import org.teasoft.honey.osql.name.NameUtil;
import org.teasoft.honey.util.SuidHelper;

/**
 * SuidRich实现类.Suidrich implementation class.
 * @author Kingstar
 * @since  1.0
 */
public class ObjSQLRich extends ObjSQL implements SuidRich {

	private ObjToSQLRich objToSQLRich; 
	
	private static final String SELECT_SQL = "select SQL: ";
	private static final String SELECT_JSON_SQL = "selectJson SQL: ";
	private static final String DELETE_BY_ID_SQL = "deleteById SQL: ";
	private static final String SELECT_BY_ID_SQL = "selectById SQL: ";
	private static final String UPDATE_SQL_WHERE_FIELDS = "update SQL(whereFields) :";
	private static final String UPDATE_SQL_UPDATE_FIELDS = "update SQL(updateFields) :";
	private static final String ID_IS_NULL = "in method selectById,id is null! ";
    private static final String START_GREAT_EQ_0 = "Parameter 'start' need great equal 0!";
	private static final String SIZE_GREAT_0 = "Parameter 'size' need great than 0!";
	private static final String TIP_SIZE_0 = "The size is 0, but it should be greater than 0 (>0)";
	
    
	public ObjToSQLRich getObjToSQLRich() {
		if(objToSQLRich==null) objToSQLRich=BeeFactory.getHoneyFactory().getObjToSQLRich();
		return objToSQLRich;
	}

	public void setObjToSQLRich(ObjToSQLRich objToSQLRich) {
		this.objToSQLRich = objToSQLRich;
	}

	@Override
	public <T> List<T> select(T entity, int size) {
		if (entity == null) return null;
		if (size == 0) {
			Logger.warn(TIP_SIZE_0);
			return Collections.emptyList();
		}
		if (size < 0) throw new BeeIllegalParameterException(SIZE_GREAT_0);

		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, size);
		sql = doAfterCompleteSql(sql);

		List<T> list = getBeeSql().select(sql, entity);
		doBeforeReturn(list);

		return list;
	}

	@Override
	public <T> List<T> select(T entity, int start, int size) {
		if (entity == null) return null;
		if (size == 0) {
			Logger.warn(TIP_SIZE_0);
			return Collections.emptyList();
		}
		if(size<0) throw new BeeIllegalParameterException(SIZE_GREAT_0);
		if(start<0) throw new BeeIllegalParameterException(START_GREAT_EQ_0);
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, start, size);
		sql = doAfterCompleteSql(sql);
		
		List<T> list = getBeeSql().select(sql, entity);
		doBeforeReturn(list);
		return list;
	}

	@Override
	public <T> List<T> select(T entity, String selectField) {//sqlLib.selectSomeField
		if (entity == null) return null;
		List<T> list = null;
		try {
			doBeforePasreEntity(entity,SuidType.SELECT);
			String sql = getObjToSQLRich().toSelectSQL(entity, selectField);
			sql = doAfterCompleteSql(sql);
			list = getBeeSql().selectSomeField(sql, entity);
			doBeforeReturn(list);
		} catch (ObjSQLException e) {
			throw e;
		}
		
		return list;
	}

	@Override
	public <T> List<T> select(T entity, String selectFields, int start, int size) {
		if (entity == null) return null;
		if (size == 0) {
			Logger.warn(TIP_SIZE_0);
			return Collections.emptyList();
		}
		if(size<0) throw new BeeIllegalParameterException(SIZE_GREAT_0);
		if(start<0) throw new BeeIllegalParameterException(START_GREAT_EQ_0);
		doBeforePasreEntity(entity,SuidType.SELECT);
		List<T> list = null;
		String sql = getObjToSQLRich().toSelectSQL(entity, selectFields,start,size);
		sql = doAfterCompleteSql(sql);
		list = getBeeSql().selectSomeField(sql, entity);
		doBeforeReturn(list);
		return list;
	}

	@Override
	public <T> List<T> selectOrderBy(T entity, String orderFields) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		List<T> list = null;
		try {
			String sql = getObjToSQLRich().toSelectOrderBySQL(entity, orderFields);
			sql = doAfterCompleteSql(sql);
			Logger.logSQL("selectOrderBy SQL: ", sql);
			list = getBeeSql().select(sql, entity);
			doBeforeReturn(list);
		} catch (ObjSQLException e) {
			throw e;
		}

		return list;
	}

	@Override
	public <T> List<T> selectOrderBy(T entity, String orderFields, OrderType[] orderTypes) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		List<T> list = null;
		try {
			String sql = getObjToSQLRich().toSelectOrderBySQL(entity, orderFields, orderTypes);
			sql = doAfterCompleteSql(sql);
			Logger.logSQL("selectOrderBy SQL: ", sql);
			list = getBeeSql().select(sql, entity);
			doBeforeReturn(list);
		} catch (ObjSQLException e) {
			throw e;
		}

		return list;
	}

	@Override
	public <T> int insert(T entity[]) {
		if (entity == null || entity.length < 1) return -1;
		checkNull(entity);
		doBeforePasreEntity(entity, SuidType.INSERT);
		String insertSql[] = getObjToSQLRich().toInsertSQL(entity);
		_regEntityClass1(entity[0]);
		insertSql[0] = doAfterCompleteSql(insertSql[0]);

		HoneyUtil.revertId(entity);

		int a = getBeeSql().batch(insertSql);
		doBeforeReturn();

		return a;
	}
	
	private <T> void checkNull(T entity[]) {
		for (int i = 0; i < entity.length; i++) {
			if(entity[i]==null) throw new ObjSQLException("entity[] have null element, index: "+i);
		}
	}

	@Override
	public <T> int insert(T entity[], String excludeFields) {
		if (entity == null || entity.length < 1) return -1;
		checkNull(entity);
		doBeforePasreEntity(entity, SuidType.INSERT);
		String insertSql[] = getObjToSQLRich().toInsertSQL(entity, excludeFields);
		_regEntityClass1(entity[0]);
		insertSql[0] = doAfterCompleteSql(insertSql[0]);

		HoneyUtil.revertId(entity);

		int a = getBeeSql().batch(insertSql);
		doBeforeReturn();
		return a;
	}

	@Override
	public <T> int insert(T entity[], int batchSize) {
		return insert(entity, batchSize, "");
	}

	@Override
	public <T> int insert(T entity[], int batchSize, String excludeFields) {
		if (entity == null || entity.length<1) return -1;
		checkNull(entity);
		doBeforePasreEntity(entity, SuidType.INSERT);
		if(batchSize<=0) batchSize=10;
		String insertSql[] = getObjToSQLRich().toInsertSQL(entity,batchSize, excludeFields);
		_regEntityClass1(entity[0]);
		insertSql[0] = doAfterCompleteSql(insertSql[0]);
		
		HoneyUtil.revertId(entity);
		
		int a= getBeeSql().batch(insertSql, batchSize);
		doBeforeReturn();
		return a;
	}

	@Override
	public <T> int update(T entity, String updateFields) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateSQL(entity, updateFields);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_UPDATE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}

	@Override
	public <T> T selectOne(T entity) {
		if (entity == null) return null;
		List<T> list = select(entity);  //已处理拦截器链
		if (list == null || list.size() != 1) return null;
		return list.get(0);
	}

	@Override
	public <T> String selectWithFun(T entity, FunctionType functionType, String fieldForFun) {
		return selectWithFun(entity, functionType, fieldForFun, null);
	}

	@Override
	public <T> String selectWithFun(T entity, FunctionType functionType, String fieldForFun, Condition condition) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		String s = null;
		String sql = getObjToSQLRich().toSelectFunSQL(entity, functionType, fieldForFun, condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		s = getBeeSql().selectFun(sql);
		doBeforeReturn();
		return s;
	}

	@Override
	public <T> int count(T entity) {
		return count(entity, null);
	}

	@Override
	public <T> int count(T entity, Condition condition) {
		String total = selectWithFun(entity, FunctionType.COUNT, "*", condition);
		return total == null ? 0 : Integer.parseInt(total);
	}

	@Override
	public <T> int update(T entity, String updateFields, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateSQL(entity, updateFields, includeType);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_UPDATE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}

	@Override
	public <T> List<T> select(T entity, IncludeType includeType) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, includeType);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_SQL, sql);
		List<T> list = getBeeSql().select(sql, entity);
		doBeforeReturn(list);
		return list;
	}

	@Override
	public <T> int update(T entity, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		String sql = getObjToSQLRich().toUpdateSQL(entity, includeType);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("update SQL: ", sql);
		
		int r= getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}

	@Override
	public <T> int insert(T entity, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.INSERT);
		String sql = getObjToSQLRich().toInsertSQL(entity, includeType);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("insert SQL: ", sql);
		int r= getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}
	
	@Override
	public <T> long insertAndReturnId(T entity, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.INSERT);
		String sql = getObjToSQLRich().toInsertSQL(entity, includeType);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("insert SQL: ", sql);
		
		return _insertAndReturnId(entity, sql);
		
	}
	
	

	@Override
	public <T> int delete(T entity, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.DELETE);
		String sql = getObjToSQLRich().toDeleteSQL(entity, includeType);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("delete SQL: ", sql);
		int r= getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}

	@Override
	public <T> List<String[]> selectString(T entity) {

		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);

		List<String[]> list = null;
		String sql = getObjToSQLRich().toSelectSQL(entity);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("select SQL(return List<String[]>): ", sql);
		list = getBeeSql().select(sql);
		doBeforeReturn();
		return list;
	}

	@Override
	public <T> List<String[]> selectString(T entity, String ...selectFields) {

		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		
		List<String[]> list = null;
		try {
			String sql = getObjToSQLRich().toSelectSQL(entity, selectFields);
			_regEntityClass1(entity);
			sql = doAfterCompleteSql(sql);
			list = getBeeSql().select(sql);
			doBeforeReturn();
		} catch (ObjSQLException e) {
			throw e;
		}

		return list;
	}
	
	@Override
	public <T> List<String[]> selectString(T entity, Condition condition) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		List<String[]> list = null;
		String sql = getObjToSQLRich().toSelectSQL(entity, condition.getIncludeType(), condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("select SQL(return List<String[]>): ", sql);
		list= getBeeSql().select(sql);
		doBeforeReturn();
		return list;
	}
	
	@Override
	public <T> String selectJson(T entity) {
		
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		
		String sql = getObjToSQLRich().toSelectSQL(entity);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_JSON_SQL, sql);
		String json = getBeeSql().selectJson(sql);
		doBeforeReturn();
		return json;
	}

	@Override
	public <T> String selectJson(T entity, IncludeType includeType) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		
		String sql = getObjToSQLRich().toSelectSQL(entity, includeType);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_JSON_SQL, sql);
		String json= getBeeSql().selectJson(sql);
		doBeforeReturn();
		return json;
	}
	
	@Override
	public <T> String selectJson(T entity, String selectField) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		
		String sql = getObjToSQLRich().toSelectSQL(entity, selectField);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("selectJson(T entity, String selectField) SQL: ", sql);
		String json= getBeeSql().selectJson(sql);
		doBeforeReturn();
		return json;
	}
	
	@Override
	public <T> String selectJson(T entity, String selectFields, int start, int size) {
		if (entity == null) return null;
		if (size == 0) {
			Logger.warn(TIP_SIZE_0);
			return null;
		}
		if(size<0) throw new BeeIllegalParameterException(SIZE_GREAT_0);
		if(start<0) throw new BeeIllegalParameterException(START_GREAT_EQ_0);
		
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, selectFields,start,size);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("selectJson(T entity, String selectField, int start, int size) SQL: ", sql);
		String json= getBeeSql().selectJson(sql);
		doBeforeReturn();
		return json;
	}

	@Override
	public <T> T selectById(T entity, Integer id) {
		if (entity == null) return null;

		if (id==null) {
			Logger.warn(ID_IS_NULL);
			return null;
		}
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectByIdSQL(entity, id);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_BY_ID_SQL, sql);
		List<T> list = getBeeSql().select(sql, entity);
		
		return getIdEntity(list);
	}
	
	@Override
	public <T> T selectById(T entity, Long id) {
		if (entity == null) return null;

		if (id==null) {
			Logger.warn(ID_IS_NULL);
			return null;
		}
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectByIdSQL(entity, id);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_BY_ID_SQL, sql);
		List<T> list = getBeeSql().select(sql, entity);
		
		return getIdEntity(list);
	}
	
	@Override
	public <T> T selectById(T entity, String id) {
		if (entity == null) return null;

		if (id==null) {
			Logger.warn(ID_IS_NULL);
			return null;
		}
		if(id.contains(",")) {
			throw new BeeIllegalParameterException("The parameter 'id' of method selectById does not allow to contain comma!");
		}
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectByIdSQL(entity, id);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_BY_ID_SQL, sql);
		
		List<T> list = getBeeSql().select(sql, entity);
		
		return getIdEntity(list);
	}
	
	@Override
	public <T> List<T> selectByIds(T entity, String ids) {
		if (entity == null) return null;

		if (ids==null) {
			Logger.warn("in method selectByIds,ids is null! ");
			return null;
		}
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectByIdSQL(entity, ids);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("selectByIds SQL: ", sql);
		List<T> list= getBeeSql().select(sql, entity);
		doBeforeReturn(list);
		return list;
	}
	
	private <T> T getIdEntity(List<T> list) {
		if(list==null || list.size()<1) {
			doBeforeReturn();
			return null;
		}else {
			doBeforeReturn(list);
			return list.get(0);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int deleteById(Class c, Integer id) {
		if(id==null) Logger.warn("in method deleteById,id is null! ");
		if (c == null || id==null) return 0;
		doBeforePasreEntity(c,SuidType.DELETE);
		String sql = getObjToSQLRich().toDeleteByIdSQL(c, id);
		_regEntityClass2(c);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(DELETE_BY_ID_SQL, sql);
		int a=getBeeSql().modify(sql);
		doBeforeReturn();
		return a;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int deleteById(Class c, Long id) {
		if(id==null) Logger.warn("in method deleteById,id is null! ");
		if (c == null || id==null) return 0;
		doBeforePasreEntity(c,SuidType.DELETE);
		String sql = getObjToSQLRich().toDeleteByIdSQL(c, id);
		_regEntityClass2(c);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(DELETE_BY_ID_SQL, sql);
		int a=getBeeSql().modify(sql);
		doBeforeReturn();
		return a;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int deleteById(Class c, String ids) {
		if(ids==null) Logger.warn("in method deleteById,ids is null! ");
		if (c == null || ids==null) return 0;
		doBeforePasreEntity(c,SuidType.DELETE);
		String sql = getObjToSQLRich().toDeleteByIdSQL(c, ids);
		_regEntityClass2(c);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(DELETE_BY_ID_SQL, sql);
		int a=getBeeSql().modify(sql);
		doBeforeReturn();
		return a;
	}

	@Override
	@Deprecated
	public <T> List<T> select(T entity, IncludeType includeType, Condition condition) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, includeType, condition);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_SQL, sql);
		List<T> list= getBeeSql().select(sql, entity);
		doBeforeReturn(list);
		return list;
	}

	@Override
	public <T> String selectJson(T entity, IncludeType includeType, Condition condition) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, includeType,condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_JSON_SQL, sql);
		String json= getBeeSql().selectJson(sql);
		doBeforeReturn();
		return json;
	}
	
	@Override
	public <T> String selectJson(T entity, Condition condition) {
		if (entity == null) return null;
		doBeforePasreEntity(entity,SuidType.SELECT);
		String sql = getObjToSQLRich().toSelectSQL(entity, condition.getIncludeType(),condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(SELECT_JSON_SQL, sql);
		
		String json= getBeeSql().selectJson(sql);
		doBeforeReturn();
		
		return json;
	}

	@Override
	public <T> int updateBy(T entity, String whereFields) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateBySQL(entity, whereFields); //updateBy
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_WHERE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}

	@Override
	public <T> int updateBy(T entity, String whereFields, IncludeType includeType) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateBySQL(entity, whereFields, includeType);//updateBy
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_WHERE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}
	
	//v1.7.2
	@Override
	public <T> int updateBy(T entity, String whereFields, Condition condition) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateBySQL(entity, whereFields, condition);//updateBy
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_WHERE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}
	
	//v1.9
	@Override
	public <T> int updateById(T entity, Condition condition) {
		String pkName="";
		try {
			entity.getClass().getDeclaredField("id");
			pkName="id";
		} catch (NoSuchFieldException e) {
			pkName = HoneyUtil.getPkFieldName(entity);
		}
		
	   //支持联合主键
       return updateBy(entity, pkName, condition);
	}

	//v1.7.2
	@Override
	public <T> int update(T entity, String updateFields, Condition condition) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateSQL(entity, updateFields, condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL(UPDATE_SQL_UPDATE_FIELDS, sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}
	
	//v1.8
	@Override
	public <T> int update(T entity, Condition condition) {
		if (entity == null) return -1;
		doBeforePasreEntity(entity,SuidType.UPDATE);
		int r = 0;
		String sql = getObjToSQLRich().toUpdateSQL(entity, "", condition);
		_regEntityClass1(entity);
		sql = doAfterCompleteSql(sql);
		Logger.logSQL("update SQL(condition) :", sql);
		r = getBeeSql().modify(sql);
		doBeforeReturn();
		return r;
	}
	
	//1.9
	@Override
	public <T> int insert(List<T> entityList) {
		if (entityList == null || entityList.size()<1) return -1;
		T entity[]=toEntityArray(entityList);
		return insert(entity);
	}
	
	//1.9
	@Override
	public <T> int insert(List<T> entityList, int batchSize) {
		if (entityList == null || entityList.size()<1) return -1;
		T entity[]=toEntityArray(entityList);
		return insert(entity, batchSize);
	}

	//1.9
	@Override
	public <T> int insert(List<T> entityList, String excludeFields) {
		if (entityList == null || entityList.size()<1) return -1;
		T entity[]=toEntityArray(entityList);
		return insert(entity, excludeFields);
	}

	//1.9
	@Override
	public <T> int insert(List<T> entityList, int batchSize, String excludeFields) {
		if (entityList == null || entityList.size()<1) return -1;
		T entity[]=toEntityArray(entityList);
		
		return insert(entity, batchSize, excludeFields);
	}

	//没能将entity传到SqlLib,需要注册
	private <T> void _regEntityClass1(T entity){
		if(entity==null) return ;
		HoneyContext.regEntityClass(entity.getClass());
	}
	
	@SuppressWarnings("rawtypes")
	private void _regEntityClass2(Class clazz){
		HoneyContext.regEntityClass(clazz);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] toEntityArray(List<T> entityList) {
		
		int len=entityList.size();
		T entity[]=(T[])new Object[len];
		
		for (int i=0; i < len; i++) {
			entity[i]=entityList.get(i);
		}
		
		return entity;
	}
	
	@Override
	public SuidRich setDynamicParameter(String para, String value) {
		OneTimeParameter.setAttribute(para, value);
		return this;
	}
	
	@Override
	public <T> boolean exist(T entity) {
		int r = count(entity);
		return r > 0 ? true : false;
	}

	@Override
	public <T> int update(T oldEntity, T newEntity) {

		if (oldEntity == null || newEntity == null) return -1;
		String oldEntityFullName = oldEntity.getClass().getName();
		String newEntityFullName = newEntity.getClass().getName();
		if (!oldEntityFullName.equals(newEntityFullName)) {
			throw new BeeErrorGrammarException(
					"BeeErrorGrammarException: the oldEntity and newEntity must be same type!");
		}
		
		doBeforePasreEntity(newEntity,SuidType.UPDATE); //拦截器只处理新实体；  旧实体oldEntity作为条件不在拦截器处理。

		Map<String, Object> oldMap = SuidHelper.entityToMap(oldEntity);
		Map<String, Object> newMap = SuidHelper.entityToMap(newEntity);

		MapSql updateMapSql = BeeFactoryHelper.getMapSql();
		updateMapSql.put(MapSqlKey.Table, _toTableName(oldEntity));
		updateMapSql.put(MapSqlSetting.IsNamingTransfer, true);
		updateMapSql.put(oldMap);
		updateMapSql.putNew(newMap);

		Logger.logSQL("update(T oldEntity, T newEntity) with MapSuid, ", "");
		MapSuid mapSuid = BeeFactoryHelper.getMapSuid();
		return mapSuid.update(updateMapSql);  //it will use Interceptor
	}
	
	private static String _toTableName(Object entity) {
		return NameTranslateHandle.toTableName(NameUtil.getClassFullName(entity));
	}
	
	/**
	 * 保存一个实体(一条记录).
	 * 如果可以区分开,建议明确调用insert(entity)或者update(entity),这样更加安全和高效.
	 * @param entity
	 * @return 返回受影响的行数.
	 * @since 1.9.8
	 */
	@Override
	public <T> int save(T entity) {
		Object id = HoneyUtil.getIdValue(entity);
		if (id == null) return insert(entity);
		Object one = selectById(entity, id.toString());
		if (one != null)
			return update(entity);
		else
			return insert(entity);
	}
	
	void doBeforePasreEntity(Object entity[], SuidType SuidType) {
		getInterceptorChain().beforePasreEntity(entity, SuidType);
	}

}
