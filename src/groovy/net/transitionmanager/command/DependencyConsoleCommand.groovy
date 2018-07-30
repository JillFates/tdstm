package net.transitionmanager.command

@grails.validation.Validateable
class DependencyConsoleCommand {
	Long       bundle
	List<Long> tagIds   = []
	String     tagMatch = 'ANY'
	String     assinedGroup
	String     subsection
	Long       groupId
	String     assetName

	static constraints = {
		bundle nullable: true
		tagMatch inList: ['ANY', 'ALL']
		assinedGroup nullable: true
		subsection nullable: true
		groupId nullable: true
		assetName nullable: true
	}
}
