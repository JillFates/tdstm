package net.transitionmanager.command.task

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartFile

@Validateable
class ActionCommand implements CommandObject {

	enum State {
		started,
		progress,
		success,
		error
	}

	State               state
	String              message
	Integer             progress
	String              stdout
	String              stderr
	JSONObject          data
	List<MultipartFile> datafile

	static constraints = {
		state inList: State.values().toList()
		message nullable: true
		progress range: 0..100, nullable: true
		stdout nullable: true
		stderr nullable: true
		data nullable: true
		datafile nullable: true
	}
}
