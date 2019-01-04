package net.transitionmanager.command

import net.transitionmanager.command.CommandObject




class ImportCommand implements CommandObject  {
	ETLInfoCommand ETLInfo
	List<ImportDomainSetCommand> domains
}
