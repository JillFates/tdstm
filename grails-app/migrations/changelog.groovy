databaseChangeLog = {
	include file: '20140307-sequence-logic.groovy' // Run on every migration to assure the tdstm_sequencer stored proc exists
	include file: 'version/v4_7_2/20191219-delete-orphaned-dataviews-and-add-constraints.groovy' // Fix dataview constraints before running 20190930-fix-dataviews.groovy
	include file: 'version/v4_7_1/20190930-fix-dataviews.groovy' //TM-15976 added to the beginning to fix the ability to upgrade from 4.5, because of a bug in a previous migration
	include file: '20180508-add-asset-class-to-transferbatch.groovy'
	include file: '20180508-migrate-and-remove-eav.groovy'
	include file: '20180612-datascript-create-unique-index-on-name-project-and-provider.groovy'
	include file: '20180612-add-field-label-map-property-to-import-batch.groovy'
	include file: '20180618-create-user-login-project-access-table.groovy'
	include file: '20180601-create-tag-and-tagLink.groovy'
	include file: '20180620-rename-etl-permissions.groovy'
	include file: '20180613-add-http-method-api-action-table.groovy'
	include file: '20180625-update-importance-values-for-c-and-i.groovy'
	include file: '20180702-create-default-tags-for-default-project-and-existing-projects.groovy'
	include file: '20180702-update-recipe-context.groovy'
	include file: '20180628-update-supportType-label-for-DEVICE-keys-in-Setting.groovy'
	include file: '20180627-add-tag-to-common-fields.groovy'
	include file: '20180629-add-sampleFilename-and-originalSampleFilename-to-Datascript-table.groovy'
	include file: '20180719-asset-comment-table-drop-is-resolved-column.groovy'
	include file: '20180702-create-api-catalog-table.groovy'
	include file: '20180627-change-tag-field-to-tagAssets-for-common-fields.groovy'
	include file: '20180713-drop-context-type.groovy'
	include file: '20180730-converting-black-to-grey-tags.groovy'
	include file: '20180909-all-devices-system-view.groovy'
	include file: '20180808-all-databases-system-view.groovy'
	include file: '20180809-cleaning-up-orphaned-party-roles.groovy'
	include file: '20180803-create-tag-event.groovy'
	include file: '20180807-add-last-updated-to-move-event.groovy'
	include file: '20180809-all-storage-system-view.groovy'
	include file: '20180809-all-applications-system-view.groovy'
	include file: '20180904-project-table-drop-column-custom-fields-shown.groovy'
	include file: '20180829-rename-agent-to-connector.groovy'
	include file: '20180815-date-columns-to-have-date-controls-for-common-fields.groovy'
	include file: '20180917-move-bulk-specification-to-field-specs.groovy'
	include file: '20181024-person-table-drop-column-model-score-bonus.groovy'
	include file: '20181012-add-bulk-actions-for-standard-custom-and-other.groovy'
	include file: '20181105-move-bundle-should-have-list-control.groovy'
	include file: 'version/v4_6_0/20181030-update-validation.groovy'
	include file: 'version/v4_6_0/20181106-bundle-should-only-have-replace.groovy'
	include file: 'version/v4_6_0/20181115-delete-bogus-user-preferences.groovy'

	//We use the Dataview Domain in several migrations since it's been updated with a new column those
	//migrations will fail unless we add the new column before them
	include file: 'version/v4_7_2/20191105-add-dataview-permissions-and-property-for-save-as.groovy'


	include file: 'version/v4_6_0/20181115-reorder-system-views-columns.groovy'
	include file: 'version/v4_6_0/20181123-update-system-views-list-titles.groovy'
	include file: 'version/v4_6_0/20181123-add-tags-column-to-all-assets-view.groovy'
	include file: 'version/v4_6_0/20181127-delete-ReportViewDiscovery-permission.groovy'
	include file: 'version/v4_6_0/20181130-validated-should-only-have-replace.groovy'
	include file: 'version/v4_6_0/20181205-validation-should-only-have-replace.groovy'
	include file: 'version/v4_6_0/20181203-maintence-retire-date-times-to-dates.groovy'
	include file: 'version/v4_6_1/20181211-usability-and-style-changes-to-system-views-part1.groovy'
	include file: 'version/v4_6_1/20181227-add-comments-import-batch-record.groovy'
	include file: 'version/v4_6_1/20190107-add-and-modify-domain-constraints.groovy'
	include file: 'version/v4_6_2/2018-tmr-task-api-action-changes.groovy'
	include file: 'version/v4_6_2/20190311-eula-notifications-mandatory-acknowledgements-changes.groovy'
	include file: 'version/v4_6_2/20190319-rename-asset-system-views.groovy'
	include file: 'version/v4_7_0/20181219-Updating-role-prefix.groovy'
	include file: 'version/v4_6_2/2018-tmr-task-api-action-changes.groovy'
	include file: 'version/v4_6_2/20190311-eula-notifications-mandatory-acknowledgements-changes.groovy'
	include file: 'version/v4_6_2/20190319-rename-asset-system-views.groovy'
	include file: 'version/v4_6_2/20190530-add-guid-and-metrics_gathering-to-LicensedClient.groovy'
	include file: 'version/v4_6_3/20190619-add-boolean-debug-flag-to-apiAction.groovy'
	include file: 'version/v4_6_3/20190619-tmd-task-api-action-changes.groovy'
//	include file: 'version/v4_7_0/20190326-Updating-recipe-size.groovy'
	include file: 'version/v4_7_0/20190327-Change-scale-field-control-type-to-inList.groovy'
	include file: 'version/v4_7_0/20190328-usability-and-style-changes-to-system-views-part2.groovy'
	include file: 'version/v4_7_0/20190502-move-event-table-drop-column-news-bar-mode.groovy'
	include file: 'version/v4_7_0/20190516-asset-entity-table-rename-column-hinfo-to-os.groovy'
	include file: 'version/v4_6_2/20190530-add-guid-and-metrics_gathering-to-LicensedClient.groovy'
	include file: 'version/v4_7_0/20190628-set-max-asset-pagination-pref-value-to-100.groovy'
	include file: 'version/v4_7_0/20190701-Fixing-role-prefix-rollbacks.groovy'
	include file: 'version/v4_7_0/20190716-fix-unasigned-people-created-during-import-process.groovy'
	include file: 'version/v4_7_1/20190725-add-four-model-fields.groovy'
	include file: 'version/v4_7_1/20190814-add-permission-ETLScriptLoadSampleData.groovy'
	include file: 'version/v4_7_1/20190814-add-dependency-permissions.groovy'
	include file: 'version/v4_7_1/20190819-add-role-user-to-the-comment-view-permission.groovy'
	include file: 'version/v4_7_1/20190823-add-asset-bulk-select-permission.groovy'
	include file: 'version/v4_7_1/20190828-add-CPA-results-in-task.groovy'
	include file: 'version/v4_7_1/20190903-drop-workflow-columns.groovy'
	include file: 'version/v4_7_1/20190905-add-dictionaryMethodName.groovy'
	include file: 'version/v4_7_1/20191030-remove-plan-methodology-from-application-fieldspecs.groovy'
	include file: 'version/v4_7_1/20191112-fixing-role-prefix-in-asset-comment.groovy'
	include file: 'version/v4_7_1/20191202-remove-offending-records-in-dataview.groovy'
	include file: 'version/v4_7_2/20190930-update-dataviews.groovy'
	include file: 'version/v4_7_2/20191009-drop-tables-move-bundle-step-and-step-snapshot.groovy'
	include file: 'version/v5_0_0/20190920-add_fk_to_party_relationship.groovy'
	include file: 'version/v5_0_0/20191112-add_fk_to_asset_comment_asset.groovy'
	include file: 'version/v4_7_2/20191028-batch-management-auto-process-import.groovy'

	include file: 'version/v4_7_2/20191119-add-use_with_asset_actions.groovy'
	include file: 'version/v4_7_2/20191120-add-tags-import-batch-record.groovy'
	include file: 'version/v5_0_0/20190925-updating-password-hashing.groovy'
	include file: 'version/v5_0_0/20191126-permissions-for-consolidating-task-view-and-task-manager.groovy'
	include file: 'version/v4_7_2/20200103-making-from-address-nullable.groovy'
	include file: 'version/v5_0_0/20200110-add-tbd-conflict-depdency-group-columns-to-common-columns-view.groovy'
	include file: 'version/v5_0_0/20200114-drop-depdencyBundle-column.groovy'
	include file: 'version/v4_7_2/20200127-fix-empty-shared-field-specs.groovy'
	include file: 'version/v4_7_2/20200206-add-license-lastcompliance-column.groovy'
    include file: 'version/v4_7_2/20200129-remove-null-fields.groovy'
	include file: 'version/v4_7_2/20200218-drop-dataview-unique-index.groovy'
	include file: 'version/v5_0_0/20200414-increase-user-preference-value-length.groovy'
	include file: 'version/v5_0_0/20200414-resize-dependency-group-column-on-manage-views.groovy'

	include file: 'version/v4_7_3/20190423-add-category-codes-to-asset-options.groovy'
	include file: 'version/v5_0_0/20200515-fixing-dataviews.groovy'
}
