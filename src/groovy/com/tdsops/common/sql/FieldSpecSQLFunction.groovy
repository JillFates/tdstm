package com.tdsops.common.sql

import org.hibernate.QueryException
import org.hibernate.dialect.function.SQLFunction
import org.hibernate.engine.spi.Mapping
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type

/**
 * Implementation of a custom {@code SQLFuntion} to be used in Dataviews. It can convert "cast" function for decimal formats
 * Following the following HQL Sentence:
 * <pre>
 * select
 *   AE.id,
 *   AE.assetName,
 *   AE.environment,
 *   cast(AE.custom10 as long),
 *   cast_big_decimal(AE.custom13, 2),
 *   cast(AE.custom11 as date),
 *   cast(AE.custom12 as timestamp),
 *   str(AE.assetClass)
 * from AssetEntity AE
 *   left outer join AE.moveBundle
 * where AE.project = :project and AE.assetClass in (:assetClasses)
 * group by AE.id
 * order by cast(AE.custom10 as long) desc
 * </pre>
 * <p>This class is in charged to convert <pre>cast_big_decimal(AE.custom13, 2)</pre> in <pre>cast(assetentit0_.custom13 as decimal(12,2)</pre></p>
 * <p>For tha reason, it is necessary 2 parameters:</p>
 * <ul>
 *  <li>First, the property to be used in this custom cast function</li>
 * 	<li>Second, precision digits taken from field specs</li>
 * </ul>
 *
 */
class FieldSpecSQLFunction implements SQLFunction {

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
		return StandardBasicTypes.BIG_DECIMAL
	}

	/**
	 * Prepares custom cast function for dealing with big decimals in an HQL sentence.
	 *
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

		String field = (String) arguments.get(0)
		String precision = (String) arguments.get(1)
		return "cast($field as decimal(12,$precision))"
	}
}
