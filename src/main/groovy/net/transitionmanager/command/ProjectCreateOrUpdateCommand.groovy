package net.transitionmanager.command

class ProjectCreateOrUpdateCommand implements CommandObject {

	Long       clientId
	Boolean    collectMetrics
	String     comment
	Date       completionDate
	Object     defaultBundle
	String     defaultBundleName
	String     description
	Long       id
	List<Long> partnerIds
	String     planMethodology
	String     projectCode
	String     projectLogo
	String     originalFilename
	Long       projectManagerId
	String     projectName
	String     projectType
	Integer    runbookOn
	Date       startDate
	String     timeZone

	static constraints = {
		id nullable: true
		runbookOn nullable: true
		defaultBundle nullable: true
		projectLogo nullable: true
		originalFilename nullable: true
		planMethodology validator: { val, obj, errors ->
			//The regex is looking for values that start with custom followed by a number 0 through 99
			if (val && ! val ==~ /custom[0-9]{2}/){
				errors.rejectValue('planMethodology', 'project.plan.methodology.invalid')
			}
		},
		nullable: true
	}

}
