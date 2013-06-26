databaseChangeLog = {

/*
 *	Going to start commenting out migrations that are greater than 2 months old since we don't try to migrate anything older 
 *	and it unnecessarily slows down the startup process.
*/

/*	
	include file: '20121206-fix-staff-system-roles.groovy'
	include file: '20121210-fix-models-manu-nulldates.groovy'
	include file: '20121211-add-projectStaff-permission.groovy'
	include file: '20121218-normalize-aka-columns.groovy'	
	include file: '20121214-drop-aka.groovy'
	include file: '20121218-update-version-on-manu-model.groovy'
	include file: '20121218-add-indexes-model-tables.groovy'
	include file: '20121224-cleanup-excel-templates.groovy'
	include file: '20130109-alter-validation-column.groovy'
	include file: '20130111-add-roletypes.groovy'
	include file: '20130114-fix-roletypes.groovy'
	include file: '20130125-add-entity-validation.groovy'
	include file: '20130125-update-plantatus-validation.groovy'
	include file: '20130213-add-model-usize.groovy'
*/

	include file: '20130315-change-userlogin-username-size.groovy'
	include file: '20130321-add-custom-fields.groovy'
	include file: '20130322-add-entity-customs.groovy'
	include file: '20130322-update-custom-field-shown.groovy'
	include file: '20130409-add-import-permission.groovy'
	include file: '20130423-add-import-export-permission.groovy'
	include file: '20130502-add-assetType-eav-attribute-options.groovy'
	include file: '20130503-delete-orphan-records-model.groovy'
	include file: '20130503-drop-application-owner-id.groovy'
	include file: '20130509-update-asset-type-case.groovy'
	include file: '20130510-add-automatic-service-person.groovy'
	include file: '20130520-add-field-importance-table.groovy'
	include file: '20130521-add-missing-eav-attribute.groovy'
	include file: '20130530-add-person-permission.groovy'
	include file: '20130614-add-userlogin-passwordchange-columns.groovy'
	include file: '20130611-add-person-ref-columns.groovy' 
	include file: '20130612-migrate-person-ref-records.groovy' 
    //include file: '20130522-create-default-project.groovy' // TODO : enable after John's review
	include file: '20130624-update-recent-users-expiry-dates.groovy'
	include file: '20130626-update-person-staff_type.groovy'
    
}
