/*
 * Copyright 2016-2022 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teasoft.bee.app.BeeSqlForApp;
import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.annotation.JoinTable;
import org.teasoft.bee.osql.annotation.customizable.Json;
import org.teasoft.bee.osql.type.SetParaTypeConvert;
import org.teasoft.bee.osql.type.TypeHandler;
import org.teasoft.honey.osql.type.SetParaTypeConverterRegistry;
import org.teasoft.honey.osql.type.TypeHandlerRegistry;
import org.teasoft.honey.util.ObjectCreatorFactory;

/**
 * @author Kingstar
 * @since  1.17
 */
public class SqlLibForApp extends SqlLib {
	
	private static boolean  showSQL=HoneyConfig.getHoneyConfig().showSQL;
	
	private BeeSqlForApp beeSqlForApp;
	
	private static boolean isFirst = true;
	private static String SUCCESS_MSG = "[Bee] ==========Load BeeSqlForApp implement class successfully!";

	public BeeSqlForApp getBeeSqlForApp() {
		if(beeSqlForApp!=null) return beeSqlForApp; 
		try {
			if(HoneyConfig.getHoneyConfig().isAndroid)
			  beeSqlForApp = (BeeSqlForApp) Class.forName("org.teasoft.beex.android.SqlLibExtForAndroid").newInstance();
			else if(HoneyConfig.getHoneyConfig().isHarmony)
			  beeSqlForApp = (BeeSqlForApp) Class.forName("org.teasoft.beex.harmony.SqlLibExtForHarmony").newInstance();
			if(isFirst) {
				Logger.info(SUCCESS_MSG);
				isFirst=false;
			}else {
				Logger.debug(SUCCESS_MSG);
			}
		} catch (Exception e) {
			Logger.warn(e.getMessage(), e);
		}

		if (beeSqlForApp == null) {
			Logger.warn("[Bee] ==========Load BeeSqlForApp implement class fail!");
			beeSqlForApp = new SqlLibEmptyForApp();
		}

		return beeSqlForApp;
	}

	public void setBeeSqlForApp(BeeSqlForApp beeSqlForApp) {
		this.beeSqlForApp = beeSqlForApp;
	}

	@Override
	public <T> List<T> select(String sql, T entity) {
		return selectSomeField(sql, entity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> selectSomeField(String sql, T entity) {

		if (sql == null || "".equals(sql.trim())) return Collections.emptyList();

		boolean isReg = updateInfoInCache(sql, "List<T>", SuidType.SELECT);
		if (isReg) {
			initRoute(SuidType.SELECT, entity.getClass(), sql);
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				List<T> list = (List<T>) cacheObj;
				logSelectRows(list.size());
				return list;
			}
		}
		List<T> rsList = null;
//		boolean hasException = false;
		try {
			rsList=getBeeSqlForApp().select(sql, entity, toStringArray(sql));
			addInCache(sql, rsList, "List<T>", SuidType.SELECT, rsList.size());

		} catch (Exception e) {
//			hasException = true;
			throw ExceptionHelper.convert(e);
//		} catch (IllegalAccessException e) {
//			hasException = true;
//			throw ExceptionHelper.convert(e);
//		} catch (InstantiationException e) {
//			hasException = true;
//			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);  //关闭资源
			clearContext(sql);
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
//			entity = null;
//			targetObj = null;
//			map = null;
		}
		logSelectRows(rsList.size());

		return rsList;
	}
	
	@Override
	public String selectFun(String sql) {
		if(sql==null || "".equals(sql.trim())) return null;
		
		boolean isReg = updateInfoInCache(sql, "String", SuidType.SELECT);
		if (isReg) {
			initRoute(SuidType.SELECT, null, sql);
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				return (String) cacheObj;
			}
		}
		
		String result = null;
//		Connection conn = null;
//		PreparedStatement pst = null;
//		ResultSet rs = null;
//		boolean hasException = false;
		try {
			result=getBeeSqlForApp().selectFun(sql, toStringArray(sql));
			
			addInCache(sql, result,"String",SuidType.SELECT,1);

		} catch (Exception e) {
//			hasException=true;
			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);
			clearContext(sql);
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}

