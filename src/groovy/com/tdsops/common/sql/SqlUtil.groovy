package com.tdsops.common.sql

import com.tdssrc.grails.NumberUtil
import net.transitionmanager.search.FieldSearchData
import com.tdssrc.grails.StringUtil
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

class SqlUtil {
	public static final String COMMA = ","
	public static final String STRING_QUOTE = "'"
	public static final String LEFT_PARENTHESIS = "("
	public static final String RIGHT_PARENTHESIS = ")"
	public static final String NULL = "null"

	/**
	 * Helper class that allows the concatenation of sql with and/or which only adds the logic if the query already contains content
	 * @param query - the string buffer that contains the current WHERE clause
	 * @param criteria - the additional criteria to append to the query (StringBuffer or String)
	 * @param andOr - the boolean identifier (default 'and')
	 * @return String concatenated string
	 */
	static String appendToWhere(String query, String additional, String andOr='and') {
		return query + (query ? ' ' + andOr + ' ' : '') + additional
	}

	/**
	 * Used to append a clause to a SQL query string and account for the AND/OR prefix boolean
	 * @param query - the string buffer that contains the current WHERE clause
	 * @param criteria - the additional criteria to append to the query (StringBuffer or String)
	 * @param andOr - the boolean identifier (default 'and')
	 */
	static void appendToWhere(StringBuffer query, CharSequence criteria, String andOr = 'and') {
		if (query) query << ' ' << andOr << ' '
		query << criteria
	}
	static void appendToWhere(StringBuilder query, CharSequence criteria, String andOr = 'and') {
		if (query) query << ' ' << andOr << ' '
		query << criteria
	}

	/**
	 * Used to do a multiple word match against a particular field
	 * @param property - the property to query on
	 * @param words - a list of words to search on
	 * @param matchAll - a boolean flag when true will require that the match must be on all words (default false)
	 * @param exact - a boolean flag when true uses equal match else uses like (default false)
	 * @return The query criteria that was constructed
	 * @example
	 * <pre>
	 * assert SqlUtil.matchWords('a', ['x','y','z'], true, false) == '(a like ? and a like ? and a like ?)'
	 * assert SqlUtil.matchWords('a', ['x','y','z'], false, true) == '(a=? or a=? or a=?)'
	 */
	static String matchWords(String property, List words, boolean matchAll=false, boolean exact=false) {
		String andOr = matchAll ? 'and' : 'or'
		StringBuilder query = new StringBuilder()
		String criteria = exact ? property + '=?' : property + ' like ?'
		int size = words.size()
		if (size) {
			query << criteria
			if (size > 1) {
				query << StringUtils.repeat(' ' + andOr + ' ' + criteria, --size)
			}
		}

		// Return the query or '(1=1)' if there were no words so that the query is still legitimate
		return '(' + (query ?: '1=1') + ')'
	}

	/**
	 * Used to wrap a list of words with the like percent markers (e.g. %word%)
	 * @param words - a list of wordds
	 * @return the formated list of words
	 */
	static List<String> formatForLike(List words) {
		words.collect { '%' + it + '%' }
	}

