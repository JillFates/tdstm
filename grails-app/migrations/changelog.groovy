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
	include file: '20130627-add-person-middlename-column.groovy'
	include file: '20130611-add-person-ref-columns.groovy' 
	include file: '20130612-migrate-person-ref-records.groovy' 
    //include file: '20130522-create-default-project.groovy' // TODO : enable after John's review
	include file: '20130624-update-recent-users-expiry-dates.groovy'
	include file: '20130626-update-person-staff_type.groovy'
	include file: '20130709-add-new-fields-application-table.groovy'
	include file: '20130710-add-new-app-field-import-export.groovy'
	include file: '20130713-add-permission-view-permission.groovy'
	include file: '20130712-add-virtual_host-import-export.groovy'
	include file: '20130712-change-asset-new-or-old-field-to-plan-status.groovy'
	include file: '20130718-add-asset-edit-delete-permission.groovy'
	include file: '20130723-add-keyvalue-table.groovy'
	include file: '20130808-update-model-status.groovy'
	include file: '20130826-update-comment-datatype-to-text.groovy'
	include file: '20130903-add-task-report-permission.groovy'
	include file: '20130903-add-index-to-ModelAlias.groovy'
	include file: '20130905-update-asset-orphan-records-with-no-racks-rooms.groovy'
	include file: '20130904-update-blades-room-and-location.groovy'
	include file: '20130910-update-person-fields.groovy'
	include file: '20130912-add-columns-for-runbook-optimization.groovy'
	include file: '20130918-add-last-modified-column-userlogin.groovy'
	include file: '20130924-add-column-dep_console_grouping_criteria-project.groovy'
	include file: '20130924-add-table-test-domain.groovy'
    include file: '20131004-size-scale-rateOfChange-properties-into-AssetEntity.groovy'
	include file: '20131014-delete-orphan-records-room-rack.groovy'
	include file: '20131018-fix-party-version-tempForUpdate.groovy'
	include file: '20131211-asset-cable-map-rebuild.groovy'
*/
	include file: '20140128-add-cookbook.groovy'
	include file: '20140130-remove-createdBy-recipe.groovy'
	include file: '20140131-add-missing-version.groovy'
	include file: '20140203-add-missing-autoincrement.groovy'
	include file: '20140205-add-new-permissions.groovy'
	include file: '20140211-add-25-to-48-custom-fields.groovy'
	include file: '20140219-add-default-values-is-published.groovy'
	include file: '20140227-add-time-move-event.groovy'
	include file: '20140305-add-custom-fields-to-dependencies.groovy'
	include file: '20140306-add-is-local-column-to-user-login-table.groovy'
	include file: '20140307-sequence-logic.groovy'
	include file: '20140312-update-person-active-field-with-user-login-active.groovy'
	include file: '20140313-task-batch-context-type.groovy'
	include file: '20140318-add-guid-to-userlogin.groovy'
	include file: '20140320-change-use-of-planning-field-to-use-for-planning.groovy'
	include file: '20140320-update-date-created-when-null.groovy'
	include file: '20140324-add-modified-by-column-in-asset-entity.groovy'
	include file: '20140327-modify-owner-frontend-label-to-appOwner.groovy'
	include file: '20140403-add-version-column-in-eav-entity-table.groovy'
	include file: '20140417-add-task-batch-project.groovy'
	include file: '20140417-insert-environment-options-in-asset-options-table.groovy'
	include file: '20140428-add-description-field-to-eav-attribute.groovy'
	include file: '20150205-add-49-64-custom-fields.groovy'
	include file: '20140506-add-missing-autoincrement.groovy'
	include file: '20140507-move-recipes-from-moveevent-to-recipe-tables.groovy'
	include file: '20140509-pre-seed-tasknumber-sequence-for-existing-clients.groovy'
	include file: '20140519-change-task-batch-log-fields.groovy'
	include file: '20140521-add-help-column-in-role-type-table.groovy'
	include file: '20140523-add-model-edit-permission.groovy'
	include file: '20140529-add-viewTaskTimeline-perms.groovy'
	include file: '20140530-remove-unused-keyvalues-rows.groovy'
	include file: '20140603-add-corporateName-corporateLocation-and-website-columns-to-manufacturer-table.groovy'
	include file: '20140611-assetclass-property-to-assets.groovy'
	include file: '20140618-fix-timescale.groovy'
	include file: '20140625-add-update-supervisor-consoles-permission.groovy'
	include file: '20140627-remove-editor-role-from-cookbook-permission.groovy'
	include file: '20140701-delete-asset-dependency-orphaned-record.groovy'
	include file: '20140205-add-viewtaskgraph-permissions.groovy'
}
