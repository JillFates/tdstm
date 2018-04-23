/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */

import * as R from 'ramda';
import {Component, Inject, OnInit} from '@angular/core';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {SelectableSettings} from '@progress/kendo-angular-grid';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

declare var jQuery: any;

export function DeviceEditComponent(template, editModel) {

	@Component({
		selector: `device-edit`,
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	}) class DeviceEditComponent implements OnInit {

		private dateFormat: string;

		constructor(
					@Inject('model') private model: any,
					private activeDialog: UIActiveDialogService,
					private preference: PreferenceService) {

			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}
		}

		/**
		 * Get the List of Supports On
		 */
		// private getSupportOnList(): void {
		// 	this.supportOnColumnModel = new SupportOnColumnsModel();
		// 	let supportsOn = [];
		// 	if (editModel.dependencyMap && editModel.dependencyMap.supportAssets) {
		// 		let supportAssets = R.clone(editModel.dependencyMap.supportAssets);
		// 		supportAssets.forEach((dependency) => {
		// 			let dependencySupportModel: DependencySupportModel = {
		// 				dataFlowFreq: dependency.dataFlowFreq,
		// 				dependencyType: dependency.type,
		// 				dependencyStatus: dependency.status
		// 			};
		// 			supportsOn.push(dependencySupportModel);
		// 		});
		// 	}
		// 	this.dataGridSupportsOnHelper = new DataGridOperationsHelper(supportsOn, this.initialSort, this.selectableSettings);
		// }

		/**
		 * Add a new Support On Dependency
		 */
		// public onAddSupportsOn(): void {
		// 	this.dataGridSupportsOnHelper.addDataItem({});
		// }

		/**
		 * Delete the selected element
		 */
		// public onDeleteSupport(dataItem: any): void {
		// 	this.dataGridSupportsOnHelper.removeDataItem(dataItem);
		// }

		/**
		 * Initiates The Injected Component
		 */
		ngOnInit(): void {
			jQuery('[data-toggle="popover"]').popover();
		}

		/***
		 * Close the Active Dialog
		 */
		public cancelCloseDialog(): void {
			this.activeDialog.close();
		}

	}
	return DeviceEditComponent;
}