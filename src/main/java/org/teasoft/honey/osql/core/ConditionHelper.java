/*
 * Copyright 2016-2019 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.FunctionType;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.OrderType;
import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.dialect.DbFeature;
import org.teasoft.bee.osql.exception.BeeErrorGrammarException;
import org.teasoft.bee.osql.exception.BeeIllegalSQLException;
import org.teasoft.honey.osql.core.ConditionImpl.FunExpress;
import org.teasoft.honey.osql.dialect.sqlserver.SqlServerPagingStruct;
import org.teasoft.honey.util.StringUtils;

/**
 * Condition的帮助类.Condition Helper class.
 * @author Kingstar
 * @since  1.6
 */
public class ConditionHelper {
//	static boolean isNeedAnd = true;    //bug 2021-10-14   not thread safe
	private static final String ONE_SPACE = " ";

	private static final String setAdd = "setAdd";
	private static final String setMultiply = "setMultiply";
	
	private static final String setAddField = "setAddField";
	private static final String setMultiplyField = "setMultiplyField";
	
	private static final String setWithField="setWithField";
	
	private static final String GROUP_BY = "groupBy";
	private static final String HAVING = "having";
	
	private ConditionHelper(){}
	
	private static DbFeature getDbFeature() {
		return BeeFactory.getHoneyFactory().getDbFeature();
	}
	
	//ForUpdate
//	static boolean processConditionForUpdateSet(StringBuffer sqlBuffer, StringBuffer valueBuffer, List<PreparedValue> list, Condition condition) {
	static boolean processConditionForUpdateSet(StringBuffer sqlBuffer, List<PreparedValue> list, Condition condition) { //delete valueBuffer
		ConditionImpl conditionImpl = (ConditionImpl) condition;
		List<Expression> updateSetList = conditionImpl.getUpdateExpList();
		boolean firstSet = true;
		
		Class entityClass = (Class) OneTimeParameter.getAttribute(StringConst.Column_EC);

//		if ( setAdd.equalsIgnoreCase(opType) || setMultiply.equalsIgnoreCase(opType) ) {
		if (updateSetList != null && updateSetList.size() > 0) {
			if (SuidType.UPDATE != conditionImpl.getSuidType()) {
				throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support the method set ,setAdd or setMultiply!");
			}
		}

		PreparedValue preparedValue = null;
		Expression expression = null;

		for (int j = 0; updateSetList!=null && j < updateSetList.size(); j++) {
			expression = updateSetList.get(j);
			String opType = expression.getOpType();

//				update orders set total=total+0.5;
//				mysql is ok. as below:
//				update orders set total=total+?   [values]: -0.1  
			
			if (opType!=null && expression.getValue() == null) {  // BUG  // UPDATE,  fieldName: toolPayWay, the num of null is null
//				throw new BeeErrorGrammarException(conditionImpl.getSuidType() + ", method:"+opType+", fieldName:"+expression.getFieldName()+", the value is null");
				throw new BeeErrorGrammarException("the value is null ("+conditionImpl.getSuidType() + ", method:"+opType+", fieldName:"+expression.getFieldName()+")!");
//			    setWithField("name",null);   //这种,不在这里抛出,字段检测时会抛
//				String n=null; setAdd("total", n); //这个也是.第二个参数是作为字段,会被检测
			} else {

				if (firstSet) {
					firstSet = false;
				} else {
					sqlBuffer.append(",");
				}
				sqlBuffer.append(_toColumnName(expression.getFieldName(), null,entityClass));
				sqlBuffer.append("=");
				
				//v1.9.8
				if(opType==null && expression.getValue() == null) { //set("fieldName",null)
					sqlBuffer.append(K.Null);
					continue;
				}
				
				if(opType!=null) { //只有set(arg1,arg2) opType=null
					if (setWithField.equals(opType)) {
						sqlBuffer.append(_toColumnName((String)expression.getValue(),entityClass));
					}else {
						sqlBuffer.append(_toColumnName(expression.getFieldName(),entityClass));  //price=[price]+delta   doing [price]
					}
				}
				   
				
				if (setAddField.equals(opType)) {//eg:setAdd("price","delta")--> price=price+delta
					sqlBuffer.append("+");
					sqlBuffer.append(_toColumnName((String)expression.getValue(),entityClass));
					continue; //no ?,  don't need set value
				} else if (setMultiplyField.equals(opType)) {
					sqlBuffer.append("*");
					sqlBuffer.append(_toColumnName((String)expression.getValue(),entityClass));
					continue; //no ?,  don't need set value
				}
				
				if (setAdd.equals(opType)) {
//					if ((double) expression.getValue() < 0)
//						sqlBuffer.append("-"); // bug 负负得正
//					else
					sqlBuffer.append("+");
				} else if (setMultiply.equals(opType)) {
					sqlBuffer.append("*");
				}
				
				if (setWithField.equals(opType)) {
                      //nothing 
					  //for : set field1=field2
				} else {
					sqlBuffer.append("?");

//					valueBuffer.append(","); // do not need check. at final will delete the first letter.
//					valueBuffer.append(expression.getValue());

					preparedValue = new PreparedValue();
					preparedValue.setType(expression.getValue().getClass().getName());
					preparedValue.setValue(expression.getValue());
					list.add(preparedValue);
				}
			}

		}

		return firstSet;
	}
	
