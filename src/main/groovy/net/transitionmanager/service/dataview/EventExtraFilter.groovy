package net.transitionmanager.service.dataview

import com.tdssrc.grails.NumberUtil
import groovy.transform.CompileStatic
import net.transitionmanager.project.Project

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
