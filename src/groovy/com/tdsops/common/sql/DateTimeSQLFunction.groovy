package com.tdsops.common.sql

import org.hibernate.QueryException
import org.hibernate.dialect.function.SQLFunction
import org.hibernate.engine.spi.Mapping
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type

/**
 * Implementation of a custom {@code SQLFunction} to be used in Dataviews. It can convert "cast" function for date and datetime formats
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
 * <p>This class is in charged to convert
 * <pre>cast_date_time(AE.custom19, '%Y-%m-%d')</pre>
 * in
 * <pre>CASE WHEN assetentit0_.custom13 != '' THEN STR_TO_DATE(assetentit0_.custom13, '%Y-%m-%d'') ELSE NULL END</pre>
 * <p>For tha reason, it is necessary 2 parameters:</p>
 * <ul>
 *  <li>First, the property to be used in this custom cast function</li>
 * 	<li>Second, format taken from field specs definition</li>
 * </ul>
 *  @see net.transitionmanager.dataview.FieldSpec#CAST_DATE_TIME_FUNCTION
 */
class DateTimeSQLFunction implements SQLFunction {

	@Override
	boolean hasArguments() {
		return false
	}

	@Override
	boolean hasParenthesesIfNoArguments() {
		return false
	}

	@Override
	Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
		return StandardBasicTypes.TIMESTAMP
	}

	/**
	 * <p>Prepares custom cast function for dealing with datetime values in an HQL sentence.</p>
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

		if (arguments.size() < 2) {
			throw new IllegalArgumentException("The function must be passed 2 arguments")
		}

		String date = (String) arguments.get(0)
		String format = (String) arguments.get(1)
		return "CASE WHEN $date != '' THEN STR_TO_DATE($date, $format) ELSE NULL END"
	}
}