	/**
	 * Used to generate the WHERE expression for a particular property in which based on the criteria will create an EQUALS, IN or LIKE based on
	 * the criteria value. The options are as such for when criteria is:
	 *  * An array - it will create an IN clause
	 *  * A string:
	 *    * If it contains a '%' sign it will create a LIKE statement
	 *    * Otherwise it will append the critera to the property name therefore it must include the conditional expression (e.g. "= 'Y'")
	 *  * A number - it will create an EQUALS statement
	 *
	 * @param property  property name (e.g. 'a.lastName)
	 * @param criteria (e.g. '5', '>=5', 'a%', ['a','b'])
	 * @param paramName  used for parameterized variables in QUERY
	 * @param isNot flag that will add NOT to the LIKE and IN expressions
	 * @return Map[sql:param] where the SQL is the parameterized SQL and the param contains the value used as the parameterize value. The BETWEEN
	 * 		expression does not return a parameter as there needs to be two params and not presently supported.
	 */
	static Map whereExpression(String property, criteria, String paramName, boolean isNot = false) {
		String not = isNot ? ' NOT ' : ' '

		if (criteria instanceof CharSequence) {
			if (criteria.contains('%')) {
				// LIKE expression
				return [sql: property + not + 'LIKE :' + paramName, param: criteria]
			}

			if (criteria.toLowerCase() ==~ /^between /) {
				// BETWEEN expression
				return [sql: property + not + criteria]
			}

			if ('!<>='.contains(criteria.substring(0, 1))) {
				// BOOLEAN expression
				String param = StringUtil.stripOffPrefixChars('!<>=', criteria)
				String expr = criteria.substring(0, criteria.size() - param.size())
				return [sql: property + ' ' + expr + ' :' + paramName, param: param]
			}

			// default EQUALS condition
			return [sql: property + ' = :' + paramName, param: criteria]
		}

		if (criteria instanceof Integer) {
			return [sql: property + ' ' + (isNot ? '<>' : '=') + ' :' + paramName, param: criteria]
		}

		if (criteria instanceof List) {
			return [sql: property + not + 'IN (:' + paramName + ')', param: criteria]
		}

		if (criteria instanceof Enum) {
			return [sql: property + ' = :' + paramName, param: criteria]
		}

		if (DomainClassArtefactHandler.isDomainClass(criteria.getClass())) {
			return [sql: property + ' = :' + paramName, param: criteria]
		}

		throw new RuntimeException("whereExpression() received criteria of unsupported class type (${criteria?.class}) for property $property")
	}

	/**
	 * Used to parse user input from filters so that we can create the appropriate SQL expression that will support boolean expressions
	 * like <, <=, >, >= or - to cause a NOT filter
	 * @param String text - the filter value
	 * @param String defExpr - the default expression to use if the filter doesn't include a filter
	 * @return List [text, expression] - the text with the expression stripped off and the appropriate expression
	 */
	static List parseExpression(String text, String defExpr = '=') {
		String expr = defExpr
		boolean not = false

		text = text.trim()

		// Get the NOT (-) switch
		if (text[0] == '-') {
			not = true
			text = StringUtils.substring(text, 1).trim()
		}

		if (['<=', '>='].contains(StringUtils.substring(text, 0, 2))) {
			expr = StringUtils.substring(text, 0, 2)
			text = StringUtils.substring(text, 2).trim()
		} else if ('<>='.contains(StringUtils.substring(text, 0, 1))) {
			expr = text[0]
			text = StringUtils.substring(text, 1).trim()
		}

		// Handle placing NOT on the expression
		if (not) {
			//println "parseExpression() in NOT handler, expr=$expr xxx"
			if (expr == '=') {
				expr = '<>'
			}
			else if (expr.toLowerCase() == 'like') {
				expr = 'NOT ' + expr
			}
		}

		//println "parseExpression() text '$text', expr '$expr'"
		return [text, expr]
	}

	/*

	// TODO - move these assertions to a testcase
	def t,e
	(t,e) = parseExpression('<5')
	assert t=='5'
	assert e=='<'

	(t,e) = parseExpression('< 5 ')
	assert t=='5'
	assert e=='<'

	(t,e) = parseExpression('<=5')
	assert t=='5'
	assert e=='<='

	(t,e) = parseExpression('-5')
	assert t=='5'
	assert e=='<>'

	(t,e) = parseExpression('>G')
	assert t=='G'
	assert e=='>'

	(t,e) = parseExpression('G', 'like')
	assert t=='G'
	assert e=='like'

	(t,e) = parseExpression('-G', 'like')
	assert t=='G'
	assert e=='NOT like'
	*/

