package com.tdsops.tm.enums.domain

import com.tds.asset.Application
import com.tdssrc.grails.NumberUtil
import groovy.transform.CompileStatic
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent

@CompileStatic
enum ContextType {

	E('Event'),
	B('Bundle'),
	A('Application')

	static ContextType getDefault() { E }

	def getObject(contextId) {
		Long id = NumberUtil.toLong(contextId)
		if (!id) return null

		switch (this) {
			case A: return Application.get(id)
			case B: return MoveBundle.get(id)
			case E: return MoveEvent.get(id)
		}
	}

	final String value

	private ContextType(String label) {
		value = label
	}

	String value() { value }

	static ContextType asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<ContextType> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