	static boolean processCondition(StringBuffer sqlBuffer, 
		 List<PreparedValue> list, Condition condition, boolean firstWhere) {
//		 StringBuffer valueBuffer=new StringBuffer(); //don't use, just adapt the old method
//		 return processCondition(sqlBuffer, valueBuffer, list, condition, firstWhere, null);
		 return processCondition(sqlBuffer, list, condition, firstWhere, null);
	}
	
	//v1.7.2  add return value for delete/update control
//	static boolean processCondition(StringBuffer sqlBuffer, StringBuffer valueBuffer, 
//			List<PreparedValue> list, Condition condition, boolean firstWhere) {
//		
////		 return processCondition(sqlBuffer, valueBuffer, list, condition, firstWhere, null);
//		 return processCondition(sqlBuffer, list, condition, firstWhere, null);
//	}
	//v1.7.2  add return value for delete/update control
	static boolean processCondition(StringBuffer sqlBuffer, 
			List<PreparedValue> list, Condition condition, boolean firstWhere,String useSubTableNames[]) {
		
		Class entityClass = (Class) OneTimeParameter.getAttribute(StringConst.Column_EC);
		
		if(condition==null) return firstWhere;
		
		PreparedValue preparedValue = null;
		boolean isNeedAnd = true;
		
		boolean isFirstWhere=firstWhere; //v1.7.2 return for control whether allow to delete/update whole records in one table

		ConditionImpl conditionImpl = (ConditionImpl) condition;
		List<Expression> expList = conditionImpl.getExpList();
		Expression expression = null;
		
		Integer start = conditionImpl.getStart();
		
		if (start!=null && SuidType.SELECT != conditionImpl.getSuidType()) {
			throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support paging with start !");
		} 
		String columnName="";
		for (int j = 0; j < expList.size(); j++) {
			expression = expList.get(j);
			String opType = expression.getOpType();
			
			columnName=_toColumnName(expression.getFieldName(),useSubTableNames,entityClass);
			
			if ( GROUP_BY.equalsIgnoreCase(opType) || HAVING.equalsIgnoreCase(opType) ) {
				if (SuidType.SELECT != conditionImpl.getSuidType()) {
					throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support the opType: "+opType+"!");
				} 
			}
			//mysql's delete,update can use order by.

			if (firstWhere) {
				if ( GROUP_BY.equalsIgnoreCase(opType) || HAVING.equalsIgnoreCase(opType) || "orderBy".equalsIgnoreCase(opType)) {
					firstWhere = false;
				} else {
//					sqlBuffer.append(" where ");
					sqlBuffer.append(" ").append(K.where).append(" ");
					firstWhere = false;
					isNeedAnd = false;
					isFirstWhere=false; //for return. where过滤条件
				}
			}
			//			} else {
			if (Op.in.getOperator().equalsIgnoreCase(opType) || Op.notIn.getOperator().equalsIgnoreCase(opType)) {
				
//				String v = expression.getValue().toString(); //close in V1.17
				Object v = expression.getValue();
				
//				if(StringUtils.isBlank(v)) continue; //v1.9.8    in的值不允许为空             这样会有安全隐患, 少了一个条件,会更改很多数据.
				
				isNeedAnd=adjustAnd(sqlBuffer,isNeedAnd);
				sqlBuffer.append(columnName);
//				sqlBuffer.append(" ");
//				sqlBuffer.append(expression.getOpType());
				if(HoneyUtil.isSqlKeyWordUpper()) sqlBuffer.append(expression.getOpType().toUpperCase());
				else sqlBuffer.append(expression.getOpType());
				sqlBuffer.append(" (");
				sqlBuffer.append("?");
				int len=1;
				if (v == null) {
					PreparedValue p = new PreparedValue();
					p.setValue(null);
					p.setType(Object.class.getName());
					list.add(p);
				} else if (List.class.isAssignableFrom(v.getClass())
						|| Set.class.isAssignableFrom(v.getClass())) { // List,Set
					Collection<?> c = (Collection<?>) v;
					len = c.size();
					for (Object e : c) {
						setPreValue(list, e);
					}
				} else if (HoneyUtil.isNumberArray(v.getClass())) { // Number Array
					Number n[] = (Number[]) v;
					len = n.length;
					for (Number number : n) {
						setPreValue(list, number);
					}
				} else if (String.class.equals(v.getClass())) { // String 逗号(,)为分隔符
					Object values[] = v.toString().trim().split(",");
					len = values.length;
					for (Object e : values) {
						setPreValue(list, e);
					}
				} else { // other one elements
					setPreValue(list, v);
				}
				
				for (int i = 1; i < len; i++) { //start 1
					sqlBuffer.append(",?");
				}

				sqlBuffer.append(")");

//				valueBuffer.append(","); //valueBuffer
//				valueBuffer.append(expression.getValue());

				isNeedAnd = true;
				continue;
			} else if (Op.like.getOperator().equalsIgnoreCase(opType) || Op.notLike.getOperator().equalsIgnoreCase(opType)) {
//				else if (opType == Op.like  || opType == Op.notLike) {
//				adjustAnd(sqlBuffer);
				isNeedAnd=adjustAnd(sqlBuffer,isNeedAnd);

				sqlBuffer.append(columnName);
//				sqlBuffer.append(expression.getOpType());
				if(HoneyUtil.isSqlKeyWordUpper()) sqlBuffer.append(expression.getOpType().toUpperCase());
				else sqlBuffer.append(expression.getOpType());
				sqlBuffer.append("?");

//				valueBuffer.append(","); //valueBuffer
//				valueBuffer.append(expression.getValue());

				String v = (String) expression.getValue();
				if (v != null) {
					Op op = expression.getOp();
					if (Op.likeLeft == op) {
						checkLikeEmptyException(v);
						v = "%" + StringUtils.escapeLike(v);
					} else if (Op.likeRight == op) {
						checkLikeEmptyException(v);
						v = StringUtils.escapeLike(v) + "%";
					} else if (Op.likeLeftRight == op) {
						checkLikeEmptyException(v);
						v = "%" + StringUtils.escapeLike(v) + "%";
					} else { // Op.like
						if (StringUtils.justLikeChar(v)) {
							throw new BeeIllegalSQLException("Like has SQL injection risk! " + columnName + " like '" + v+"'");
						}
					}
				} else {
                  Logger.warn("the parameter value in like is null !",new BeeIllegalSQLException());
				}
				
				preparedValue = new PreparedValue();
				if(v==null) preparedValue.setType(Object.class.getName());
				else preparedValue.setType(expression.getValue().getClass().getName());
//				preparedValue.setValue(expression.getValue());
				preparedValue.setValue(v);
				list.add(preparedValue);

				isNeedAnd = true;
				continue;
			} else if (" between ".equalsIgnoreCase(opType) || " not between ".equalsIgnoreCase(opType)) {

//				adjustAnd(sqlBuffer);
				isNeedAnd=adjustAnd(sqlBuffer,isNeedAnd);

				sqlBuffer.append(columnName);
				sqlBuffer.append(opType);
				sqlBuffer.append("?");
				sqlBuffer.append(" "+K.and+" ");
				sqlBuffer.append("?");

//				valueBuffer.append(","); //valueBuffer
//				valueBuffer.append(expression.getValue()); //low
//				valueBuffer.append(","); //valueBuffer
//				valueBuffer.append(expression.getValue2()); //high

				preparedValue = new PreparedValue();
				preparedValue.setType(expression.getValue().getClass().getName());
				preparedValue.setValue(expression.getValue());
				list.add(preparedValue);

				preparedValue = new PreparedValue();
				preparedValue.setType(expression.getValue2().getClass().getName());
				preparedValue.setValue(expression.getValue2());
				list.add(preparedValue);

				isNeedAnd = true;
				continue;

			} else if (GROUP_BY.equalsIgnoreCase(opType)) {
				if (SuidType.SELECT != conditionImpl.getSuidType()) {
					throw new BeeErrorGrammarException("BeeErrorGrammarException: "+conditionImpl.getSuidType() + " do not support 'group by' !");
				}

				sqlBuffer.append(expression.getValue());//group by或者,
				sqlBuffer.append(columnName);

				continue;
			} else if (HAVING.equalsIgnoreCase(opType)) {
				if (SuidType.SELECT != conditionImpl.getSuidType()) {
					throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support 'having' !");
				}

//				if (2 == expression.getOpNum()) {//having("count(*)>5")
//					sqlBuffer.append(expression.getValue());//having 或者 and
//					sqlBuffer.append(expression.getValue2()); //表达式
//				} else if (5 == expression.getOpNum()) { //having(FunctionType.MIN, "field", Op.ge, 60)
				if (5 == expression.getOpNum()) { //having(FunctionType.MIN, "field", Op.ge, 60)
					sqlBuffer.append(expression.getValue());//having 或者 and
//					sqlBuffer.append(expression.getValue3()); //fun
					sqlBuffer.append(FunAndOrderTypeMap.transfer(expression.getValue3().toString())); //fun
					sqlBuffer.append("(");
					if (FunctionType.COUNT.getName().equals(expression.getValue3()) && "*".equals(expression.getFieldName().trim())) {
						sqlBuffer.append("*");
					} else {
						sqlBuffer.append(columnName);
					}

					sqlBuffer.append(")");
					sqlBuffer.append(expression.getValue4()); //Op
					//		                  sqlBuffer.append(expression.getValue2()); 
					sqlBuffer.append("?");

//					valueBuffer.append(",");
//					valueBuffer.append(expression.getValue2()); // here is value2

					preparedValue = new PreparedValue();
					preparedValue.setType(expression.getValue2().getClass().getName());
					preparedValue.setValue(expression.getValue2());
					list.add(preparedValue);
				}

				continue;
			}else if ("orderBy".equalsIgnoreCase(opType)) {

				if (SuidType.SELECT != conditionImpl.getSuidType()) {
					throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support 'order by' !");
				}

				sqlBuffer.append(expression.getValue());//order by或者,
				if (4 == expression.getOpNum()) { //order by max(total)
//					sqlBuffer.append(expression.getValue3());
					sqlBuffer.append(FunAndOrderTypeMap.transfer(expression.getValue3().toString()));
					sqlBuffer.append("(");
					sqlBuffer.append(columnName);
					sqlBuffer.append(")");
				} else {
					sqlBuffer.append(columnName);
				}

				if (3 == expression.getOpNum() || 4 == expression.getOpNum()) { //指定 desc,asc
					sqlBuffer.append(ONE_SPACE);
//					sqlBuffer.append(expression.getValue2());
					sqlBuffer.append(FunAndOrderTypeMap.transfer(expression.getValue2().toString()));
					
//					//V1.17
//					//SqlServer 2012版之前的复杂分页语法需要判断
//					if(!orderByIdDescInSqlServer && start>1 && HoneyUtil.isSqlServer()) {
//						pkName="";
//						try {
//							entityClass.getDeclaredField("id");
//							pkName="id";
//						} catch (NoSuchFieldException e) {
//							pkName = HoneyUtil.getPkFieldNameByClass(entityClass).split(",")[0]; //有多个,只取第一个
//						}
//						
//						String pkColumnName=_toColumnName(pkName,useSubTableNames,entityClass);
//						// 1判断是否是主键  // 2判断是否是DESC
//						if(pkColumnName.equalsIgnoreCase(columnName)) {
//							if("desc".equalsIgnoreCase(expression.getValue2().toString())) {
//								//需要调整内部分页排序
//								orderByIdDescInSqlServer=true;
//							}
//						}
//					}
					
				}
				continue;
			}//end orderBy

			if (expression.getOpNum() == -2) { // (
//				adjustAnd(sqlBuffer);
				isNeedAnd=adjustAnd(sqlBuffer,isNeedAnd);
				sqlBuffer.append(expression.getValue());
				continue;
			}
			if (expression.getOpNum() == -1) {// )
				sqlBuffer.append(expression.getValue());
				isNeedAnd = true;
				continue;

			} else if (expression.getOpNum() == 1) { // or operation 
				sqlBuffer.append(" ");
				sqlBuffer.append(expression.getValue());
				sqlBuffer.append(" ");
				isNeedAnd = false;
				continue;
			}
//			adjustAnd(sqlBuffer);
			isNeedAnd=adjustAnd(sqlBuffer,isNeedAnd);

			//}

			sqlBuffer.append(columnName);  

			if (expression.getValue() == null) {
				if("=".equals(expression.getOpType())){
//					sqlBuffer.append(" is null");
					sqlBuffer.append(" "+K.isNull);
				}else{
					sqlBuffer.append(" "+K.isNotNull);
					if(! "!=".equals(expression.getOpType())) {
						String fieldName=columnName;
						Logger.warn(fieldName+expression.getOpType()+"null transfer to : " +fieldName+" "+K.isNotNull);
					}
				}
			} else {
				if (expression.getOpNum() == -3) { //eg:field1=field2   could not use for having in mysql 
					sqlBuffer.append(expression.getOpType());
					sqlBuffer.append(expression.getValue());
				} else {
					sqlBuffer.append(expression.getOpType());
					sqlBuffer.append("?");

//				    valueBuffer.append(",");
//				    valueBuffer.append(expression.getValue());

					preparedValue = new PreparedValue();
					preparedValue.setType(expression.getValue().getClass().getName());
					preparedValue.setValue(expression.getValue());
					list.add(preparedValue);
				}
			}
			isNeedAnd = true;
		} //end expList for 

		//>>>>>>>>>>>>>>>>>>>paging start
		if (SuidType.SELECT == conditionImpl.getSuidType()) {
			if (! OneTimeParameter.isTrue(StringConst.Select_Fun)) {
				Integer size = conditionImpl.getSize();
				String sql = "";
				if (start != null && size != null) {
					HoneyUtil.regPagePlaceholder();
					
					// V1.17 sql server paging
					Map<String, String> orderByMap = conditionImpl.getOrderByMap();
					adjustSqlServerPagingIfNeed(sqlBuffer, orderByMap, start, entityClass, useSubTableNames);
					
					sql = getDbFeature().toPageSql(sqlBuffer.toString(), start, size);
//			        sqlBuffer=new StringBuffer(sql); //new 之后不是原来的sqlBuffer,不能带回去.
					sqlBuffer.delete(0, sqlBuffer.length());
					sqlBuffer.append(sql);
					HoneyUtil.setPageNum(list);
					
				} else if (size != null) {
					HoneyUtil.regPagePlaceholder();
					
					// V1.17 sql server paging
					Map<String, String> orderByMap = conditionImpl.getOrderByMap();
					adjustSqlServerPagingIfNeed(sqlBuffer, orderByMap, 0, entityClass, useSubTableNames); //start=0,只用于2012的offset语法

					sql = getDbFeature().toPageSql(sqlBuffer.toString(), size);
//			        sqlBuffer=new StringBuffer(sql);
					sqlBuffer.delete(0, sqlBuffer.length());
					sqlBuffer.append(sql);
					HoneyUtil.setPageNum(list);
				}
			}
		}
		//>>>>>>>>>>>>>>>>>>>paging end
		

		//>>>>>>>>>>>>>>>>>>>for update
		//仅用于SQL的单个表select
		if (useSubTableNames==null && SuidType.SELECT == conditionImpl.getSuidType()) {
			
			Boolean isForUpdate=conditionImpl.getForUpdate();
			if(isForUpdate!=null && isForUpdate.booleanValue()){
//				sqlBuffer.append(" for update ");
				sqlBuffer.append(" "+K.forUpdate+" ");
			}
		}
		//>>>>>>>>>>>>>>>>>>>for update
		
		
		//check
		if (SuidType.SELECT == conditionImpl.getSuidType()) {
			List<Expression> updateSetList = conditionImpl.getUpdateExpList();
			if (updateSetList != null && updateSetList.size() > 0) {
				Logger.warn("Use Condition's set method(s) in SELECT type, but it just effect in UPDATE type! Involved field(s): "+conditionImpl.getUpdatefields());
			}
		}
		
		return isFirstWhere;
	}
	
