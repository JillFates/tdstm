package com.tdsops.common.sql

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.search.FieldSearchData
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.StringEscapeUtils
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
	 * @param criteria - the additional criteria to append to the query (StringBuilder or String)
	 * @param andOr - the boolean identifier (default 'and')
	 * @return String concatenated string
	 */
	static String appendToWhere(String query, String additional, String andOr='and') {
		return query + (query ? ' ' + andOr + ' ' : '') + additional
	}

	/**
	 * Used to append a clause to a SQL query string and account for the AND/OR prefix boolean
	 * @param query - the string buffer that contains the current WHERE clause
	 * @param criteria - the additional criteria to append to the query (StringBuilder or String)
	 * @param andOr - the boolean identifier (default 'and')
	 */
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

		// Check for many-to-many before attempting one of the standard filterings.
		if (fieldSearchData.isManyToMany()) {
			handleManyToMany(fieldSearchData)
			return
		}

		if (isEnumerationField(fieldSearchData)) {
			handleEnumerationField(fieldSearchData)
			return
		}

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
				} else if (originalFilter ==~ /^<>.*/) {
					fieldSearchData.filter = originalFilter.substring(2)
					buildDistinctParameter(fieldSearchData)

					/* Scenario 4: Starts with '<' or '>' and any literal follows. */
				} else {
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
	 * <p>It handles Enumeration filtering in Asset views.</p>
	 * <p>Using an instance of {@code FieldSearchData} it can prepare the HQL part that manages enumeration filters.</p>
	 * After calculate several scenarios, it can save that result in {@code fieldSearchData.sqlSearchExpression}
	 *
	 * @param fieldSearchData an instance of {@code FieldSearchData}
	 */
	private static void handleEnumerationField(FieldSearchData fieldSearchData) {

		String operator = 'IN'
		fieldSearchData.useWildcards = true

		switch (fieldSearchData.filter) {
			/* Scenario 1: Starts with '!=' */
			case ~/^(!=).*/:
				fieldSearchData.filter = fieldSearchData.filter.substring(2)
				fieldSearchData.useWildcards = false
				operator = 'NOT IN'
				break

			/* Scenario 2: Starts with '=' */
			case ~/^(=).*/:
				fieldSearchData.filter = fieldSearchData.filter.substring(1)
				fieldSearchData.useWildcards = false
				operator = 'IN'
				break

			/* Scenario 3: Starts with '-' or '!' */
			case ~/^(-|!).*/:
				fieldSearchData.filter = fieldSearchData.filter.substring(1)
				operator = 'NOT IN'
				break
		}

		Object paramValue = parseEnumParameter(fieldSearchData)
		if (paramValue) {
			String expression = getSingleValueExpression(fieldSearchData.whereProperty, fieldSearchData.columnAlias, operator)
			if (operator == 'NOT IN') {
				fieldSearchData.sqlSearchExpression = '( ' + expression + ' OR ' + fieldSearchData.whereProperty + ' IS NULL )'
			} else {
				fieldSearchData.sqlSearchExpression = expression
			}

			fieldSearchData.addSqlSearchParameter(fieldSearchData.columnAlias, paramValue)
		} else {
			// Particular Scenario. Because we are filtering here and not in the SQL sentence directly,
			// we need to solve those cases where filter does not match with any of the enumeration values.
			// Then, to manage this case, we simply add a false where clause, expecting an empty list of results.
			fieldSearchData.sqlSearchExpression = " 1 = 0"
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

		fieldSearchData.sqlSearchExpression = fieldSearchData.whereProperty + ' ' + inOp + ' (' + values.join(', ') + ')'
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
			String sqlExpression = getSingleValueExpression(fieldSearchData.whereProperty, namedParameter, likeOp )
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
		String searchColumn = fieldSearchData.whereProperty
		Object paramValue

		if (isNumericField(fieldSearchData)) {
			if (fieldSearchData.filter.isNumber()) {
				paramValue = parseNumberParameter(fieldSearchData)
			}
		} else { // we treat the field as a String
			paramValue = parseStringParameter(fieldSearchData.filter, fieldSearchData.useWildcards)
		}

		// Calculate the expression only if there's a valid value to be added to the query.
		if (paramValue != null) {
			String expression = getSingleValueExpression(searchColumn, fieldSearchData.columnAlias, operator)
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
	 * Parse Enum parameters from {@code FieldSearchData}
	 * @param value
	 * @param useWildcards
	 * @return a list of values to be used filtering {@code Enum} values
	 */
	private static List parseEnumParameter(FieldSearchData fsd) {
		Class type = fsd.getType()
		String regex = convertFilterToRegex(fsd)
		List coincidences = type.values().findAll { enumeration ->
			String enumValue = enumeration.value
			if (fsd.useWildcards) {
				enumValue = enumValue.toLowerCase()
			}
			enumValue =~ /$regex/
		}
		return coincidences?:null
	}

	/**
	 * Transform a String filter typed by user in a valid regular expression
	 * <pre>
	 *     'P%'      ^P.*
	 *     '%obyte'  .*obyte$
	 *     '%obyt*'  .*obyte.*
	 *     '%ob*t*'  .*ob.*te.*
	* </pre>
	 * @param filter
	 * @return
	 */
	static String convertFilterToRegex(FieldSearchData fsd) {

		String filter = fsd.getFilter()
		String begining = '^'
		String end = '$'

		// useWildcards = false means use exact match
		// useWildcards = true means use not exact match
		if (fsd.useWildcards) {
			filter = filter.toLowerCase()
			begining = '.*'
			end = '.*'
		}
		// 1) Check if filter contains only characters.
		// If this is the case, we can filter using the most generic expression
		// e.g.: Mega --> .*Mega.*
		if (!filter.contains('|') && !filter.contains(':') && !filter.contains('&') &&
			!filter.contains('?') && !filter.contains('%') && !filter.contains('$') &&
			!filter.contains('(') && !filter.contains(')') && !filter.contains('[') &&
			!filter.contains(']') && !filter.contains('{') && !filter.contains('}') &&
			!filter.contains('*')
		) {
			return begining + filter + end
		}

		// 2) Escape  regular expression characters / ' () ? .* $ {}
		String specialCharRegex = /[\$\?\.\{\}\(\)\[\]]/
		String regex = filter.replaceAll(specialCharRegex, '\\\\$0')

		// 3) Translate % or * characters for the .* - any character sequence
		regex = regex.replace('*', '.*')
		regex = regex.replace('%', '.*')

		// 4) Check if user type an 'Or' expression using '|' character
		if (filter.contains('|')) {
			return begining + '(' + filter + ')' + end
		}
		// 5) Check if user type an 'OR' expression using ':' character
		// 'Petabyte'.matches(/.*(by|Peta).*/)
		if (filter.contains(':')) {
			return begining + '(' + filter.replaceAll(':', '|') + ')' + end
		}
		// 6) Check if user type an 'AND' expression using '&' character
		// 'Petabyte'.matches(/(?=.*by)(?=.*Peta).*/)
		if (filter.contains('&')) {
			List<String> parts = filter.split('&').collect { "(?=.*$it)" }
			return begining + parts.join() + end
		}
		// 7) check if filter starts with a word character, short for [a-zA-Z_0-9]
		if (regex.matches(/^\w.*/)) {
			regex = '^' + regex
		}
		// 8) Check if filter ends with a word character, short for [a-zA-Z_0-9]
		if (regex.matches(/.*\w$/)) {
			regex = regex + '$'
		}

		return regex
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
			} else if (type == BigDecimal) {
				parsedNumber = NumberUtil.toBigDecimal(filter)
			}  else {
				parsedNumber = filter.toFloat()
			}
		}
		return parsedNumber

	}

	/**
	 * Determine if the field being used for filtering is Enumeration.
	 * @param fsd an instance of {@code FieldSearchData}
	 * @return true if {@code FieldSearchData#domain} is Enum type
	 */
	private static boolean isEnumerationField(FieldSearchData fsd) {
		return fsd.type?.isEnum()
	}
	/**
	 * Determine if the field being used for filtering is numeric.
	 * @param fsd
	 * @return
	 */
	private static boolean isNumericField(FieldSearchData fsd) {
		boolean isNumeric = false

		FieldSpec fieldSpec = fsd.searchInfo?.fieldSpec

		if(fieldSpec?.isCustom()){
			isNumeric = fieldSpec.isNumeric()
		} else {

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

	/**
	 * Handle the filtering of many to many relationships.
	 * @param fieldSearchData
	 * @return
	 */
	private static void handleManyToMany(FieldSearchData fieldSearchData) {
		List<Long> manyToManyMatches = getManyToManyMatches(fieldSearchData)
		if (!manyToManyMatches) {
			manyToManyMatches.add(-1)
		}
		List<Long> values = []
		for (int i = 0; i < manyToManyMatches.size(); i++) {
			String namedParameter = "${fieldSearchData.columnAlias}__" + i
			values << ':' + namedParameter
			fieldSearchData.addSqlSearchParameter(namedParameter, manyToManyMatches[i].toLong())
		}

		fieldSearchData.sqlSearchExpression = fieldSearchData.whereProperty + ' IN '  + ' (' + values.join(', ') + ')'

	}

	/**
	 * Build a list of the ids of the assets that match the given criteria in a many-to-many relationship
	 * context, such as with tags.
	 * @param FieldSearchData
	 * @return
	 */
	private static List<Long> getManyToManyMatches(FieldSearchData fieldSearchData) {
		boolean isAnd = fieldSearchData.filter.contains("&")
		List<Long> matches = []
		String key = isAnd ? 'AND' : 'OR'
		Closure queryBuilder = fieldSearchData.manyToManyQueries[key]
		Map queryInfo = queryBuilder(fieldSearchData.filter)
		if (queryInfo) {
			Class domainClass = fieldSearchData.domain
			// Check the given Class is an actual domain class.
			if (GormUtil.isDomainClass(domainClass)) {
				matches = domainClass.executeQuery(queryInfo.query, queryInfo.params)
			} else {
				throw new RuntimeException("Invalid domain class ${domainClass.name}.")
			}
		}

		return matches
	}

}

