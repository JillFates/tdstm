package net.transitionmanager.task.taskgraph

import groovy.transform.CompileStatic

@CompileStatic
class TaskSearchQueryBuilder {

    StringBuffer query
    Map<String, Object> queryParams

    TaskSearchQueryBuilder() {
        query = new StringBuffer()
    }

    TaskSearchQueryBuilder add(String op, String sentence, String suffix = '') {
        return addSentence(op, sentence, suffix)
    }

    TaskSearchQueryBuilder select(String sentence) {
        return addSentence('SELECT', sentence)
    }

    TaskSearchQueryBuilder from(String sentence) {
        return addSentence('FROM', sentence)
    }

    TaskSearchQueryBuilder where(String sentence) {
        return addSentence('WHERE', sentence)
    }

    TaskSearchQueryBuilder innerJoin(String sentence, String suffix) {
        return addSentence('INNER JOIN', sentence, suffix)
    }

    TaskSearchQueryBuilder leftOuterJoin(String sentence) {
        return addSentence('LEFT OUTER JOIN', sentence)
    }

    TaskSearchQueryBuilder and(String sentence) {
        return addSentence('AND', sentence)
    }

    private TaskSearchQueryBuilder addSentence(String sentence, String term, String suffix = '') {
        if (term) {
            query.append("$sentence $term")
            if (suffix) query.append(" $suffix")
            query.append(System.lineSeparator())
        }
        return this
    }

    String build() {
        return query.toString().stripIndent().trim()
    }
}