	private static void checkLikeEmptyException(String value) {
		if ("".equals(value))
			throw new BeeIllegalSQLException("Like has SQL injection risk! the value can not be empty string!");
	}
	
	private static void setPreValue(List<PreparedValue> list, Object value) {
		PreparedValue preparedValue = new PreparedValue();
		preparedValue.setValue(value);
		preparedValue.setType(value.getClass().getName());
		list.add(preparedValue);
	}
	
	
	// V1.17 for Sql Server,分页需要
	private static void adjustSqlServerPagingIfNeed(StringBuffer sqlBuffer,
			Map<String, String> orderByMap, Integer start, Class entityClass, String useSubTableNames[]) {
		
		if (!HoneyUtil.isSqlServer()) return ;
		
		SqlServerPagingStruct struct=new SqlServerPagingStruct();
		
		boolean needAdjust = false;
		boolean justChangePk=false;
		String pkName = "id";
		int majorVersion=HoneyConfig.getHoneyConfig().getDatabaseMajorVersion();
		// 要是参数没有condition,或condition为null,则使用默认排序.
		if (HoneyUtil.isSqlServer()) {
			if (orderByMap.size() > 0) { // 2012版之前的复杂分页语法需要判断. 之后的语法有order by即可.
				struct.setHasOrderBy(true);
//				orderByMap有值时,offset语法,只需要将默认order by id删除.
				if (majorVersion >= 11) {
					needAdjust = true;
				}else if(start > 1) {//// 2012版之前的复杂分页语法,两个参数,要是有主键倒序,则要调整
					String order = orderByMap.get("id");
					if (order != null) {
						pkName = "id";
						if ("desc".equals(order)) {
							needAdjust = true;
							struct.setOrderType(OrderType.DESC);
						}
					}

					if (!needAdjust) {// 测试名称不叫id的主键
						String pkName0 = HoneyUtil.getPkFieldNameByClass(entityClass);
						if (!"".equals(pkName0)) {
							pkName = pkName0.split(",")[0]; // 有多个,只取第一个
							order = orderByMap.get(pkName);
							if (order != null) {
								if ("desc".equals(order)) {
									needAdjust = true;
									struct.setOrderType(OrderType.DESC);
								}else {
									justChangePk=true;    //只要更改主键名
									struct.setJustChangeOrderColumn(true);
								}
							}
						}
					}
				}
			}else {//检测是否要更改主键名
				String pkName0 = HoneyUtil.getPkFieldNameByClass(entityClass);
				if (!"".equals(pkName0)) {
					pkName = pkName0.split(",")[0]; // 有多个,只取第一个
					justChangePk=true;
					struct.setJustChangeOrderColumn(true);
				}
			}
		}
		
		pkName=_toColumnName(pkName,useSubTableNames,entityClass);
		if(pkName.contains(".")) {
			justChangePk=true;
			struct.setJustChangeOrderColumn(true);
		}
		
		struct.setOrderColumn(pkName);
		//保存struct
		HoneyContext.setSqlServerPagingStruct(sqlBuffer.toString(), struct); //作为key的sql不是最终sql;因此处理后,一般就要先分页
	}
	
