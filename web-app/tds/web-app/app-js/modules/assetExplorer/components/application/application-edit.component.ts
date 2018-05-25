/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, ViewChild, Inject, OnInit } from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';

import { PreferenceService } from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AssetShowComponent} from '../asset/asset-show.component';

export function ApplicationEditComponent(template: string, editModel: any): any {
	@Component({
		selector: 'application-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class ApplicationShowComponent implements OnInit {
		defaultItem = {fullName: 'Please Select', personId: 0};
		yesNoList = ['Y', 'N'];
		private dateFormat: string;
		constructor(
			@Inject('model') private model: any,
			private activeDialog: UIActiveDialogService,
			private dialogService: UIDialogService,
			private assetExplorerService: AssetExplorerService,
			private notifierService: NotifierService,
			private preference: PreferenceService) {}
		ngOnInit(): void {
			console.log('Loading application-edit.component');
			this.dateFormat = this.preference.preferences['CURR_DT_FORMAT'];
			this.dateFormat = this.dateFormat.toLowerCase().replace(/m/g, 'M');
			this.initModel();
		}

		private initModel(): void {

			this.model.asset = R.clone(editModel.asset);
			this.model.asset.retireDate = DateUtils.compose(this.model.asset.retireDate);
			this.model.asset.maintExpDate = DateUtils.compose(this.model.asset.maintExpDate);

			this.model.asset.sme = this.model.asset.sme || { id: null };
			this.model.asset.sme2 = this.model.asset.sme2 || { id: null };
			this.model.asset.appOwner = this.model.asset.appOwner || { id: null };

			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: ''
				};
			}

			this.model.asset.startUpBySelectedValue = { id: null, text: 'Please Select'};
			if (this.model.asset.startUpBySelectedValue) {
				this.model.asset.startUpBySelectedValue.id = this.model.asset.startupBy;
			}

		}

		shufflePerson(source: string, target: string) {
			const sourceId = this.model.asset && this.model.asset[source] && this.model.asset[source].id || null;
			const targetId = this.model.asset && this.model.asset[target] && this.model.asset[target].id || null;

			if (sourceId && targetId) {
				const backSource = sourceId;

				this.model.asset[source].id = targetId;
				this.model.asset[target].id = backSource;
			}
		}

		/***
		 * Close the Active Dialog
		 */
		cancelCloseDialog(): void {
			this.activeDialog.close();
		}

		private showAssetDetailView(assetClass: string, id: number) {
			this.dialogService.replace(AssetShowComponent, [
					{ provide: 'ID', useValue: id },
					{ provide: 'ASSET', useValue: assetClass }],
				'lg');
		}

		onUpdate(): void {
			const modelRequest   = R.clone(this.model);

			if (modelRequest.asset.appOwner && modelRequest.asset.appOwner.id && modelRequest.asset.appOwner.id.personId ) {
				modelRequest.asset.appOwner.id = modelRequest.asset.appOwner.id.personId;
			}

			if (modelRequest.asset.sme && modelRequest.asset.sme.id && modelRequest.asset.sme.id.personId ) {
				modelRequest.asset.sme.id = modelRequest.asset.sme.id.personId;
			}

			if (modelRequest.asset.sme2 && modelRequest.asset.sme2.id && modelRequest.asset.sme2.id.personId ) {
				modelRequest.asset.sme2.id = modelRequest.asset.sme2.id.personId;
			}

			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;

			// Scale Format
			modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;

			// Custom Fields
			this.model.customs.forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});

			this.assetExplorerService.saveAsset(modelRequest).subscribe((res) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
			});

			console.log(modelRequest);
		}
	}

	return ApplicationShowComponent;
}