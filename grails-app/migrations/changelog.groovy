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
	include file: '20150526-add-import-export-staff-permissions.groovy'
	include file: '20150520-create-column-send-notification-in-asset-comment.groovy'
	include file: '20150529-drop-party-column-from-project-logo.groovy'
	include file: '20150602-add-column-instructions-link-to-asset-comment.groovy'
	//include file: '20150610-task-role-defaulted-to-blank.groovy'
	include file: '20150710-poject-activity-metrics-table.groovy'
	include file: '20150721-remove-old-refresh-preferences.groovy'
	include file: '20150803-add-roletype-type-and-level-columns.groovy'
	include file: '20150731-add-additional-teams-to-role-type.groovy'
	include file: '20150803-new-user-login-properties.groovy'
	include file: '20150807-email-dispatch.groovy'
	include file: '20150806-password-reset.groovy'
	include file: '20150810-add-new-user-login-permissions.groovy'
	include file: '20150830-add-salt-prefix-to-user-login.groovy'
	include file: '20150828-add-SendUserActivations-permission.groovy'
	include file: '20150930-add-new-eav-attribute-for-lun.groovy'
	include file: '20151020-add-new-virtualization-teams.groovy'
	include file: '20151019-move-event-staff-fix.groovy'
	include file: '20151102-move-event-staff-rename.groovy'
	include file: '20151109-change-some-asset-datetimes-to-date.groovy'
	include file: '20151109-remove-invalid-party-relationship-references-to-party-table.groovy'
	include file: '20151116-dependency-analyzer-permissions.groovy'
	include file: '2015113-add-project-staff-partyrelationships.groovy'
	include file: '2015117-set-old-persons-and-users-inactive.groovy'
	include file: '20150616-timezones.groovy'
	include file: '20150626-add-column-timezone-to-project.groovy'
	include file: '20150827-set-timezone-and-datetime-user-preferences.groovy'
	// TM-8152 - This property was removed
	// include file: '20151020-set-custom-fields-to-hidden.groovy'  //  TM-6622 - has been removed because FieldImportance table and domain class no longer exists.
	include file: '20151214-fix-invalid-status-and-type-values-in-assetdep.groovy'
	include file: '20151214-remove-new-or-old-column-from-asset-entity.groovy'
	include file: '20151209-nullout-invalid-person-ref.groovy'
	include file: '20151230-remove-from-role-user-dep-analyzer-view.groovy'
	include file: '20160210-new-security-permission-for-critical-path.groovy'
	include file: '20160310-add-permission-monitoring-permission.groovy'
	include file: '20160315-fix-orphan-records-asset_comment.groovy'
	include file: '20160321-add-permission-restartapplication-admin.groovy'
	include file: '20160325-recreate-orphaned-staff-team-references.groovy'
	include file: '20160415-add-perms-for-accountImportExprt.groovy'
	include file: '20160425-change-rate_of_change-in-asset_entity-precision.groovy'
	include file: '20160428-remove-orphan-staffing-party-references.groovy'
	include file: '20160601-drop-nonnull-on-UserLogin-passwordChangedDate.groovy'
	include file: '20160607-delete-orphan-parties.groovy'
	include file: '20160711-remove-ContactMech-domain.groovy'
	include file: '20160712-add-additional-Application-criticality-options.groovy'
	include file: '20160721-delete-PRINTER_NAME-preference.groovy'
	include file: '20160725-change-engine-to-InnoDB-for-MyISAM-tables.groovy'

	// Had to reshuffle the creating of these tables that are now part of the person delete / merge logic
	// which the 20160727-delete-orphaned-persons.groovy script references and started breaking when the
	// PersonService.deletePerson was updated to address these tables as well.
	// See ticket TM-8152
	include file: '20170814-create-dataview-table.groovy'
	include file: '20170925-create-favorite-dataview-table.groovy'
	include file: '20160920-create-license-table.groovy'
	include file: '20160920-create-licensed_client-table.groovy'
	include file: '20170220-create-license-activity-track-table.groovy'
	// The Delete Orphans using the PersonService.deletePerson can be temperamental with changes to the service
	// like adding new references.
 	include file: '20160727-delete-orphaned-persons.groovy'
	// Back to our regularly scheduled program

	include file: '20160727-drop-notnull-for-created-by.groovy'
 	include file: '20160805-update-person-middle-and-last-name-default-value.groovy'
	include file: '20160815-add-permission-company-crud-admin.groovy'
	include file: '20160817-delete-source-target-team-eav-attributes.groovy'
	include file: '20160909-add-duration-locked-to-tasks.groovy'
	include file: '20161006-update-user-preference-code-legendTwistieState.groovy'
	include file: '20161010-notice-support.groovy'
	include file: '20161010-notice-support-change-noticetype-column.groovy'
	include file: '20161111-delete-asset-dependency-orphans.groovy'
	include file: '20161129-fix-license-columns.groovy'
	include file: '20161207-add-owner-to-license-table.groovy'
	include file: '20161208-add-host-data-to-license-table.groovy'
	include file: '20161208-add-host-data-to-licenseclient-table.groovy'
	include file: '20161220-add-banner_message-to-licensedclient-table.groovy'
	include file: '20170110-add-banner_message-to-license-table.groovy'
	include file: '20170110-add-license_compliant-and-seal-to-projectdailymetric-table.groovy'
	include file: '20170110-apply-seal-to-projectdailymetric-table.groovy'
	include file: '20170118-add-grace_period_days-to-licensedclient-table.groovy'
	include file: '20170125-drop_not_null_dbFormat_constraint_on_database_table.groovy'
	include file: '20170125-drop_not_null_fileFormat_constraint_on_files_table.groovy'
	include file: '20170203-add-table_columns-for-api-support.groovy'
	include file: '20170209-delete-model-manufacturer-aliases-matching-their-parents-name.groovy'
	include file: '20170224-add-autotimestamp-license-and-licensedclient.groovy'
	include file: '20170224-change-license-enum-columns.groovy'
	include file: '20170224-change-licensedclient-enum-columns.groovy'
	include file: '20170224-delete-current-license-activities.groovy'
	include file: '20170227-update-autotimestamps-license-licensed-client.groovy'
	include file: '20170228-remove-version-in-license-licensed-client.groovy'
	include file: '20170228-fix-type-method-enum-licensed-client.groovy'
	include file: '20170307-change-license-installation-column-spelling.groovy'
	include file: '20170308-nullout-orphaned-asset-comments-by-workflow-transition.groovy'
	include file: '20170403-remove-racks-and-rooms-for-VMs.groovy'
	include file: '20170227-delete-create-or-rename-permissions-for-new-naming-scheme.groovy'
	include file: '20170413-add-new-permission-to-clone-assets-feature.groovy'
	include file: '20170417-remove-orphan-move-event-asset-comments-user-preferences-references.groovy'
	include file: '20170504-delete-room-merge-permission.groovy'
	include file: '20170530-create-setting-table.groovy'
	include file: '20170623-add-planMethodology-to-Ptoject.groovy'
	include file: '20170704-delete-model-alias-records-that-associate-model-of-same-name.groovy'
	include file: '20170712-create-new-fieldsettings-specs.groovy'
	include file: '20170714-remove-all-user-preference-for-the-various-asset-list-columns.groovy'
	include file: '20170712-add-fieldName-to-DataTransferValue.groovy'
	include file: '20170714-Clear-out-orphaned-data-references-that-cause-the-Task-Report-to-fail.groovy'
	include file: '20170725-remove-EavAttributeSet-reference-in-AssetEntity.groovy'
	include file: '20170731-null-out-move-event-orphans-for-tasks.groovy'
	include file: '20170816-Clear-out-possible-corrupted-Tasks-referencing-not-existing-Assets.groovy'
	include file: '20170817-add-ProjectManageDefaults-permission.groovy'
	include file: '20170830-Assign-a-company-as-the-owner-for-the-default-project.groovy'
	include file: '20170807-add-new-permission-to-asset-explorer-feature.groovy'
	include file: '20170830-remove-staff-prefix-for-team-descriptions-v2.groovy'
	include file: '20171002-create-provider-and-datascript-tables.groovy'
	include file: '20171010-remove-legacy-field-settings.groovy'
	include file: '20171011-create-credential-table.groovy'
	include file: '20170816-add-api-action-permissions.groovy'
	include file: '20171020-add-properties-for-api-etl.groovy'
	include file: '20171025-create-default-bundle-for-projects.groovy'
	include file: '20171026-add-assetclass-to-common-fields.groovy'
	include file: '20171030-add-permissions-for-datascript.groovy'
	include file: '20171102-add-permissions-for-provider.groovy'
	include file: '20171108-all-asset-system-view.groovy'
	include file: '20171110-create-new-permission-asset-explorer-system-list.groovy'
	include file: '20171205-add-action-reset-permission.groovy'
	include file: '20171221-change-asset-dependency-comment-type.groovy'
	include file: '20171227-rename-all-assets-view.groovy'
}
