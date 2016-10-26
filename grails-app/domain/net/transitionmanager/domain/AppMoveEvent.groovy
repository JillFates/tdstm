package net.transitionmanager.domain

import com.tds.asset.Application

class AppMoveEvent {

	Application application
	MoveEvent moveEvent
	String value

	static constraints = {
		application nullable: true
		moveEvent nullable: true
		value nullable: true
	}
}
