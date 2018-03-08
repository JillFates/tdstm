package net.transitionmanager.credential

import net.transitionmanager.service.InvalidSyntaxException
import spock.lang.Specification

class CredentialValidationExpressionUnitSpec extends Specification {

    def '1. Test body command'() {
		given:
			InvalidSyntaxException e
			CredentialValidationExpression result

		when: 'called with a valid expression that contains value'
			result = new CredentialValidationExpression('body content contains "Welcome"')
		then:
			ExpressionAttributeEnum.BODY == result.attribute
			ExpressionEvaluationEnum.CONTAINS == result.evaluation
			null == result.headerName
			'Welcome' == result.value

		when: 'called with a valid expression that is missing value'
			result = new CredentialValidationExpression('body content missing "Password:"')
		then:
			ExpressionAttributeEnum.BODY == result.attribute
			ExpressionEvaluationEnum.MISSING == result.evaluation
			null == result.headerName
			'Password:' == result.value

		when: 'called with "content" missing'
			new CredentialValidationExpression('body contains "Password:"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_BODY_EXPRESSION_MSG == e.message

		when: 'called with invalid evaluation type'
			new CredentialValidationExpression('body content unknownEvaluation "Password:"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called with extraneous text'
			new CredentialValidationExpression('body content contains "Password:" and other things')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called without an expression'
			new CredentialValidationExpression('body content')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_BODY_EXPRESSION_MSG == e.message

	}

    def '2. Test header command'() {
		given:
			InvalidSyntaxException e
			CredentialValidationExpression result

		when: 'called with a valid expression that contains value'
			result = new CredentialValidationExpression('header Location contains "/welcome"')
		then:
			ExpressionAttributeEnum.HEADER == result.attribute
			ExpressionEvaluationEnum.CONTAINS == result.evaluation
			'Location' == result.headerName
			'/welcome' == result.value

		when: 'called with a valid expression that is missing value'
			result = new CredentialValidationExpression('header Location missing "/login"')
		then:
			ExpressionAttributeEnum.HEADER == result.attribute
			ExpressionEvaluationEnum.MISSING == result.evaluation
			'Location' == result.headerName
			'/login' == result.value

		when: 'called with missing elements'
			new CredentialValidationExpression('header')
		then:
			e = thrown()
			CredentialValidationExpression.UNRECOGNIZED_EXPRESSION_MSG == e.message

		when: 'called with empty header name'
			new CredentialValidationExpression('header "" contains "xyzzy"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_HEADER_EXPRESSION_MSG == e.message

		when: 'called with invalid evaluation type'
			new CredentialValidationExpression('header Location unknownEvaluation "fubar"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called with extraneous text'
			new CredentialValidationExpression('header Location missing "fubar" and other things')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called without an expression'
			new CredentialValidationExpression('header Location')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_HEADER_EXPRESSION_MSG == e.message
	}

    def '3. Test status command'() {
		given:
			InvalidSyntaxException e
			CredentialValidationExpression result

		when: 'called with a valid expression that contains value'
			result = new CredentialValidationExpression('status code contains "200"')
		then:
			ExpressionAttributeEnum.STATUS == result.attribute
			ExpressionEvaluationEnum.CONTAINS == result.evaluation
			null == result.headerName
			'200' == result.value

		when: 'called with a valid expression that is missing value'
			result = new CredentialValidationExpression('status code missing "200"')
		then:
			ExpressionAttributeEnum.STATUS == result.attribute
			ExpressionEvaluationEnum.MISSING == result.evaluation
			null == result.headerName
			'200' == result.value

		when: 'called with "code" missing'
			new CredentialValidationExpression('status CODE missing "200"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_STATUS_EXPRESSION_MSG == e.message

		when: 'called with invalid evaluation type'
			new CredentialValidationExpression('status code unknownExpression "200"')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called with extraneous text'
			new CredentialValidationExpression('status code missing "200" plus some more crap')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_EXPRESSION_MSG == e.message

		when: 'called without an expression'
			new CredentialValidationExpression('status code')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_STATUS_EXPRESSION_MSG == e.message

	}

    def '4. Test invalid expressions'() {
		given:
			InvalidSyntaxException e

		when: 'called with nothing'
			new CredentialValidationExpression('')
		then:
			e = thrown()
			CredentialValidationExpression.EMPTY_EXPRESSION_MSG == e.message

		when: 'called with NULL'
			new CredentialValidationExpression(null)
		then:
			e = thrown()
			CredentialValidationExpression.EMPTY_EXPRESSION_MSG == e.message

		when: 'called with body()'
			new CredentialValidationExpression('body()')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_BODY_EXPRESSION_MSG == e.message

		when: 'called with header()'
			new CredentialValidationExpression('header()')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_HEADER_EXPRESSION_MSG == e.message

		when: 'called with status()'
			new CredentialValidationExpression('status()')
		then:
			e = thrown()
			CredentialValidationExpression.INVALID_STATUS_EXPRESSION_MSG == e.message

	}

	def '5. Test the evaluate method'() {
		// TODO : Implement this test case
	}
}