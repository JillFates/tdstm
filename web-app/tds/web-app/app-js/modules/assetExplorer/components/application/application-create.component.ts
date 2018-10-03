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
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';

const pleaseSelectMessage = 'Please Select';

export function ApplicationCreateComponent(template: string, model: any, metadata: any): any {
	@Component({
		selector: 'tds-application-create',
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	})
	class ApplicationCreateComponent extends AssetCommonEdit {
		defaultItem = {fullName: pleaseSelectMessage, personId: null};
		addPersonItem = {fullName: 'Add person', personId: -1};
		yesNoList = ['Y', 'N'];
		personList: any[] = null;
		persons = {
			sme: null,
			sme2: null,
			appOwner: null
		};

		constructor(
			@Inject('model') model: any,
			activeDialog: UIActiveDialogService,
			preference: PreferenceService,
			assetExplorerService: AssetExplorerService,
			dialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService,
			private prompt: UIPromptService,
			) {
				super(model, activeDialog, preference, assetExplorerService, dialogService, notifierService, tagService, metadata, promptService);
				this.initModel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.asset = {};
			this.model.asset.retireDate =   '';
			this.model.asset.maintExpDate =  '';

			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.model.asset.planStatus = this.model.planStatusOptions[0];
			this.model.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.APPLICATION
			};

			this.model.asset.sme = this.model.asset.sme || { id: null };
			this.model.asset.sme2 = this.model.asset.sme2 || { id: null };
			this.model.asset.appOwner = this.model.asset.appOwner || { id: null };

			this.model.asset.scale = { name: '' };
			this.model.asset.startUpBySelectedValue = { id: null, text: pleaseSelectMessage};

			this.persons.sme = { personId: null};
			this.persons.sme2 = { personId: null };
			this.persons.appOwner = { personId: null};
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
				this.updatePersonReferences();
			}
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		public onCreate(): void {
			const modelRequest   = R.clone(this.model);

			if (modelRequest && modelRequest.asset.moveBundle) {
				modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			}

			// Scale Format
			if (modelRequest.asset && modelRequest.asset.scale) {
				modelRequest.asset.scale = (modelRequest.asset.scale.name.value) ? modelRequest.asset.scale.name.value : modelRequest.asset.scale.name;
			}

			// Custom Fields
			Object.keys(modelRequest.asset)
				.filter((key: string) => key.startsWith('custom'))
				.forEach((key: string) => {
					modelRequest.asset[key] = modelRequest.asset[key].value ? modelRequest.asset[key].value : modelRequest.asset[key];
				});

			/*
			(this.model.customs || []).forEach((custom: any) => {
				let customValue = modelRequest.asset[custom.field.toString()];
				if (customValue && customValue.value) {
					modelRequest.asset[custom.field.toString()] = customValue.value;
				}
			});
			*/

			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});
				if (result === ApiResponseModel.API_SUCCESS || result === 'Success!') {
					this.saveAssetTags();
				}
			});
		}
		onAddPerson(person: any, asset: string, fieldName: string, companies: any[], teams: any[], staffTypes: any[]): void {
			if (person.personId !== this.addPersonItem.personId) {
				this.model.asset[fieldName].id = person.personId;
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
				], false, true)
				.then((result) => {
					this.personList.push({personId: result.id, fullName: result.name})
					this.model.asset[fieldName].id = result.id;
					this.updatePersonReferences();
				})
				.catch((error) => {
					// get back to previous value
					this.persons[fieldName] = { personId: this.model.asset[fieldName].id};
				});
		}

		getPersonList(personList: any[]): any[] {
			if (!this.personList) {
				this.personList = personList;
				this.personList.unshift(this.addPersonItem)
			}
			return this.personList;
		}

		updatePersonReferences(): void {
			this.persons.sme = { personId: this.model.asset.sme.id};
			this.persons.sme2 = { personId: this.model.asset.sme2.id};
			this.persons.appOwner = { personId: this.model.asset.appOwner.id};
		}

	}

	return ApplicationCreateComponent;
}