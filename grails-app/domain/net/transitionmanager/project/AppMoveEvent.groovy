package net.transitionmanager.project

import net.transitionmanager.asset.Application

class AppMoveEvent {

	Application application
	MoveEvent moveEvent
	String value

	static constraints = {
		application nullable: true
		moveEvent nullable: true, fetch: 'join'
		value nullable: true
	}
}
