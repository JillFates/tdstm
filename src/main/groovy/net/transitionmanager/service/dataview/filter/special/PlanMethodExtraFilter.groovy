package net.transitionmanager.service.dataview.filter.special

import groovy.transform.CompileStatic
import net.transitionmanager.asset.Application
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.Project
import net.transitionmanager.service.dataview.filter.ExtraFilterType
import org.apache.commons.lang.StringEscapeUtils

/**
 * <p>An instance of {@code EventExtraFilter} is used to create extra filters
 * based on the following json example:</p>
 * <code>
 *     {"property" : "_planMethod", "filter": "Plan I"}
 *     {"property" : "_planMethod", "filter": "Unknown"}
 * </code>
 * <p>It can generate HQL used in {@code DataviewService#previewQuery}.
 * That HQL sentence is based on {@code Project#planMethodology} field
 * and It should be defined first.</p>
 * <p>Filtering for a particular value, HQL generated looks like:</p>
 * <code>
 *     assert project.planMethodology == 'custom5'
 *     " AE.custom5 = 'Plan I' "
 * </code>
 * <p>Filtering for a Unknown value, HQL generated looks like:</p>
 * <code>
 *     assert project.planMethodology == 'custom5'
 *     " (AE.custom5 is NULL OR AE.custom5 = '') "
 * </code>
 */
@CompileStatic
class PlanMethodExtraFilter extends SpecialExtraFilter {

	@Override
	Map<String, ?> generateHQL(Project project) {

		String customField = project.planMethodology
		if (!customField) {
			throw new InvalidParamException('Invalid filter definition for '
				+ ExtraFilterType.PLAN_METHOD.name
				+ '. Project.planMethodology must be defined first.'
			)
		}

		// Unescaping the parameter since it can include HTML encoded characters (like \' == &#39; )
		String planMethodology = StringEscapeUtils.unescapeHtml(this.filter)
		if (planMethodology == Application.UNKNOWN) {

			return [
				hqlExpression: " COALESCE(AE.${customField}, '') = '' ",
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
