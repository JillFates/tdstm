package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class ETLDataRecordFieldsCommand implements CommandObject {
	Map<String, ETLDataRecordFieldsPropertyCommand> fields
}
