/*
 * Copyright 2016-2020 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.distribution.ds;

import org.teasoft.bee.distribution.ds.Route;
import org.teasoft.honey.osql.core.HoneyConfig;
import org.teasoft.honey.osql.core.HoneyContext;

/**
 * @author Kingstar
 * @since  1.8
 */
public final class Router {

	private static Route route = null;

	private static int multiDsType;
	private static String defaultDs;

	static {
		init();
	}
	
	private Router() {}
	
	private static void init(){
		multiDsType = HoneyConfig.getHoneyConfig().multiDS_type;
		defaultDs = HoneyConfig.getHoneyConfig().multiDS_defalutDS;

		if (multiDsType == 1) {
			route = new RwDs();
		} else if (multiDsType == 2) {
			route = new OnlyMulitiDB();
		}
		
//	要定义一个通用多数据源路由?? 不需要.  不使用特殊的1和2类型,即为一般类型. 比如可以在Suid的当前对象设置数据源.
	}

	//order:1.appointDS -> 2.tempDS(suid.getDataSourceName()) -> 3.route.getDsName()
	public static String getDsName() {
		if (HoneyContext.isConfigRefresh()) {
			refresh();
			HoneyContext.setConfigRefresh(false);
		}
		String dsName =HoneyContext.getAppointDS();
		if (dsName != null) return dsName;
		
		dsName =HoneyContext.getTempDS(); //for Suid.setDataSourceName(String dsName) and so on
		if (dsName != null) return dsName;
		
		if (route == null) return defaultDs;

		return route.getDsName();
	}

	public static void refresh() {
		init();  //refresh all model
	}

}
