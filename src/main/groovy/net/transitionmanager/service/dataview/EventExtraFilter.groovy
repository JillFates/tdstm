package net.transitionmanager.service.dataview

import com.tdssrc.grails.NumberUtil
import groovy.transform.CompileStatic
import net.transitionmanager.project.Project

/**
 * <p>An instance of {@code EventExtraFilter} is used to create extra filters
 * based on the following json example:</p>
 * <code>
 *     {"property" : "_event", "filter": "364"}
 * </code>
 * <p>It can generate HQL used in {@code DataviewService#previewQuery}</p>
 * <code>
 *     " AE.moveBundle.moveEvent.id = 364 "
 * </code>
 */
@CompileStatic
class EventExtraFilter extends NamedExtraFilter implements ExtraFilterHqlGenerator {

	@Override
	Map<String, ?> generateHQL(Project project) {
		return [
			hqlExpression: " AE.moveBundle.moveEvent.id = :extraFilterMoveEventId ",
			hqlParams    : [
				extraFilterMoveEventId: NumberUtil.toPositiveLong(this.filter, 0)
			]
		]
	}
}
