package net.transitionmanager.command

import com.tdssrc.grails.TimeUtil
import grails.databinding.BindUsing
import net.transitionmanager.project.MoveEvent


class MoveBundleCommand implements CommandObject {

 	Long id
	String name
	String description
	@BindUsing({ obj, source ->
		return TimeUtil.parseISO8601DateTime(source['startTime'])
	})
	Date startTime                   // Time that the MoveBundle Tasks will begin
	@BindUsing({ obj, source ->
		return TimeUtil.parseISO8601DateTime(source['completionTime'])
	})
	Date completionTime              // Planned Completion Time of the MoveBundle
	Integer operationalOrder = 1     // Order that the bundles are performed in (NOT BEING USED)
	MoveEvent moveEvent
	Boolean useForPlanning = true
	Long sourceRoomId
	Long targetRoomId
	Boolean tasksCreated = false

	static constraints = {
		id nullable: true
		description nullable: true
		completionTime nullable: true, blank: true
		description nullable: true
		moveEvent nullable: true
		name blank: false, unique: ['project']
		operationalOrder range: 1..25
		sourceRoomId nullable: true
		startTime nullable: true
		targetRoomId nullable: true
		tasksCreated nullable: true
	}

}


