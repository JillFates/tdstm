package com.tdsops.common.sql

import org.hibernate.QueryException
import org.hibernate.dialect.function.SQLFunction
import org.hibernate.engine.spi.Mapping
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type

/**
 * Implementation of a custom {@code SQLFunction} to be used in Dataviews. It can convert "cast" function for non decimal formats
 * Following the following HQL Sentence:
 * <pre>
 * select AE.id,
 *        AE.assetName,
 *        cast_long(AE.custom10),
 *        cast_big_decimal(AE.custom18, 2),
 *        cast_date_time(AE.custom19, '%Y-%m-%d'),
 *        cast_date_time(AE.custom20, '%Y-%m-%dT%TZ'),
 *        str(AE.assetClass)
 * from AssetEntity AE
 * where AE.project = :project
 *   and AE.assetClass in (:assetClasses)
 * group by AE.id
 * order by AE.id asc
 * </pre>
 * <p>This class is in charged to convert <pre>cast_long(AE.custom10)</pre> in <pre>CASE WHEN $field != '' THEN CONVERT(assetentit0_.custom10,UNSIGNED INTEGER) ELSE NULL END</pre></p>
 * <p>For that reason, it is necessary 1 parameter:</p>
 * <ul>
 *  <li>First, the property to be used in this custom cast function</li>
 * </ul>
 */
class LongSQLFunction implements SQLFunction {

	@Override
	boolean hasArguments() {
		return true
	}

	@Override
	boolean hasParenthesesIfNoArguments() {
		return true
	}

	@Override
	Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
		return StandardBasicTypes.LONG
	}

	/**
	 * <p>Prepares custom cast function for dealing with non decimal numeric values in an HQL sentence.</p>
	 * <p>It prepares a MySQL sentence using specific functions.</p>
	 * <p>It uses 'CASE' function to avoid casting on empty String content</p>
	 * @param firstArgumentType
	 * @param arguments
	 * @param factory
	 * @return
	 * @throws QueryException
	 */
	@Override
	String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {

		if (arguments.size() < 1) {
			throw new IllegalArgumentException("The function must be passed 1 arguments")
		}

		String field = (String) arguments.get(0)
		return "CASE WHEN $field != '' THEN CONVERT($field,UNSIGNED INTEGER) ELSE NULL END"
	}
}
