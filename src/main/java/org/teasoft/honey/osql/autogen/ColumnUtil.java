/*
 * Copyright 2016-2021 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.autogen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.teasoft.honey.osql.core.HoneyUtil;

/**
 * @author Kingstar
 * @since  1.9.8
 */
public class ColumnUtil {

	public static List<ColumnBean> getColumnList(String tableName) {
		GenBean bean = new GenBean(null);
		Table table = bean.getTableInfo(tableName);

		if (table == null || table.getColumnNames() == null) return Collections.emptyList();

		List<String> columnNames = table.getColumnNames();
		List<String> columnTypes = table.getColumnTypes(); //jdbcType
		Map<String, String> commentMap = table.getCommentMap();
		List<Boolean> ynNulls = table.getYnNulls();
		String newTableName = table.getTableName();
		String tableComment=commentMap.get(newTableName);

		List<ColumnBean> list = new ArrayList<>();
		ColumnBean columnBean = null;

		for (int i = 0; i < columnNames.size(); i++) {
			String columnName = columnNames.get(i);
			String columnType = columnTypes.get(i);
			boolean ynNull = ynNulls.get(i) == null ? true : ynNulls.get(i);
			String comment = commentMap.get(columnName);
//			if (StringUtils.isBlank(comment)) comment = columnName;

			columnBean = new ColumnBean();
			columnBean.setName(columnName);
			columnBean.setType(HoneyUtil.getFieldType(columnType)); //java type
			columnBean.setLabel(comment);
			columnBean.setYnnull(ynNull);
			columnBean.setTablename(newTableName);
			columnBean.setTablecomment(tableComment);
			columnBean.setYnkey(isKey(columnName,table.getPrimaryKeyNames()));

			list.add(columnBean);
		}

		return list;
	}
	
	private static boolean isKey(String col,Map<String, String> primaryKeyMap) {
        if(primaryKeyMap==null || col==null) return false;
		return primaryKeyMap.get(col)!=null? true: false;
	}

}
