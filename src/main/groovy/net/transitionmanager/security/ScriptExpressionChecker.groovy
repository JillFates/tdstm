package net.transitionmanager.security

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

@CompileStatic
class ScriptExpressionChecker implements SecureASTCustomizer.ExpressionChecker {


	private static final List<String> ProhibitedStringMethods = ['execute', 'asType', 'toURI', 'toURL']
	private static final List<String> AllowedObjectMethods    = ['clone', 'equals', 'toString', 'any', 'asBoolean', 'collect', 'contains',
														 'count', 'each', 'eachWithIndex', 'equals', 'every', 'find', 'findIndexOf',
														 'findIndexValues', 'findLastIndexOf', 'findResult', 'flatten', 'getAt', 'grep',
														 'groupBy', 'inject', 'is', 'join', 'putAt', 'size', 'sum', 'with'
	]

	@Override
	boolean isAuthorized(Expression expression) {
		if (expression && expression instanceof MethodCallExpression) {
			MethodCallExpression methodCall = (MethodCallExpression) expression
			ConstantExpression methodExpression = (ConstantExpression) methodCall?.method

			if (methodExpression && methodExpression?.type?.name == String.class.name &&
				methodExpression.value in ProhibitedStringMethods) {
				return false
			}

			if (methodExpression && methodExpression?.type?.name == Object.class.name &&
				!AllowedObjectMethods.contains(methodExpression?.value)) {
				return false
			}

		}

		return true
	}
}
