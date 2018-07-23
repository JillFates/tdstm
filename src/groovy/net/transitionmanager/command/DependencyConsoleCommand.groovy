package net.transitionmanager.command

@grails.validation.Validateable
class DependencyConsoleCommand {
	Long       bundle
	List<Long> tagIds
	String     tagMatch = 'ANY'
	String     assinedGroup
	String     subsection
	Long       groupId
	String     assetName
}