		return result;
	}

	@Override
	public List<String[]> select(String sql) {
		
		if(sql==null || "".equals(sql.trim())) return Collections.emptyList();
		
		boolean isReg = updateInfoInCache(sql, "List<String[]>", SuidType.SELECT);
		if (isReg) {
			initRoute(SuidType.SELECT, null, sql);
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				List<String[]> list=(List<String[]>) cacheObj;
				logSelectRows(list.size());
				return list;
			}
		}
		
		List<String[]> list = new ArrayList<>();
		
//		Connection conn = null;
//		PreparedStatement pst = null;
//		ResultSet rs = null;
//		boolean hasException = false;
		try {
//			conn = getConn();
//			String exe_sql=HoneyUtil.deleteLastSemicolon(sql);
//			pst = conn.prepareStatement(exe_sql);
//			setPreparedValues(pst, sql);
//			rs = pst.executeQuery();
//			list=TransformResultSet.toStringsList(rs);
			
			list=getBeeSqlForApp().select(sql, toStringArray(sql));
			
			logSelectRows(list.size());
			addInCache(sql, list,"List<String[]>",SuidType.SELECT,list.size());
			
		} catch (Exception e) {
//			hasException=true;
			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);
			clearContext(sql);
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}

		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectMapList(String sql) {
		
	if(sql==null || "".equals(sql.trim())) return Collections.emptyList();
		
		boolean isReg = updateInfoInCache(sql, "List<Map<String,Object>>", SuidType.SELECT);
		if (isReg) { 
			initRoute(SuidType.SELECT, null, sql);
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				List<Map<String,Object>> list=(List<Map<String,Object>>) cacheObj;
				logSelectRows(list.size());
				return list;
			}
		}
		
		List<Map<String,Object>> list = new ArrayList<>();
//		Connection conn = null;
//		PreparedStatement pst = null;
//		ResultSet rs = null;
//		boolean hasException = false;
		try {
//			conn = getConn();
			String exe_sql=HoneyUtil.deleteLastSemicolon(sql);
//			pst = conn.prepareStatement(exe_sql);
//			setPreparedValues(pst, sql);
//			rs = pst.executeQuery();

//			list=TransformResultSet.toMapList(rs);
			
			list = getBeeSqlForApp().selectMapList(exe_sql, toStringArray(sql));
			
			logSelectRows(list.size());
			
			addInCache(sql, list,"List<Map<String,Object>>",SuidType.SELECT,list.size());
			
		} catch (Exception e) {
//			hasException=true;
			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);
			clearContext(sql);
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}

		return list;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public String selectJson(String sql) {
		
		if(sql==null || "".equals(sql.trim())) return null;
		
		boolean isReg = updateInfoInCache(sql, "StringJson", SuidType.SELECT);
		Class entityClass = (Class) OneTimeParameter.getAttribute(StringConst.Route_EC);
		if (isReg) {
			initRoute(SuidType.SELECT, entityClass, sql);
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				return (String) cacheObj;
			}
		}
		
		String json="";
		
