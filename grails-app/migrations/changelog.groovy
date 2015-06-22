databaseChangeLog = {

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
	include file: '20140709-add-defualt-bundle-to-project.groovy'
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
	include file: '20140704-pre-seed-asset-tag-sequence-for-existing-clients.groovy'
	include file: '20140717-task-batch-add-recipe-id-field.groovy'
	include file: '20140804-fix-asset-comment-task-number.groovy'
	include file: '20140805-change-asset-import-export-fields.groovy'
	//include file: '20140812-remove-asset-entity-old-fields.groovy'
	include file: '20140813-add-default-asset-entity-field-to-recipe.groovy'
	include file: '20140814-clean-up-orphaned-commentNotes.groovy'
	include file: '20140819-change-assetentity_assetname-notnull.groovy'
	include file: '20140903-drop-permission-ViewSupervisorConsoles.groovy'
	include file: '20140903-drop-colums-AssetEntity-source-target-rooms.groovy'
	include file: '20140908-add-bulk-delete-permissions.groovy'
	include file: '20140909-update-model-types.groovy'
	include file: '20140922-sequence-logic.groovy'
	include file: '20140929-changeBladeChassisRefToUseIds.groovy'
//	include file: '20141006-RenameGenericManuAndModel.groovy' // This is currently commented out until I can finish it (JPM 10/2014)
	include file: '20141008-add-edit-project-field-settings-permissions.groovy'
	include file: '20141107-clean-up-preference-asset-columns.groovy'
	include file: '20141107-remove-current-status-from-eav-attribute.groovy'
	include file: '20141110-comment-notes-change-column-note-to-text.groovy'
	include file: '20141110-nullout-invalid-person-ref.groovy'
	include file: '20141110-new-custom-columns-from-65-to-96.groovy'
	include file: '20141111-set-default-model-on-racks.groovy'
	include file: '20141112-clean-up-preference-columns.groovy'
	include file: '20141113-addSomeColumnsTo-DataTransferBatch.groovy'
	include file: '20141114-addIndexTo_DataTransferValue_table.groovy'
	include file: '20141204-delete-invalid-references-from-party-relationship.groovy'
	include file: '20150114-add-model-add-change-pending-status-permission.groovy'
	include file: '20150170-data-transfer-batch-import-results-change.groovy'
	include file: '20150203-add-architecture-view-perm.groovy'
	include file: '20150319-remove-invalid-security-entries-from-party-role.groovy'
	include file: '20150407-cleanup-cabling-data.groovy'
	include file: '20150408-set-all-PartyRoles-toUpperCase.groovy'
	include file: '20150408-modify-fields-environment-validation-criticaly.groovy'
	include file: '20150409-new-user-login-permissions.groovy'
	include file: '20150410-remove-user-login-permissions-from-client-admin-role.groovy'
	include file: '20150417-remove-from-model-sync-batch-user-login-fields.groovy'
	include file: '20150417-alter-foreign-key-on-model-sync.groovy'
	include file: '20150420-update-client-admin-and-client-mgr-permissions.groovy'
	include file: '20150428-new-person-and-project-staff-permissions.groovy'
	include file: '20150512-add-some-privileges.groovy'
	include file: '20150430-remove-asset-transition.groovy'
	include file: '20150519-rename-in-progress-to-news-bar-mode.groovy'
	include file: '20150526-assettype-defaulted-to-server-null-values.groovy'
	include file: '20150526-delete-orphaned-dependency-bundles.groovy'	
	include file: '20150520-create-column-send-notification-in-asset-comment.groovy'
	include file: '20150529-drop-party-column-from-project-logo.groovy'
	include file: '20150602-add-column-instructions-link-to-asset-comment.groovy'
	include file: '20150616-timezones.groovy'
}
