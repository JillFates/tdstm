/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import { Component, Inject, OnInit } from '@angular/core';
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
	class ApplicationCreateComponent extends AssetCommonEdit implements OnInit {
		defaultItem = {fullName: pleaseSelectMessage, personId: null};
		addPersonItem = {fullName: 'Add person', personId: -1};
		moveBundleList = [];
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
		}

		ngOnInit() {
			this.initModel();
			this.focusControlByName('assetName');
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.asset.retireDate =   '';
			this.model.asset.maintExpDate =  '';

			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.moveBundleList = this.model.dependencyMap.moveBundleList;
			this.model.asset.planStatus = this.model.planStatusOptions[0];
			this.model.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.APPLICATION
			};

			this.model.asset.sme = {id: null };
			this.model.asset.sme2 =  {id: null };
			this.model.asset.appOwner = {id: null };
			this.model.asset.shutdownBy = {id: null };
			this.model.asset.startupBy = {id: null };
			this.model.asset.testingBy = {id: null };

			this.model.asset.scale = { name: { value: '', text: ''} };

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
			modelRequest.asset.scale = (modelRequest.asset.scale && modelRequest.asset.scale.name && modelRequest.asset.scale.name.value || '');
			modelRequest.asset.shutdownBy = modelRequest.asset.shutdownBy && modelRequest.asset.shutdownBy.id || '';
			modelRequest.asset.startupBy = modelRequest.asset.startupBy && modelRequest.asset.startupBy.id || '';
			modelRequest.asset.testingBy = modelRequest.asset.testingBy && modelRequest.asset.testingBy.id || '';

			modelRequest.asset.environment  = modelRequest.asset.environment === this.defaultSelectOption ?
				'' : modelRequest.asset.environment;

			modelRequest.asset.criticality  = modelRequest.asset.criticality === this.defaultSelectOption ?
				'' : modelRequest.asset.criticality;

			// Custom Fields
			Object.keys(modelRequest.asset)
				.filter((key: string) => key.startsWith('custom'))
				.forEach((key: string) => {
					modelRequest.asset[key] = modelRequest.asset[key] && modelRequest.asset[key].value
						? modelRequest.asset[key].value : modelRequest.asset[key];
				});

			this.assetExplorerService.createAsset(modelRequest).subscribe((result) => {
				this.notifierService.broadcast({
					name: 'reloadCurrentAssetList'
				});

				if (result.id && !isNaN(result.id) && result.id > 0) {
					this.createTags(result.id);
				}
			});
		}

		/**
		 * Open the dialog to allow create a person
		 * @param {any}  person Contains the info related to the asset in which the person will be  created
		 * @param {string}  fieldName Contains the field asset type
		 * @param {any[]}  companies List of companies to display
		 * @param {any[]}  teams List of teams to display
		 * @param {any[]}  staffTypes List of staffs types to display
		 * @returns {void}
		 */
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

		/**
		 * Get the current array containing the person items
		 * It adds the default Add Person item
		 */
		getPersonList(personList: any[]): any[] {
			if (!this.personList) {
				this.personList = personList;
				this.personList.unshift(this.addPersonItem)
			}
			return this.personList;
		}

		/**
		 * Set the correct model values to objects of type person
		 */
		updatePersonReferences(): void {
			this.persons.sme = { personId: this.model.asset.sme.id};
			this.persons.sme2 = { personId: this.model.asset.sme2.id};
			this.persons.appOwner = { personId: this.model.asset.appOwner.id};
		}

	}

	return ApplicationCreateComponent;
}