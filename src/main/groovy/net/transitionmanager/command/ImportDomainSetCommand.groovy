package net.transitionmanager.command

import net.transitionmanager.command.CommandObject
import grails.validation.Validateable

@Validateable
class ImportDomainSetCommand implements CommandObject  {

	String domain
	List<String> fields
	List<Map> data
}
