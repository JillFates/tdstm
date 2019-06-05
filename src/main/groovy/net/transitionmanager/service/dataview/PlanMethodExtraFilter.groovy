package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.asset.Application
import net.transitionmanager.project.Project
import org.apache.commons.lang.StringEscapeUtils

@CompileStatic
class PlanMethodExtraFilter extends NamedExtraFilter implements ExtraFilterHqlGenerator {

	@Override
	Map<String, ?> generateHQL(Project project) {

		String customField = project.planMethodology
		// Unescaping the parameter since it can include HTML encoded characters (like \' == &#39; )
		String planMethodology = StringEscapeUtils.unescapeHtml(this.filter)
		if (planMethodology == Application.UNKNOWN) {

			return [
				hqlExpression: " (AE.${customField} is NULL OR AE.${customField} = '') ",
				hqlParams    : [:]
			]

		} else {

			return [
				hqlExpression: " AE.${customField} = :planMethodology ",
				hqlParams    : [
					planMethodology: planMethodology
				]
			]
		}
	}
}