	static String processSelectField(String columnNames, Condition condition) {
		return processSelectField(columnNames, condition, null);
	}
	
	static String processSelectField(String columnNames, Condition condition,Map<String,String> subDulFieldMap) {
		
		if(condition==null) return null;

		ConditionImpl conditionImpl = (ConditionImpl) condition;
		if (SuidType.SELECT != conditionImpl.getSuidType()) {
			throw new BeeErrorGrammarException(conditionImpl.getSuidType() + " do not support specifying partial fields by method selectField(String) !");
		}
		String selectField[] = conditionImpl.getSelectField();

		if (selectField == null) return null;

		return HoneyUtil.checkAndProcessSelectFieldViaString(columnNames, subDulFieldMap, selectField);
	}
	
	public static String processFunction(String columnNames,Condition condition) {
//		if(condition==null) return null;

		ConditionImpl conditionImpl = (ConditionImpl) condition;
		List<FunExpress> funExpList=conditionImpl.getFunExpList();
		String columnName;
		String funStr="";
		boolean isFirst=true;
		String alias;
		for (int i = 0; i < funExpList.size(); i++) {
			if("*".equals(funExpList.get(i).getField())) {
				columnName="*";
			}else {
				columnName = HoneyUtil.checkAndProcessSelectFieldViaString(columnNames, null, funExpList.get(i).getField());
			}
			if(isFirst) {
				isFirst=false;
			}else {
				funStr+=",";
			}
//			funStr+=funExpList.get(i).getFunctionType().getName()+"("+columnName+")"; // funType要能转大小写风格
//			String functionTypeName=funExpList.get(i).getFunctionType().getName();
			String functionTypeName=funExpList.get(i).getFunctionType();
			funStr+=FunAndOrderTypeMap.transfer(functionTypeName)+"("+columnName+")"; 
			
			alias=funExpList.get(i).getAlias();
			if(StringUtils.isNotBlank(alias)) funStr+=" "+K.as+" "+alias;
		}
		
		return funStr;
	}
	
