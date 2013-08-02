package com.tdsops.common.sql

import com.tdssrc.grails.StringUtil
import org.apache.commons.lang.StringUtils

/** 
 * This class provides a number of utility functions
 */
class SqlUtil {
	
	/**
	 * Helper class that allows the concatenantion of sql with and/or which only adds the logic if the query already contains content
	 * @param String existing query
	 * @param String additional query
	 * @param String logical and/or (default 'and')
	 * @return String concatenated string
	 */
	static String appendToWhere(query, additional, andOr='and') {
		return query + (query.size() > 0 ? " $andOr " : '') + additional
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
	 * @param String - property name (e.g. 'a.lastName)
	 * @param String/Array - criteria (e.g. '5', '>=5', 'a%', ['a','b'])
	 * @param String - paramName that will be used for parameterized variables in QUERY
	 * @param Boolean - isNot flag that will add NOT to the LIKE and IN expressions
	 * @return Map[sql:param] where the SQL is the parameterized SQL and the param contains the value used as the parameterize value. The BETWEEN 
	 * 		expression does not return a parameter as there needs to be two params and not presently supported.
	 *
	 */
	static Map whereExpression(property, criteria, paramName, isNot=false) {
		def map = [:]
		def not=isNot ? ' NOT ' : ' '
		if (criteria instanceof java.lang.String || criteria instanceof org.codehaus.groovy.runtime.GStringImpl) {
			if (criteria.contains('%')) {
				// LIKE expression
				map = [sql:"${property}${not}LIKE :${paramName}", param:criteria]
			} else if (criteria.toLowerCase() ==~ /^between / ) {
				// BETWEEN expression
				map = [sql:"${property}${not}${criteria}", param:null] 
			} else if ('!<>='.contains(criteria.substring(0,1))) {
				// BOOLEAN expression
				def param = StringUtil.stripOffPrefixChars('!<>=', criteria)
				def expr = criteria.substring(0, (criteria.size() - param.size()) )
				map = [sql:"$property $expr :$paramName", param:param]
			} else {
				// default EQUALS condition
				println "in string - default EQUALS"				
				map = [sql:"$property = :$paramName", param:criteria]
			}
		} else if (criteria instanceof 	java.lang.Integer) {
			def expr=isNot ? '<>' : '='
			map = [sql:"$property $expr :$paramName", param:criteria]
		} else if (criteria instanceof java.util.ArrayList) {
			println "whereExpression-Array"
			map = [sql:"${property}${not}IN (:$paramName)", param:criteria]		
		} else {
			// log.error "whereExpression received criteria of unsupported class type (${criteria.class}) for property $property"
			map = null
		}
		return map
	}
	
	/**
	 * Used to parse user input from filters so that we can create the appropriate SQL expression that will support boolean expressions
	 * like <, <=, >, >= or - to cause a NOT filter
	 * @param String text - the filter value
	 * @param String defExpr - the default expression to use if the filter doesn't include a filter
	 * @return List [text, expression] - the text with the expression stripped off and the appropriate expression
	 */
	static List parseExpression( text, defExpr='=') {
		def expr = defExpr
		def not = false

		text = text.trim()

		// Get the NOT (-) switch
		if (text[0] == '-') {
			not = true
			text = StringUtils.substring(text, 1).trim()
		}

		if (['<=','>='].contains( StringUtils.substring(text, 0, 2) ) ) {
			expr = StringUtils.substring(text, 0, 2)
			text = StringUtils.substring(text,2).trim()
		} else if ('<>='.contains( StringUtils.substring(text, 0, 1) ) ) {
			expr = text[0]
			text = StringUtils.substring(text, 1).trim()
		}

		// Handle placing NOT on the expression
		if (not) {
			//println "parseExpression() in NOT handler, expr=$expr xxx"
			if (expr == '=')
				expr = '<>'
			else if (expr.toLowerCase() == 'like' )
				expr = 'NOT ' + expr
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
}
