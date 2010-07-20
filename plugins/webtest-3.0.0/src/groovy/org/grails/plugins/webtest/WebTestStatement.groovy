package org.grails.plugins.webtest

import org.junit.runners.model.Statement
import org.junit.runners.model.FrameworkMethod


class WebTestStatement extends Statement {

    public WebTestStatement(Statement statement, FrameworkMethod method, Class testClass) {
        this.statement = statement;
        this.method = method;
        this.testClass = testClass
    }

    Statement statement
    FrameworkMethod method
    Class testClass

    void evaluate() {
        WebTestCase.buildListener.reset()
        def name = testClass.name + "." + method.name
        try {
            WebTestCase.ant.webtest(name: name) {
                statement.evaluate()
            }
        } finally {
            if (!WebTestCase.buildListener.hasPassed()) {
                throw new RuntimeException("WebTest ${name} has failed. See WebTestReport for details.")
            }
        }
    }
}
