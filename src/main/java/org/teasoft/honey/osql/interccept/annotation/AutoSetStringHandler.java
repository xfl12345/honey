/*
 * Copyright 2016-2022 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.interccept.annotation;

import java.lang.reflect.Field;

import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.annotation.customizable.AnnotationHandler;
import org.teasoft.bee.osql.annotation.customizable.AutoSetString;
import org.teasoft.honey.osql.core.Logger;

/**
 * @author Kingstar
 * @since  1.11-E
 */
public class AutoSetStringHandler {

	private AutoSetStringHandler() {

	}

	public static void process(Field field, Object entity, SuidType sqlSuidType) {

		try {
			AutoSetString anno = field.getAnnotation(AutoSetString.class);
			boolean override = anno.override();
			SuidType setSuidType = anno.suidType();

			if (!(setSuidType == sqlSuidType || setSuidType == SuidType.SUID
					|| (setSuidType == SuidType.MODIFY && (sqlSuidType == SuidType.UPDATE
							|| sqlSuidType == SuidType.INSERT || sqlSuidType == SuidType.DELETE))))
				return; //操作类型不对,则返回

			field.setAccessible(true);
			if (!override && field.get(entity) != null) { //不允许覆盖,原来有值则返回
				return;
			}

			Class c = anno.handler(); //获取具体实现的处理器
			AnnotationHandler obj = (AnnotationHandler) c.newInstance();
			field.set(entity, obj.process());

		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

}
