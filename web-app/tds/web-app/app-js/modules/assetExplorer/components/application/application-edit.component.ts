/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, Inject} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';

import { PreferenceService } from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import * as R from 'ramda';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import { AddPersonComponent } from '../../../../shared/components/add-person/add-person.component';
import { PersonModel } from '../../../../shared/components/add-person/model/person.model';
import {PersonService} from '../../../../shared/services/person.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

export function ApplicationEditComponent(template: string, editModel: any, metadata: any): any {
	@Component({
		selector: 'application-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class ApplicationShowComponent extends AssetCommonEdit {
		defaultItem = {fullName: 'Please Select', personId: 0};
		addPersonItem = {fullName: 'Add person', personId: -1};
		yesNoList = ['Y', 'N'];
		personList: any[] = null;

		constructor(
			@Inject('model') model: any,
			activeDialog: UIActiveDialogService,
			preference: PreferenceService,
			assetExplorerService: AssetExplorerService,
			dialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService) {
				super(model, activeDialog, preference, assetExplorerService, dialogService, notifierService, tagService, metadata, promptService);
				this.initModel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
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

		/**
		 * Swap values among two persons
		 * @param {source}  name of the source asset
		 * @param {target}  name of the target asset
		 */
		public shufflePerson(source: string, target: string): void {
			const sourceId = this.model.asset && this.model.asset[source] && this.model.asset[source].id || null;
			const targetId = this.model.asset && this.model.asset[target] && this.model.asset[target].id || null;

			if (sourceId && targetId) {
				const backSource = sourceId;
				this.model.asset[source].id = targetId;
				this.model.asset[target].id = backSource;
			}
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		public onUpdate(): void {
			const modelRequest   = R.clone(this.model);

			if ((modelRequest.asset.appOwner && modelRequest.asset.appOwner.id && modelRequest.asset.appOwner.id.personId) || (modelRequest.asset.appOwner.id && modelRequest.asset.appOwner.id.personId === 0)) {
				modelRequest.asset.appOwner.id = (modelRequest.asset.appOwner.id.personId !== 0) ? modelRequest.asset.appOwner.id.personId : null;
			}

			if ((modelRequest.asset.sme && modelRequest.asset.sme.id && modelRequest.asset.sme.id.personId) || (modelRequest.asset.sme.id && modelRequest.asset.sme.id.personId === 0)) {
				modelRequest.asset.sme.id = (modelRequest.asset.sme.id.personId !== 0) ? modelRequest.asset.sme.id.personId : null;
			}

			if ((modelRequest.asset.sme2 && modelRequest.asset.sme2.id && modelRequest.asset.sme2.id.personId) || (modelRequest.asset.sme2.id && modelRequest.asset.sme2.id.personId === 0)) {
				modelRequest.asset.sme2.id = (modelRequest.asset.sme2.id.personId !== 0) ? modelRequest.asset.sme2.id.personId : null;
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

			this.assetExplorerService.saveAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}
		onPersonSelected(person: any, asset: string, fieldName: string, companies: any[], teams: any[], staffTypes: any[]): void {
			if (person.personId !== this.addPersonItem.personId) {
				return;
			}

			const personModel = new PersonModel();
			personModel.asset = asset;
			personModel.fieldName = fieldName;
			personModel.companies = companies || [];
			personModel.teams = teams;
			personModel.staffType = staffTypes || [];
			this.dialogService.extra(AddPersonComponent,
				[UIDialogService,
					{
						provide: PersonModel,
						useValue: personModel
					},
					PersonService
				], false, false)
				.then((result) => {
					this.personList.push({personId: result.id, fullName: result.name})
					this.model.asset[fieldName].id = result.id;
				})
				.catch(err => console.log('Cancelling add person'));
		}
		getPersonList(personList: any[]): any[] {
			if (!this.personList) {
				this.personList = personList;
				this.personList.unshift(this.addPersonItem)
			}
			return this.personList;
		}
	}

	return ApplicationShowComponent;
}