package com.tdsops.common.sql

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

	static String parseParameter(String prop, String expr, Map params, Class clazz) {

		expr = expr.trim()

		String firstChar = expr[0]
		String rest = expr.substring(1).trim()

		String queryString

		switch (firstChar) {
			/* Starts with '=' */
			case '=':
				queryString = buildSingleValueParameter(prop, rest, '=', params)
				break

			/* Starts with '<' or '>' */
			case ['<', '>']:
				/* Starts with '<=' or '>=' */
				if (rest[0] == '=') {
					queryString = buildSingleValueParameter(prop, rest.substring(1), firstChar + '=', params)
				/* Starts with '<>' */
				}else if (expr ==~ /^<>.*/) {
					queryString = buildDistinctParameter(prop, rest.substring(1), params, clazz)
				/* Starts with '<' or '>' */
				}else{
					queryString = buildSingleValueParameter(prop, rest, firstChar, params)
				}

				break

			/* Starts with '-' or '!' */
			case ['-', '!']:
				switch(rest) {
					/* Start with '-' or '!' and it's a list of ':' separated values. */
					case ~ /.*:.*/:
						queryString = buildLikeList(prop, rest, 'NOT LIKE', 'AND', ':', params)
						break
					/* Start with '-' or '!' and it's a list of '|' separated values. */
					case ~ /.*\|.*/:
						queryString = buildInList(prop, 'NOT IN', rest, params)
						break
					/* Starts with '-' or '!' and it isn't a list. */
					default:
						/* If there are wildcards it must be handled as a LIKE operation. */
						if (isOverriding(rest)) {
							// Parses de parameter accordingly.
							queryString = buildDistinctParameter(prop, rest.replaceAll('\\*', '%'), params, clazz, true)
						/* Starts with '-' and no overriding required. */
						} else if (firstChar == '-') {
							queryString = buildDistinctParameter(prop, rest, params, clazz)
						/* Starts with '!' and it's not a list of values and no overriding required. */
						}else{
							queryString = buildSingleValueParameter(prop, rest, '<>', params)
						}

						break
				}
				break
			default:
					switch(expr) {
						/* It's a list of '&' separated values. */
						case ~ /.*&.*/:
							queryString = buildLikeList(prop, expr, 'LIKE', 'AND', '&', params)
							break
						/* It's a list of '|' separated values. */
						case ~ /.*\|.*/:
							queryString = buildInList(prop, 'IN', expr, params)
							break
						/* It's a list of ':' separated values. */
						case ~ /.*:.*/:
							queryString = buildLikeList(prop, expr, 'LIKE', 'OR', ':', params)
							break
						/* It starts and ends with double quotation marks. */
						case ~ /^\'.*\'/:
							queryString = buildSingleValueParameter(prop, expr.substring(1, expr.length() - 1), '=', params)
							break
						/* Default scenario (overriding may be required). */
						default:
							queryString = buildSingleValueParameter(prop, parseStringParameter(expr, true), 'LIKE', params)
							break
					}
					break
		}

		return queryString
	}

	/**
	 * Determines whether the user is overriding the default filter logic.
	 */
	 private static boolean isOverriding(String expr) {
	 	expr ==~ /.*[\*%].*/
	 }

	/**
	 * Constructs simple not-like expressions for number and string parameters.
	 */
	private static String buildDistinctParameter(String prop, String value, Map params, Class clazz, boolean checkOverriding = false) {
		// TODO Analyze column type
		boolean fieldTypeIsNumber = isSubclassOf(fieldType(clazz, prop), Number)
		if (value.isNumber() && fieldTypeIsNumber) {
			buildSingleValueParameter(prop, parseNumberParameter(value), '<>', params)
		}
		else {
			buildSingleValueParameter(prop, parseStringParameter(value, checkOverriding), 'NOT LIKE', params)
		}
	}

	private static String buildInList(String prop, String inOp, String expr, Map params) {

		def values = []
		expr.split('\\|').eachWithIndex{ String value, int index ->
			String paramKey = prop + index
			values << ':' + paramKey
			params[paramKey] = escapeStringParameter(value)
		}
		return prop + ' ' + inOp + '(' + values.join(',') + ')'
	}

	private static String buildLikeList(String prop, String expr, String likeOp, String logicalOp, String separator, Map params) {
		List<String> conditions = []
		expr.split(separator).eachWithIndex{ String value, int index ->
			conditions << buildSingleValueParameter(prop + index, parseStringParameter(value, false), likeOp, params, prop)
		}
		conditions.join(" $logicalOp ")
	}

	/**
	 * Constructs a simple expression like 'parameter = value', 'parameter <> value'
	 */
	private static String buildSingleValueParameter(String prop, value, String operator, Map params, String propName = prop) {
		params[prop] = value
		propName + ' ' + operator + ' :' + prop
	}

	/**
	 * Escape the given parameter.
	 * @param parameter
	 * @return
	 */
	static String escapeStringParameter(String parameter) {
		return StringEscapeUtils.escapeJava(parameter.toString().trim())
	}

	/**
	 * Escapes the parameter accordingly and determines whether or not
	 * it should include whildcards.
	 */
	private static String parseStringParameter(String parameter, boolean checkOverride) {
		String before = '%'
		String after = '%'
		if (checkOverride && isOverriding(parameter)) {
			before = after = ''
			parameter = parameter.replaceAll('\\*', '%')
		}
		return before + escapeStringParameter(parameter) + after
	}

	private static Number parseNumberParameter(String parameter) {
		parameter.isInteger() ? parameter.toInteger() : parameter.toDouble()
	}

	private static Class fieldType(Class clazz, String field) {
		return clazz.metaClass.properties.find { it.name == field }.type
	}

	private static boolean isSubclassOf(Class clazz, Class superClazz) {
		return superClazz.isAssignableFrom(clazz)
	}
}

