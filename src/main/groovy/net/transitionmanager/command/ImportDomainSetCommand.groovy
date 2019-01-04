package net.transitionmanager.command

import net.transitionmanager.command.CommandObject



class ImportDomainSetCommand implements CommandObject  {

	String domain
	List<String> fields
	List<Map> data
}
