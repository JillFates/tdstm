package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
// class ETLDataRecordFieldsCommand implements CommandObject {
class ETLDataRecordFieldsCommand {
	Map<String, ETLDataRecordFieldsPropertyCommand> fields
}
