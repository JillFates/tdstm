package net.transitionmanager.command

import net.transitionmanager.command.CommandObject
import grails.validation.Validateable


@Validateable
class ImportCommand implements CommandObject  {
	ETLInfoCommand ETLInfo
	List<ImportDomainSetCommand> domains
}