	/**
	 * Used for filtering a domain class with a filter on a field.
	 *
	 * Possible Expressions:
	 *
	 *	Scenario 1:
	 * 		"=something" => prop = "something"
	 * 		"=50"		=> prop = 50 or prop = "50" (depending on field's data type).
	 * 	Scenario 2 (also applicable to Strings):
	 * 		"<=40"	=>	prop <= 40
	 * 		">=40"	=>	prop >= 40
	 * 	Scenario 3:
	 * 		"<>50"			=> prop <> 50
	 * 		"<>something"	=>	prop NOT LIKE "%something%"
	 * 	Scenario 4 (also applicable to Strings):
	 * 		"<50"	=>	prop < 50
	 * 		">50"	=>	prop > 50
	 * 	Scenario 5:
	 * 		!ab:bc	=> prop NOT LIKE "%ab%" AND prop NOT LIKE "%bc%"
	 * 		-ab:bc	=> prop NOT LIKE "%ab%" AND prop NOT LIKE "%bc%"
	 * 	Scenario 6:
	 * 		!ab|bc	=>	prop NOT IN ('ab', 'bc')
	 * 		-ab|bc	=>	prop NOT IN ('ab', 'bc')
	 * 	Scenario 7:
	 * 		-ab		=> prop NOT LIKE "%ab%"
	 * 		-50		=> prop <> 50
	 * 	Scenario 8:
	 * 		!ab		=> prop <> "ab"
	 * 	Scenario 9:
	 * 		ab & bc	=> prop LIKE "ab" AND prop LIKE "bc"
	 * 	Scenario 10:
	 * 		ab | bc	=> prop IN ("ab", "bc")
	 * 	Scenario 11:
	 * 		ab : bc	=> prop LIKE "%ab%" OR prop LIKE "%bc%"
	 * 	Scenario 12:
	 * 		"ab"	=> prop = "ab"
	 * 	Scenario 13:
	 * 		ab		=> prop LIKE "%ab%"
	 * 		ab*		=> prop LIKE "ab%"
	 * 		*ab		=> prop LIKE "%ab"
	 * 		*ab*	=> prop LIKE "%ab%"
	 *
	 *
	 *
	 *
	 * @param fieldSearchData
	 */
	static void parseParameter(FieldSearchData fieldSearchData) {

		String errMsg = fieldSearchData.validate()
		if (errMsg) {
			throw new RuntimeException(errMsg)
		}

		String originalFilter = fieldSearchData.filter

		int filterSize = originalFilter.size()

		/* We're handling 1-char long filters individually because the
		* switch below has cases where it asks for the second char, which
		* would break the app. */
		if (filterSize == 1) {
			fieldSearchData.useWildcards = true
			buildSingleValueParameter(fieldSearchData, 'LIKE')
			return
		}

		// At this point we know that the filter is at least 2-char long.

		switch (originalFilter[0]) {
		/* Scenario 1: Starts with '=' */
			case '=':
				fieldSearchData.filter = originalFilter.substring(1)
				buildSingleValueParameter(fieldSearchData, '=')
				break

		/* Starts with '<' or '>' */
			case ['<', '>']:

				/* Scenario 2: Starts with '<=' or '>=' */
				if (originalFilter[1] == '=') {
					String operator = originalFilter.substring(0, 2)
					fieldSearchData.filter = originalFilter.substring(2)
					buildSingleValueParameter(fieldSearchData, operator)

					/* Scenario 3: Starts with '<>' */
				}else if (originalFilter ==~ /^<>.*/) {
					fieldSearchData.filter = originalFilter.substring(2)
					buildDistinctParameter(fieldSearchData)

					/* Scenario 4: Starts with '<' or '>' and any literal follows. */
				}else{
					fieldSearchData.filter = originalFilter.substring(1)
					String operator = originalFilter[0]
					buildSingleValueParameter(fieldSearchData, operator)
				}

				break

		/* Starts with '-' or '!' */
			case ['-', '!']:

				String rest = originalFilter.substring(1)
				fieldSearchData.filter = rest

				switch(rest) {

				/* Scenario 5: Start with '-' or '!', and it's a list of ':' separated values. */
					case ~ /.*:.*/:
						buildLikeList(fieldSearchData, 'NOT LIKE', 'AND', ':')
						break

				/* Scenario 6: Start with '-' or '!' and it's a list of '|' separated values. */
					case ~ /.*\|.*/:
						buildInList(fieldSearchData, 'NOT IN')
						break

				/* Starts with '-' or '!' and it isn't a list. */
					default:
						fieldSearchData.filter = rest
						/* Scenario 7: Starts with '-' and it isn't a list. */
						if (originalFilter[0] == '-') {
							buildDistinctParameter(fieldSearchData)

							/* Scenario8 : Starts with '!' and it's not a list of values. */
						} else {
							buildSingleValueParameter(fieldSearchData, '<>')
						}

						break
				}
				break

			default:
				switch(originalFilter) {

				/* Scenario 9: It's a list of '&' separated values. */
					case ~ /.*&.*/:
						buildLikeList(fieldSearchData, 'LIKE', 'AND', '&')
						break

				/* Scenario 10: It's a list of '|' separated values. */
					case ~ /.*\|.*/:
						buildInList(fieldSearchData, 'IN')
						break

				/* Scenario 11: It's a list of ':' separated values. */
					case ~ /.*:.*/:
						buildLikeList(fieldSearchData, 'LIKE', 'OR', ':')
						break

				/* Scenario 12: It starts and ends with double quotation marks. */
					case ~ /^\".*\"/:
						fieldSearchData.filter = originalFilter.substring(1, filterSize - 1)
						buildSingleValueParameter(fieldSearchData, '=')
						break

				/* Scenario 13: Default scenario (overriding may be required). */
					default:
						fieldSearchData.useWildcards = true
						buildSingleValueParameter(fieldSearchData, 'LIKE')
						break
				}
				break
		}

	}

