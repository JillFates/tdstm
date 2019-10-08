package net.transitionmanager.command

class ProjectCommand implements CommandObject {

	Long       clientId
	Integer    collectMetrics
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
		collectMetrics range: 0..1
		planMethodology validator: { val, obj, errors ->
			if (val && ! val ==~ /custom[0-9]{2}/) {
				errors.rejectValue('planMethodology', 'project.plan.methodology.invalid')
			}
		},
		nullable: true
	}

}
