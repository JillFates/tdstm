package com.tdsops.common.security.spring;

import grails.util.Holders;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Collections;
import java.util.List;

/**
 * The transformation class for the @HasPermission annotation. Each annotated method and all
 * methods when the class is annotated have a call to securityService.requirePermission()
 * added at the top of the method.
 *
 * @author <a href="mailto:burt@agileorbit.com">Burt Beckwith</a>
 */
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
public class HasPermissionASTTransformation extends AbstractASTTransformation {

	public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
		if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode)) {
			return;
		}

		ASTNode node = nodes[1];
		if (!(node instanceof MethodNode) && !(node instanceof ClassNode)) {
			return;
		}

		AnnotationNode annotation = findAnnotation((AnnotatedNode) node);
		if (annotation == null) {
			// not possible but makes the code simpler
			return;
		}
		ListExpression args;
		Expression annotationValueExpression = annotation.getMember("value");
		if (annotationValueExpression instanceof ListExpression) {
			args = (ListExpression) annotationValueExpression;
		}
		else {
			args = new ListExpression(Collections.singletonList(annotationValueExpression));
		}

		if (node instanceof MethodNode) {
			MethodNode methodNode = (MethodNode) node;
			apply(methodNode, sourceUnit, args, false);
		}
		else {
			for (MethodNode methodNode : ((ClassNode) node).getMethods()) {
				apply(methodNode, sourceUnit, args, true);
			}
		}
	}

	private void apply(MethodNode methodNode, SourceUnit sourceUnit, ListExpression args, boolean classScope) {
		// if the class and method are both annotated, ignore the class annotation settingsmes
		if (classScope && methodNode.getAnnotations(new ClassNode(HasPermission.class)).get(0) != null) {
			return;
		}

		Expression getApplicationContext = new StaticMethodCallExpression(
				new ClassNode(Holders.class), "getApplicationContext",
				ArgumentListExpression.EMPTY_ARGUMENTS);
		Expression getBean = new MethodCallExpression(getApplicationContext, "getBean",
				new ConstantExpression("securityService"));
		Expression callService = new MethodCallExpression(getBean, "requirePermission",
				new ArgumentListExpression(args));

		((BlockStatement) methodNode.getCode()).getStatements().add(0, new ExpressionStatement(callService));

		VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit);
		for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
			scopeVisitor.visitClass(classNode);
		}
	}

	private AnnotationNode findAnnotation(AnnotatedNode node) {
		List<AnnotationNode> annotations = node.getAnnotations(new ClassNode(HasPermission.class));
		return annotations.isEmpty() ? null : annotations.get(0);

	}
}