	public static void processOnExpression(Condition condition, MoreTableStruct moreTableStruct[],
			List<PreparedValue> list) {
		Class entityClass = (Class) OneTimeParameter.getAttribute(StringConst.Column_EC);
		if (condition == null || moreTableStruct == null) return;
		
		List<PreparedValue> list2=new ArrayList<>();

		ConditionImpl conditionImpl = (ConditionImpl) condition;
		List<Expression> onExpList = conditionImpl.getOnExpList();
		StringBuffer onExpBuffer = new StringBuffer();
		Expression exp = null;
		int sub1 = 0, sub2 = 0;
		for (int i = 0; i < onExpList.size(); i++) {

			exp = onExpList.get(i);
			if (moreTableStruct[0].joinTableNum == 1 && i != 0) {
				onExpBuffer.append(K.space).append(K.and).append(K.space);
			}
			onExpBuffer.append(_toColumnName(exp.getFieldName(),entityClass));
			onExpBuffer.append(K.space);
			onExpBuffer.append(exp.opType);
			//			onExpBuffer.append(K.space);
			//			onExpBuffer.append(exp.getValue());
			onExpBuffer.append("?");

			if (moreTableStruct[0].joinTableNum == 2) {
				String fieldName = exp.getFieldName();
				if (fieldName.startsWith(moreTableStruct[2].tableName + ".")
						|| (moreTableStruct[2].hasSubAlias
								&& fieldName.startsWith(moreTableStruct[2].subAlias + "."))) { //第2个从表
					if (sub2 != 0) moreTableStruct[2].onExpression += K.space + K.and + K.space;
					moreTableStruct[2].onExpression += onExpBuffer.toString();
					sub2++;
					addValeToList(list2, exp);
				} else {
					if (sub1 != 0) moreTableStruct[2].onExpression += K.space + K.and + K.space;
					moreTableStruct[1].onExpression += onExpBuffer.toString();
					sub1++;
					addValeToList(list, exp);
				}
				if (i != onExpList.size() - 1) onExpBuffer = new StringBuffer();
			} else {
				addValeToList(list, exp);
			}

			if (i == onExpList.size() - 1) {
				if (moreTableStruct[0].joinTableNum == 1)
					moreTableStruct[1].onExpression = onExpBuffer.toString();
				else if (sub2 != 0) list.addAll(list2);
			}
		}

	}
	