	/**
	 * Constructs simple not like/not equal expressions for number and string parameters.
	 *
	 * - When handling numbers the resulting expression will be column <> aNumber.
	 * - When handling strings, the expression is column NOT LIKE "%aString%"
	 */
	private static void buildDistinctParameter(FieldSearchData fieldSearchData) {

		// At this point the filter is just the value to lookup.
		String value = fieldSearchData.filter

		// If the column is a number and also the value, handle this and a numeric expression.
		if (value.isNumber() && isNumericField(fieldSearchData)) {
			buildSingleValueParameter(fieldSearchData, '<>')
		} // If not a numeric expression, handle it as a NOT LIKE expression.
		else {
			fieldSearchData.useWildcards = true
			buildSingleValueParameter(fieldSearchData, 'NOT LIKE')
		}
	}

	/**
	 * Build a SQL IN or NOT IN expression.
	 *
	 * @param fieldSearchData
	 * @param inOp
	 */
	private static void buildInList(FieldSearchData fieldSearchData, String inOp) {
		def values = []
		fieldSearchData.filter.split('\\|').eachWithIndex{ String value, int index ->
			// Use underscores to avoid issues when handling sme, potentially conflicting with sme2
			String namedParameter = "${fieldSearchData.columnAlias}__" + index
			values << ':' + namedParameter
			fieldSearchData.addSqlSearchParameter(namedParameter, escapeStringParameter(value))
		}

		fieldSearchData.sqlSearchExpression = fieldSearchData.column + ' ' + inOp + ' (' + values.join(', ') + ')'
	}

	/**
	 * Construct a list of LIKE or NOT LIKE expressions concatenated by a logical operator (AND/OR).
	 * @param fieldSearchData
	 * @param likeOp
	 * @param logicalOp
	 * @param separator
	 */
	private static void buildLikeList(FieldSearchData fieldSearchData, String likeOp, String logicalOp, String separator) {
		List<String> expressions = []
		fieldSearchData.useWildcards = true

		fieldSearchData.filter.split(separator).eachWithIndex{ String value, int index ->
			// Use underscores to avoid issues when handling sme, potentially conflicting with sme2
			String namedParameter = "${fieldSearchData.columnAlias}__" + index
			// Parse the string parameter considering user wildcards.
			String parsedParameter = parseStringParameter(value, fieldSearchData.useWildcards)
			String sqlExpression = getSingleValueExpression(fieldSearchData.column, namedParameter, likeOp )
			fieldSearchData.addSqlSearchParameter(namedParameter, parsedParameter)
			expressions << sqlExpression
		}
		// Surround with () to avoid issues when mixing ANDs and ORs
		fieldSearchData.sqlSearchExpression = "(${expressions.join(" $logicalOp ")})"
	}

	/**
	 * Build a simple "column + operator + parameter" used in different places.
	 *
	 * @param column
	 * @param namedParameter
	 * @param operator
	 * @return
	 */
	private static String getSingleValueExpression(String column, String namedParameter, String operator) {
		return column + ' ' + operator + ' :' + namedParameter
	}

