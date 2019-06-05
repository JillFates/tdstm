package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.project.Project

/**
 * Defines a common Interface to generate
 */
@CompileStatic
interface ExtraFilterHqlGenerator {

	Map<String, ?> generateHQL(Project project)
}