	private static void addValeToList(List<PreparedValue> list,Expression exp) {
		PreparedValue preparedValue = new PreparedValue();
		preparedValue.setType(exp.getValue().getClass().getName());
		preparedValue.setValue(exp.getValue());
		list.add(preparedValue);
	}
	
	
	private static String _toColumnName(String fieldName,Class entityClass) {
		return NameTranslateHandle.toColumnName(fieldName,entityClass);
	}
	
	private static String _toColumnName(String fieldName,String useSubTableNames[],Class entityClass) {
		if(StringUtils.isBlank(fieldName)) return fieldName;
		if(!fieldName.contains(",")) return _toColumnName0(fieldName, useSubTableNames,entityClass);
		
		String str[]=fieldName.split(",");
		String newFields="";
		int len=str.length;
		for (int i = 0; i < len; i++) {
			newFields+=_toColumnName0(str[i],useSubTableNames,entityClass);
			if(i!=len-1) newFields+=",";
		}
		return newFields;
		
	}
			
	private static String _toColumnName0(String fieldName,String useSubTableNames[],Class entityClass) {
		
		if(useSubTableNames==null) return NameTranslateHandle.toColumnName(fieldName,entityClass);   //one table type
		
		String t_fieldName="";
		String t_tableName="";
		String t_tableName_dot;
		String find_tableName="";
		int index=fieldName.indexOf('.');
		if(index>-1){
			t_fieldName=fieldName.substring(index+1);
			t_tableName=fieldName.substring(0,index);
			t_tableName_dot=fieldName.substring(0,index+1);
			// check whether is useSubTableName
			if(useSubTableNames[0]!=null && useSubTableNames[0].startsWith(t_tableName_dot)){
				find_tableName=t_tableName;
			}else if(useSubTableNames[1]!=null && useSubTableNames[1].startsWith(t_tableName_dot)){
				find_tableName=t_tableName;
			}else{
				OneTimeParameter.setTrueForKey(StringConst.DoNotCheckAnnotation);//adjust for @Table
				find_tableName=NameTranslateHandle.toTableName(t_tableName);
			}
			
			return find_tableName+"."+NameTranslateHandle.toColumnName(t_fieldName,entityClass);
		}else {
			fieldName=useSubTableNames[2]+"."+fieldName;
		}
		return NameTranslateHandle.toColumnName(fieldName,entityClass);
	}

	private static boolean adjustAnd(StringBuffer sqlBuffer,boolean isNeedAnd) {
		if (isNeedAnd) {
			sqlBuffer.append(" "+K.and+" ");
			isNeedAnd = false;
		}
		return isNeedAnd;
	}
	
	public static Integer getPageSize(Condition condition) {
		if(condition==null) return null;
		ConditionImpl conditionImpl = (ConditionImpl) condition;
		return conditionImpl.getSize();
	}
}