	/**
	 * Constructs a simple expression like 'parameter = value', 'parameter <> value'
	 */
	private static void buildSingleValueParameter(FieldSearchData fieldSearchData, String operator) {

		Object paramValue

		if (isNumericField(fieldSearchData)) {
			if (fieldSearchData.filter.isNumber()) {
				paramValue = parseNumberParameter(fieldSearchData)
			}
		} else {
			paramValue = parseStringParameter(fieldSearchData.filter, fieldSearchData.useWildcards)
		}

		// Calculate the expression only if there's a valid value to be added to the query.
		if (paramValue) {
			String expression = getSingleValueExpression(fieldSearchData.column, fieldSearchData.columnAlias, operator)
			fieldSearchData.sqlSearchExpression = expression
			fieldSearchData.addSqlSearchParameter(fieldSearchData.columnAlias, paramValue)
		}
	}
	/**
	 * Escape the parameter to avoid SQL Injection attacks.
	 * @param parameter
	 * @return
	 */
	static String escapeStringParameter(String parameter) {
		return StringEscapeUtils.escapeJava(parameter.toString().trim())
	}

	/**
	 * Parses the string parameter and replaces wildcards accordingly.
	 * @param value - the parameter
	 * @param useWildcards - flag if wildcards should be used.
	 * @return
	 */
	private static String parseStringParameter(String value, boolean useWildcards) {
		String before = ''
		String after = ''
		value = value.trim()
		if (useWildcards) {
			switch (value) {
			// Ends with the wildcard and starts with anything else.
				case ~ /[^*|%].*[*|%]/:
					value = value.substring(0, value.size() - 1)
					after = "%"
					break
			// Starts with the wildcard and ends with anything else.
				case ~ /[*|%].*[^*|%]/:
					value = value.substring(1)
					before = "%"
					break
			// Starts and ends with the wildcard
				case ~ /[*|%].*[*|%]/:
					value = value.substring(1, value.size() - 1)
				default:
					before = after = "%"
					break
			}

		}

		return before + escapeStringParameter(value) + after
	}

	/**
	 * Return the numeric representation for a String (Long or Double).
	 *
	 * @param parameter
	 * @return numeric representation or null if the parameter is not a number.
	 */
	private static Number parseNumberParameter(FieldSearchData fsd) {
		Class type = fsd.getType()
		String filter = fsd.getFilter()
		def parsedNumber = null
		if (filter.isNumber()) {
			if (type == Integer) {
				parsedNumber = NumberUtil.toInteger(filter)
			} else if (type == Long) {
				parsedNumber = NumberUtil.toLong(filter)
			} else {
				parsedNumber = filter.toFloat()
			}
		}
		return parsedNumber

	}

	/**
	 * Determine if the field being used for filtering is numeric.
	 * @param fsd
	 * @return
	 */
	private static boolean isNumericField(FieldSearchData fsd) {
		boolean isNumeric = false
		def properties = fsd.domain.metaClass.properties
		// Look up the field type using the column.
		Class fieldType = properties.find { it.name == fsd.column }?.type
		// If it couldn't be found, try with the columnAlias
		if (!fieldType) {
			fieldType = properties.find { it.name == fsd.columnAlias }?.type
		}

		// If we could determine the class, check if it is a subclass of Number
		if(fieldType) {
			isNumeric = Number.isAssignableFrom(fieldType)
		}

		return isNumeric

	}

	/**
	 * Used to generate HQL that will properly concatenate a person's full name
	 * @param propertyName - the particular property to construct the HQL for
	 * @return the HQL for the computed fullName
	 */
	static String personFullName(String propertyName = "", String table = null) {
		if (propertyName) {
			if (table != null) {
				propertyName = "${table}.${propertyName}"
			}
			propertyName = "${propertyName}."
		}

		return """
				CONCAT( 
					COALESCE(${propertyName}firstName, ''),
					CASE WHEN COALESCE(${propertyName}middleName, '') = '' THEN '' ELSE ' ' END,
					COALESCE(${propertyName}middleName,''),
					CASE WHEN COALESCE(${propertyName}lastName, '') = '' THEN '' ELSE ' ' END,
					COALESCE(${propertyName}lastName,'')
				)
				"""
	}

}

