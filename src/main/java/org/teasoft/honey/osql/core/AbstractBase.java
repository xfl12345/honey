/*
 * Copyright 2016-2022 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.lang.reflect.Field;
import java.util.List;

import org.teasoft.bee.osql.Cache;
import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.type.TypeHandler;
import org.teasoft.honey.osql.util.AnnoUtil;
import org.teasoft.honey.sharding.ShardingUtil;

/**
 * @author Kingstar
 * @since  2.0
 */
public abstract class AbstractBase {
	
	private int cacheWorkResultSetSize=HoneyConfig.getHoneyConfig().cache_workResultSetSize;
	private boolean showSQL = getShowSQL();
	private boolean showShardingSQL = getShowShardingSQL();
	
	protected static boolean openFieldTypeHandler = HoneyConfig.getHoneyConfig().openFieldTypeHandler;
	
	private Cache cache;
	public Cache getCache() {
		if(cache==null) {
			cache=BeeFactory.getHoneyFactory().getCache();
		}
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	@SuppressWarnings("rawtypes")
	protected static String _toColumnName(String fieldName, Class entityClass) {
		return NameTranslateHandle.toColumnName(fieldName, entityClass);
	}

//	@SuppressWarnings("rawtypes")
//	private static String _toFieldName(String columnName,Class entityClass) {
//		return NameTranslateHandle.toFieldName(columnName,entityClass);
//	}
	
	//add on 2019-10-01
	protected void addInCache(String sql, Object rs, String returnType, SuidType suidType,int resultSetSize) {
		
		if(HoneyContext.getSqlIndexLocal()!=null) return ; //子查询不放缓存
		
//		如果结果集超过一定的值则不放缓存
		if(resultSetSize>cacheWorkResultSetSize){
		   HoneyContext.deleteCacheInfo(sql);
		   return;
		}
		
		CacheSuidStruct struct = HoneyContext.getCacheInfo(sql);
		if (struct != null) { //之前已定义有表结构,才放缓存.否则放入缓存,可能会产生脏数据.  不判断的话,自定义的查询也可以放缓存
//			struct.setReturnType(returnType);  //因一进来updateInfoInCache时,已添加有
//			struct.setSuidType(suidType.getType());
//			HoneyContext.setCacheInfo(sql, struct);
			getCache().add(sql, rs);
		}
	}
	
//	查缓存前需要先更新缓存信息,才能去查看是否在缓存
	protected boolean updateInfoInCache(String sql, String returnType, SuidType suidType) {
		if(HoneyContext.getSqlIndexLocal()!=null) return false; //子查询不放缓存
		return HoneyContext.updateInfoInCache(sql, returnType, suidType);
	}
	
	protected void clearInCache(String sql, String returnType, SuidType suidType, int affectRow) {
		CacheSuidStruct struct = HoneyContext.getCacheInfo(sql);
		if (struct != null) {
			struct.setReturnType(returnType);
			struct.setSuidType(suidType.getType());
			HoneyContext.setCacheInfo(sql, struct);
		}
		clearContext(sql);
		if (affectRow > 0) { //INSERT、UPDATE 或 DELETE成功,才清除结果缓存
			getCache().clear(sql);
		}
	}
	
	protected void clearContext(String sql) {
		HoneyContext.clearPreparedValue(sql); // close in 2.0  ???
//		if(HoneyContext.isNeedRealTimeDb() && HoneyContext.isAlreadySetRoute()) { //当可以从缓存拿时，需要清除为分页已设置的路由
//			HoneyContext.removeCurrentRoute(); //放到拦截器中
//		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void initRoute(SuidType suidType, Class clazz, String sql) {
		boolean enableMultiDs=HoneyConfig.getHoneyConfig().multiDS_enable;
//		if (!enableMultiDs) return;  //close in 1.17
		if (!enableMultiDs && !HoneyContext.useStructForLevel2()) return; //1.17 fixed
		if(HoneyContext.isNeedRealTimeDb() && HoneyContext.isAlreadySetRoute()) return; // already set in parse entity to sql.
		//enableMultiDs=true,且还没设置的,都要设置   因此,清除时,也是这样清除.
		HoneyContext.initRoute(suidType, clazz, sql);
	}
	
	protected boolean isConfuseDuplicateFieldDB(){
		return HoneyUtil.isConfuseDuplicateFieldDB();
	}
	
	protected void logSelectRows(int size) {
		if (ShardingUtil.isSharding()&& !showShardingSQL) return ;
		Logger.logSQL(" | <--  select rows: ", size + "" + shardingIndex());
	}
	
	protected void logDsTab() {
		if (! showShardingSQL) return ;
		List<String> dsNameListLocal=HoneyContext.getListLocal(StringConst.DsNameListLocal);
		List<String> tabNameList=HoneyContext.getListLocal(StringConst.TabNameListLocal);
		Logger.logSQL("========= Involved DataSource: "+dsNameListLocal+"  ,Involved Table: "+tabNameList);
	}
	
	protected static final String INDEX1 = "_SYS[index";
	protected static final String INDEX2 = "]_End ";
	
	protected void clearContext(String sql_0, int batchSize, int len) {
		for (int i = 0; i < len; i++) {
			String sql_i = INDEX1 + i + INDEX2 + sql_0;
			clearContext(sql_i);
		}
	}
	
	
	//检测是否有Json注解
	protected boolean isJoson(Field field) {
		return AnnoUtil.isJson(field);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object jsonHandlerProcess(Field field, Object obj, TypeHandler jsonHandler) {
		if (List.class.isAssignableFrom(field.getType())) {
			Object newObj[] = new Object[2];
			newObj[0] = obj;
			newObj[1] = field;
			obj = jsonHandler.process(field.getType(), newObj);
		} else {
			obj = jsonHandler.process(field.getType(), obj);
		}
		return obj;
	}
	
	@SuppressWarnings("rawtypes")
	protected Object createObject(Class c) throws IllegalAccessException,InstantiationException{
		return c.newInstance();
	}
	
	private String shardingIndex() {
		Integer subThreadIndex = HoneyContext.getSqlIndexLocal();
		String index = "";
		if (subThreadIndex != null) {
			index = " (sharding " + subThreadIndex + ")";
		}
		return index;
	}
	
	private boolean getShowSQL() {
		return HoneyConfig.getHoneyConfig().showSQL;
	}
	
	private boolean getShowShardingSQL() {
		return showSQL && HoneyConfig.getHoneyConfig().showShardingSQL;
	}

}
