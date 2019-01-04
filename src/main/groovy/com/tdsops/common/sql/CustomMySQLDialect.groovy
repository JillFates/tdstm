package com.tdsops.common.sql

import net.transitionmanager.dataview.FieldSpec
import org.hibernate.dialect.MySQL5InnoDBDialect
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.StringType

class CustomMySQLDialect extends MySQL5InnoDBDialect {

	public CustomMySQLDialect() {
		registerFunction("group_concat", new StandardSQLFunction("group_concat", StringType.INSTANCE))
		registerFunction("concat_ws", new StandardSQLFunction("concat_ws", StringType.INSTANCE))
		registerFunction("json_object", new StandardSQLFunction("json_object", StringType.INSTANCE))
		registerFunction(FieldSpec.CAST_BIG_DECIMAL_FUNCTION, new BigDecimalSQLFunction())
		registerFunction(FieldSpec.CAST_DATE_TIME_FUNCTION, new DateTimeSQLFunction())
		registerFunction(FieldSpec.CAST_LONG_FUNCTION, new LongSQLFunction())
	}
}