//		StringBuffer json=new StringBuffer("");
//		Connection conn = null;
//		PreparedStatement pst = null;
//		ResultSet rs = null;
//		boolean hasException = false;
		try {
			String exe_sql=HoneyUtil.deleteLastSemicolon(sql);
			
//			conn = getConn();
//			pst = conn.prepareStatement(exe_sql);
//			setPreparedValues(pst, sql);
//			rs = pst.executeQuery();
//			json = TransformResultSet.toJson(rs,entityClass);
			
			json = getBeeSqlForApp().selectJson(exe_sql, toStringArray(sql), entityClass);
			
			addInCache(sql, json,"StringJson",SuidType.SELECT,-1);  //没有作最大结果集判断

		} catch (Exception e) {
//			hasException = true;  //fixbug  2021-05-01
			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);
			clearContext(sql);
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}

		return json;
	}

	@Override
	public int modify(String sql) {
		if(sql==null || "".equals(sql)) return -2;
		
		initRoute(SuidType.MODIFY,null,sql);
		
		int num = 0;
//		boolean hasException = false;
		try {
			String exe_sql=HoneyUtil.deleteLastSemicolon(sql);
			num = getBeeSqlForApp().modify(exe_sql, toObjArray(sql));
			
//		} catch (SQLException e) {
//			boolean notCatch=HoneyConfig.getHoneyConfig().notCatchModifyDuplicateException;
//			if (!notCatch && isConstraint(e)) { //内部捕获并且是重复异常,则由Bee框架处理 
//				boolean notShow=HoneyConfig.getHoneyConfig().notShowModifyDuplicateException;
//				if(! notShow) Logger.warn(e.getMessage());
//				return num;
//			}
//			
//			hasException=true;
//			throw ExceptionHelper.convert(e);
		} finally {
			clearInCache(sql, "int",SuidType.MODIFY,num); //has clearContext(sql)
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}
		
		Logger.logSQL(" | <--  Affected rows: ", num+"");
		
		return num;
	}

	@Override
	public long insertAndReturnId(String sql) {
		if (sql == null || "".equals(sql)) return -2L;

		initRoute(SuidType.INSERT, null, sql);

		long returnId = -1L;
		int num = 1;
		try {
			String exe_sql = HoneyUtil.deleteLastSemicolon(sql);
			returnId = getBeeSqlForApp().insertAndReturnId(exe_sql, toObjArray(sql));
			if (returnId == -1) num = 0;
		} finally {
			clearInCache(sql, "int", SuidType.INSERT, num); 
		}

		Logger.logSQL(" | <--  Affected rows: ", num + "");

		return returnId;
	}

	@Override
	public int batch(String[] sql) {
		if(sql==null) return -1;
		int batchSize = HoneyConfig.getHoneyConfig().insertBatchSize;

		return batch(sql,batchSize);
	}

	@Override
	public int batch(String sql[], int batchSize) {

		if (sql == null || sql.length < 1) return -1;

		initRoute(SuidType.INSERT, null, sql[0]);

		int len = sql.length;
		int total = 0;
		int temp = 0;
		try {
			if (len <= batchSize) {
				total = batch(sql[0], 0, len);
			} else {
				for (int i = 0; i < len / batchSize; i++) {
					temp = batch(sql[0], i * batchSize, (i + 1) * batchSize);
					total += temp;
				}

				if (len % batchSize != 0) { // 尾数不成批
					temp = batch(sql[0], len - (len % batchSize), len);
					total += temp;
				}
			}
		} catch (Exception e) {
			clearContext(sql[0], batchSize, len);
		} finally {
			// 更改操作需要清除缓存
			clearInCache(sql[0], "int[]", SuidType.INSERT, total);
		}

		return total;
	}

	private static final String INDEX1 = "_SYS[index";
	private static final String INDEX2 = "]_End ";
	private static final String INDEX3 = "]";
	private static final String INSERT_ARRAY_SQL = " insert[] SQL : ";

	private int batch(String sql, int start, int end) {
		int a = 0;
		
		List<Object[]> listBindArgs = new ArrayList<>(end - start);
		for (int i = start; i < end; i++) { // start... (end-1)
			if (showSQL) {
				if (i == 0) Logger.logSQL(INSERT_ARRAY_SQL, sql);

				OneTimeParameter.setAttribute("_SYS_Bee_BatchInsert", i + "");
				String sql_i = INDEX1 + i + INDEX2 + sql;
				Logger.logSQL(INSERT_ARRAY_SQL, sql_i);
			}

			listBindArgs.add(toObjArray(INDEX1 + i + INDEX2 + sql,false));
		}

		sql=HoneyUtil.deleteLastSemicolon(sql);//上面的sql还不能执行去分号,要先拿了缓存.
		a = getBeeSqlForApp().batchInsert(sql, listBindArgs);

		Logger.logSQL(" | <-- index[" + (start) + "~" + (end - 1) + INDEX3 + " Affected rows: ",
				a + "");

		return a;
	}
	
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> moreTableSelect(String sql, T entity) {
		
		if(sql==null || "".equals(sql.trim())) return Collections.emptyList();
		
		MoreTableStruct moreTableStruct[]=HoneyUtil.getMoreTableStructAndCheckBefore(entity);
		
//		不经过MoreTable,直接传入sql,需要重新生成结构  V1.11
		if(moreTableStruct==null) {
			OneTimeParameter.setTrueForKey(StringConst.MoreStruct_to_SqlLib);
			moreTableStruct=HoneyUtil.getMoreTableStructAndCheckBefore(entity);
		}
		
		boolean subOneIsList1=moreTableStruct[0].subOneIsList;
		boolean subTwoIsList2=moreTableStruct[0].subTwoIsList;
		String listFieldType=""+subOneIsList1+subTwoIsList2+moreTableStruct[0].oneHasOne;
		boolean isReg = updateInfoInCache(sql, "List<T>"+listFieldType, SuidType.SELECT);
		if (isReg) {
			initRoute(SuidType.SELECT, entity.getClass(), sql); //多表查询的多个表要在同一个数据源.
			Object cacheObj = getCache().get(sql); //这里的sql还没带有值
			if (cacheObj != null) {
				clearContext(sql);
				
				List<T> list=(List<T>) cacheObj;
				logSelectRows(list.size());
				return list;
			}
		}
		
		T targetObj=null;
		List<T> rsList=null;
		boolean hasException = false;
		int recordRow=0;
		try {
			String exe_sql=HoneyUtil.deleteLastSemicolon(sql);
			
			List<Map<String, String>> rsMapList=getBeeSqlForApp().selectMapListWithColumnName(exe_sql, toStringArray(sql));
			
			rsList = new ArrayList<>();

			Field field[] = entity.getClass().getDeclaredFields();
//			int columnCount = field.length;
			
			boolean oneHasOne=moreTableStruct[0].oneHasOne;
			
			Field subField[] = new Field[2];
			String subUseTable[]=new String[2];
			String variableName[]=new String[2];
			Class subEntityFieldClass[]=new Class[2];
			for (int i = 1; i <= 2; i++) {
				if(moreTableStruct[i]!=null){
					subField[i-1]=moreTableStruct[i].subEntityField;
					variableName[i-1]=subField[i-1].getName();
					if (subOneIsList1 && i==1) {
						subEntityFieldClass[0]=moreTableStruct[1].subClass;  //v1.9.8 List Field
					} else if (subTwoIsList2 && i==2) {
						subEntityFieldClass[1]=moreTableStruct[2].subClass;  //v1.9.8 List Field
					}else {
						subEntityFieldClass[i - 1] = subField[i - 1].getType();
					}
					subUseTable[i-1]=moreTableStruct[i].useSubTableName;
				}
			}
			
			Field fields1[] = subEntityFieldClass[0].getDeclaredFields();
			Field fields2[] =null;
			
            if(subField[1]!=null){
            	fields2=subEntityFieldClass[1].getDeclaredFields();
            }
            
            Map<String,String> dulSubFieldMap=moreTableStruct[0].subDulFieldMap;
            
            boolean sub1_first=true;
            boolean sub2_first=true;
            
            Object v1=null;
            Object v2=null;
            
            Map<String,List> subOneMap=null;
            Map<String,List> subTwoMap=null;
            
            if(subOneIsList1) subOneMap=new HashMap<>();
            if(subTwoIsList2) subTwoMap=new HashMap<>();
            
            StringBuffer checkKey=null;
            StringBuffer checkKey2ForOneHasOne=null;
			
			String tableName=moreTableStruct[0].tableName;
		
			Map<String,String> rsMap=null;
			for (int m = 0; m < rsMapList.size(); m++) {
			    rsMap=rsMapList.get(m);
				
			    recordRow++;
				boolean isDul=false;
				String dulField="";
				
				//从表2设置(如果有)  先设置,因oneHasOne时,设置从表1已经要用从表2了.
				sub2_first=true;
				Object subObj2=null;
				if(subField[1]!=null){
					String columnName="";
					for (int i = 0; i < fields2.length; i++) {
						
						if(HoneyUtil.isSkipField(fields2[i])) continue;
						
						boolean isRegHandlerPriority2 = false;
						if (openFieldTypeHandler) {
							isRegHandlerPriority2 = TypeHandlerRegistry.isPriorityType(fields2[i].getType());
						}
						
						v2=null;
						fields2[i].setAccessible(true);
						isDul=false;
						dulField="";
						try {
							columnName=_toColumnName(fields2[i].getName(),subEntityFieldClass[1]);
							String tempCName2="";
							//get v2
							if(isConfuseDuplicateFieldDB()){
								dulField=dulSubFieldMap.get(subUseTable[1]+"."+columnName);
								if(dulField!=null){
									isDul=true;  //set true first
//									v2 = rs.getObject(dulField);
									tempCName2=dulField;
								}else{
//									v2= rs.getObject(columnName);
									tempCName2=columnName;
								}
							} else {
//								v2= rs.getObject(subUseTable[1] + "." + columnName);
								tempCName2=subUseTable[1] + "." + columnName;
							}
//							v2 = rsMap.get(tempCName2);
							v2 =ObjectCreatorFactory.create(rsMap.get(tempCName2), fields2[i].getType());
							
							boolean processAsJson = false;
							if (isJoson(fields2[i])) {
								TypeHandler jsonHandler = TypeHandlerRegistry.getHandler(Json.class);
								if (jsonHandler != null) {
//									v2 = jsonHandler.process(fields2[i].getType(), v2);
									v2 = jsonHandlerProcess(fields2[i], v2, jsonHandler);
									processAsJson = true;
								}
							}

							if (!processAsJson && isRegHandlerPriority2) { //process v2 by handler
								v2 = TypeHandlerRegistry.handlerProcess(fields2[i].getType(), v2);
							}
							
							if (v2 != null) {
								if (sub2_first) {
									subObj2 = createObject(subEntityFieldClass[1]);
									sub2_first = false;
								}
								fields2[i].set(subObj2, v2);
							}
							
						} catch (IllegalArgumentException e) {
							Logger.error(e.getMessage(),e);
						
//							//get v2
//							if(isConfuseDuplicateFieldDB()){
//								v2=_getObjectForMoreTable_ConfuseField(rs,fields2[i],isDul,dulField,subEntityFieldClass[1]);  //todo
//							}else{
//								v2=_getObjectForMoreTable_NoConfuse(rs,subUseTable[1],fields2[i],subEntityFieldClass[1]);
//							}
//							
//							boolean alreadyProcess = false;
//							try {
//								if (openFieldTypeHandler) {//process v2 by handler
//									Class type = fields2[i].getType();
//									TypeHandler handler = TypeHandlerRegistry.getHandler(type);
//									if (handler != null) {
//										Object newV2 = handler.process(type, v2);//process v2 by handler
//										if (newV2 != null) {
//											if (sub2_first) {
//												subObj2 = createObject(subEntityFieldClass[1]);
//												sub2_first = false;
//											}
//											fields2[i].set(subObj2, newV2);
//											alreadyProcess = true;
//										}
//									}
//								}
//							} catch (Exception e2) {
//								alreadyProcess = false;
//							}
//							
//							if (!alreadyProcess && v2 != null) {
//								if (sub2_first) {
//									subObj2 = createObject(subEntityFieldClass[1]);
//									sub2_first = false;
//								}
//								fields2[i].set(subObj2, v2);
//							}
							
//						}catch (SQLException e) {// for after use condition selectField method
////							fields2[i].set(subObj2,null);
//							Logger.error(e.getMessage(),e);
						}
							
					}
				}
				
				sub1_first=true;
				Field subField2InOneHasOne=null;
				if(oneHasOne) checkKey2ForOneHasOne=new StringBuffer();
				//从表1设置
				Object subObj1 = subEntityFieldClass[0].newInstance();
//				Object subObj1 =null; //不行.   当它的从表在第1位时,就报null
				
				for (int i = 0; i < fields1.length; i++) {

					if (oneHasOne) {
						if (HoneyUtil.isSkipFieldForMoreTable(fields1[i])) continue; //从表1也有1个从表
					} else {
						if (HoneyUtil.isSkipField(fields1[i])) continue;
					}

					boolean isRegHandlerPriority1 = false;
					if (openFieldTypeHandler) {
						isRegHandlerPriority1 = TypeHandlerRegistry.isPriorityType(fields1[i].getType());
					}
					v1 = null;
					fields1[i].setAccessible(true);
					isDul = false;
					dulField = "";
					try {

						if (oneHasOne && fields1[i] != null && fields1[i].isAnnotationPresent(JoinTable.class)) {
							if (subField[1] != null && fields1[i].getName().equals(variableName[1]) && subObj2 != null) {
								fields1[i].setAccessible(true);
								if (subTwoIsList2) {
									subField2InOneHasOne = fields1[i];
								} else {
									fields1[i].set(subObj1, subObj2); //设置子表2的对象     要考虑List. 
								}

								if (sub1_first) {
									sub1_first = false;
								}
							}
							continue; // go back
						}

						String columnName = _toColumnName(fields1[i].getName(), subEntityFieldClass[0]);
						String tempCName1="";
						//get v1
						if (isConfuseDuplicateFieldDB()) {
							dulField = dulSubFieldMap.get(subUseTable[0] + "." + columnName);
							if (dulField != null) {
								isDul = true; //fixed bug.  need set true before fields1[i].set(  )
//								v1 = rs.getObject(dulField);
								tempCName1=dulField;
							} else {
//								v1 = rs.getObject(columnName);
								tempCName1=columnName;
							}
						} else {
//							v1 = rs.getObject(subUseTable[0] + "." + columnName);
							tempCName1=subUseTable[0] + "." + columnName;
						}
						
						//v1=     ;  TODO 2  
//						v1 = rsMap.get(tempCName1);
						v1 =ObjectCreatorFactory.create(rsMap.get(tempCName1), fields1[i].getType());
						
						boolean processAsJson = false;
						if (isJoson(fields1[i])) {
							TypeHandler jsonHandler = TypeHandlerRegistry.getHandler(Json.class);
							if (jsonHandler != null) {
//								v1 = jsonHandler.process(fields1[i].getType(), v1);
								v1 = jsonHandlerProcess(fields1[i], v1, jsonHandler);
								processAsJson = true;
							}
						}

						if (!processAsJson && isRegHandlerPriority1) { //process v1 by handler
							v1 = TypeHandlerRegistry.handlerProcess(fields1[i].getType(), v1);
						}

						if (v1 != null) {
							if (sub1_first) {
								sub1_first = false;
							}
							fields1[i].set(subObj1, v1);
						}
					} catch (IllegalArgumentException e) {
						Logger.error(e.getMessage(),e);
					
//						if(isConfuseDuplicateFieldDB()){
//							v1=_getObjectForMoreTable_ConfuseField(rs,fields1[i],isDul,dulField,subEntityFieldClass[0]);
//						}else{
//							v1=_getObjectForMoreTable_NoConfuse(rs,subUseTable[0],fields1[i],subEntityFieldClass[0]);
//						}
//						
//						
//						boolean alreadyProcess = false;
//						try {
//							if (openFieldTypeHandler) {//process v1 by handler
//								Class type = fields1[i].getType();
//								TypeHandler handler = TypeHandlerRegistry.getHandler(type);
//								if (handler != null) {
//									Object newV1 = handler.process(type, v1);//process v1 by handler
//									if (newV1 != null) {
//										if (sub1_first) {
//											sub1_first = false;
//										}
//										fields1[i].set(subObj1, newV1);
//										alreadyProcess=true;
//									}
//								}
//							}
//						} catch (Exception e2) {
//							alreadyProcess = false;
//						}
//						
//						if (!alreadyProcess && v1 != null) {
//							if (sub1_first) {
//								sub1_first = false;
//							}
//							fields1[i].set(subObj1, v1);
//						}
						
//					}catch (SQLException e) {// for after use condition selectField method
////						fields1[i].set(subObj1,null);
//						Logger.error(e.getMessage(),e);
					}
					
					if(oneHasOne) checkKey2ForOneHasOne.append(v1);
				}   // end for fields1
				
//				if(sub1_first) subObj1=null;  //没有创建过,设置为null
				if(sub1_first && (!oneHasOne || (oneHasOne && sub2_first))) subObj1=null;  //没有创建过,设置为null(是oneHasOne时,子表1里的子表2也是null才行)
				
				
//				Integer id=null;    //配置一个主键是id的项,即可用,效率也高些    行. 有可能只查几个字段
				checkKey=new StringBuffer();   
				Field subOneListField=null;
				Field subTwoListField=null;
				
				//主表设置  oneHasOne can not set here
				targetObj = (T) entity.getClass().newInstance();
				for (int i = 0; i < field.length; i++) {
//					if("serialVersionUID".equals(field[i].getName()) || field[i].isSynthetic()) continue;
					if(HoneyUtil.isSkipFieldForMoreTable(field[i])) continue;  //有Ignore注释,将不再处理JoinTable
					if (field[i]!= null && field[i].isAnnotationPresent(JoinTable.class)) {
						field[i].setAccessible(true);
						if(field[i].getName().equals(variableName[0])){
							if(subOneIsList1) subOneListField=field[i];  //子表1字段是List
							else field[i].set(targetObj,subObj1); //设置子表1的对象
						}else if(!oneHasOne && subField[1]!=null && field[i].getName().equals(variableName[1])){
							//oneHasOne在遍历子表1时设置
							if(subTwoIsList2) subTwoListField=field[i];  
							else field[i].set(targetObj,subObj2); //设置子表2的对象
						}
						continue;  // go back
					}
					
					boolean isRegHandlerPriority = false;
					if (openFieldTypeHandler) {
						isRegHandlerPriority = TypeHandlerRegistry.isPriorityType(field[i].getType());
					}

					field[i].setAccessible(true);
					Object v = null;

					try {
						//get v
						String tempCName="";
						if (isConfuseDuplicateFieldDB()) {
//							v = rs.getObject(_toColumnName(field[i].getName(), entity.getClass()));
							tempCName=_toColumnName(field[i].getName(), entity.getClass());
						} else {
//							v = rs.getObject(tableName + "."+ _toColumnName(field[i].getName(), entity.getClass()));
							tempCName=tableName + "."+ _toColumnName(field[i].getName(), entity.getClass());
						}
//						v=rsMap.get(tempCName);
						v = ObjectCreatorFactory.create(rsMap.get(tempCName), field[i].getType());
						
						boolean processAsJson = false;
						if (isJoson(field[i])) {
							TypeHandler jsonHandler = TypeHandlerRegistry.getHandler(Json.class);
							if (jsonHandler != null) {
//								v = jsonHandler.process(field[i].getType(), v);
								v = jsonHandlerProcess(field[i], v, jsonHandler);
								processAsJson = true;
							}
						}

						if (!processAsJson && isRegHandlerPriority) { //process v by handler
							v = TypeHandlerRegistry.handlerProcess(field[i].getType(), v);
						}

						field[i].set(targetObj, v);
						checkKey.append(v);
					} catch (IllegalArgumentException e) {
						Logger.error(e.getMessage(),e);
					
//						v = _getObjectForMoreTable(rs, tableName, field[i], entity.getClass());

//						boolean alreadyProcess = false;
//						try {
//							if (openFieldTypeHandler) {//process v by handler
//								Class type = field[i].getType();
//								TypeHandler handler = TypeHandlerRegistry.getHandler(type);
//								if (handler != null) {
//									Object newV = handler.process(type, v);//process v by handler
//									field[i].set(targetObj, newV);
//									alreadyProcess = true;
//								}
//							}
//						} catch (Exception e2) {
//							alreadyProcess = false;
//						}
//						if (!alreadyProcess) field[i].set(targetObj, v);
						
					} catch (Exception e) { // for after use condition selectField method
						field[i].set(targetObj, null);
					}
					
				} //end for
				
				
				if(oneHasOne) checkKey2ForOneHasOne.insert(0, checkKey); //主表+从表1
				if(subTwoIsList2 && oneHasOne && subObj1!=null && subField2InOneHasOne!=null) { //for oneHasOne List   oneHasOne 或者 两个都在主表,只会存在其中一种
					List subTwoList = subTwoMap.get(checkKey2ForOneHasOne.toString());  //需要等从表1遍历完,等到完整checkKey2ForOneHasOne
					if (subTwoList == null) { //表示,还没有添加该行记录
						subTwoList=new ArrayList();
						subTwoList.add(subObj2);
						subField2InOneHasOne.set(subObj1, subTwoList);  //subObj1
						subTwoMap.put(checkKey2ForOneHasOne.toString(), subTwoList);
						
//						rsList.add(targetObj);
					} else {
						subTwoList.add(subObj2);
					}
				}
				
				//全是null的数据不会到这里,所以不用判断
				if (subOneIsList1 && subObj1!=null) { //子表1是List类型字段
					List subOneList = subOneMap.get(checkKey.toString());  //需要等主表遍历完,等到完整checkKey
					if (subOneList == null) { //表示主表,还没有添加该行记录
						subOneList=new ArrayList();
						subOneList.add(subObj1);
						subOneListField.set(targetObj, subOneList);
						subOneMap.put(checkKey.toString(), subOneList);
						
						rsList.add(targetObj);
					} else {
						if(!oneHasOne) 
							subOneList.add(subObj1);
						else if(subObj2==null)
							subOneList.add(subObj1);
					}
				}else if(subTwoIsList2 && !oneHasOne && subObj2!=null) {
					List subTwoList = subTwoMap.get(checkKey.toString());  //需要等主表遍历完,等到完整checkKey
					if (subTwoList == null) { //表示主表,还没有添加该行记录
						subTwoList=new ArrayList();
						subTwoList.add(subObj2);
						subTwoListField.set(targetObj, subTwoList);
						subTwoMap.put(checkKey.toString(), subTwoList);
						
						rsList.add(targetObj);
					} else {
						subTwoList.add(subObj2);
					}
				}else {
					rsList.add(targetObj);
				}
				
				
			   } // end while (rs.next())
		
			addInCache(sql, rsList,"List<T>"+listFieldType,SuidType.SELECT,rsList.size());
			
//		} catch (SQLException e) {
//			hasException=true;
//			throw ExceptionHelper.convert(e);
		} catch (IllegalAccessException e) {
			hasException=true;
			throw ExceptionHelper.convert(e);
		} catch (InstantiationException e) {
			hasException=true;
			throw ExceptionHelper.convert(e);
		} finally {
//			closeRs(rs);
			clearContext(sql);
			
//			if (hasException) {
//				checkClose(pst, null);
//				closeConn(conn);
//			} else {
//				checkClose(pst, conn);
//			}
		}

		entity = null;
		targetObj = null;
		
//		子表是List类型时，要连原始数据行数也打印日志
		if(subOneIsList1 || subTwoIsList2)
		   Logger.logSQL(" | <--  ( select raw record rows: ", recordRow + " )");
		logSelectRows(rsList.size());

		return rsList;
	}
	
	
	private static final String[] EMPTY_ARRAY = new String[0];
	
	private String[] toStringArray(String sql) {
		List<PreparedValue> list = HoneyContext.justGetPreparedValue(sql);
		if (list == null) {
			return EMPTY_ARRAY;
		} else {
//			return list.toArray(new String[list.size()]);
			
			String str[]=new String[list.size()];
			Object ob;
			for (int i = 0; i < list.size(); i++) {
				ob=list.get(i).getValue();
				if(ob!=null) str[i]=ob.toString();
				else str[i]=null;
			}
			return str;
		}
	}
	
	private Object[] toObjArray(String sql) {
		return toObjArray(sql, true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object[] toObjArray(String sql,boolean justGet) {
		List<PreparedValue> list;
		if (justGet)
			list = HoneyContext.justGetPreparedValue(sql);
		else
			list = HoneyContext.getAndClearPreparedValue(sql); //用于批处理.
		
		if (list == null) {
			return EMPTY_ARRAY;
		} else {
//			return list.toArray(new Object[list.size()]);
			
			Object obj[]=new Object[list.size()];
//			int typeIndexArray[]=new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				
//				Field f=list.get(i).getField();  //只处理得insert的情形.  update的不能处理.
//				if(f!=null && f.isAnnotationPresent(Json.class)) {
//					typeIndexArray[i]=26; //标识是Json
//				}
				
				
				obj[i]=list.get(i).getValue();
				
				// 提前将Json字段转成Json 字符串????
				Field f = list.get(i).getField();
				if (f != null && f.isAnnotationPresent(Json.class)) {
					SetParaTypeConvert converter = SetParaTypeConverterRegistry.getConverter(Json.class);
					if (converter != null) {
						obj[i] = converter.convert(obj[i]);
					}
				}
			}
			
			return obj;
		}
	}
	
}
