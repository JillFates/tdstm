package org.grails.plugins.webtest

import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class WebTestRule implements MethodRule {
        
    Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        return new WebTestStatement(statement, frameworkMethod, o.class)
    }
}