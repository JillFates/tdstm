/**
 *  The component is being used dynamically, some vars will show as not being used or referenced but they could be part
 *  of the GSP
 *
 *  Use angular/views/TheAssetType as reference
 */
import {Component, Inject, OnInit, AfterViewInit, ViewChild} from '@angular/core';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import * as R from 'ramda';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {AssetCommonEdit} from '../asset/asset-common-edit';
import { AddPersonComponent } from '../../../../shared/components/add-person/add-person.component';
import { PersonModel } from '../../../../shared/components/add-person/model/person.model';
import {PersonService} from '../../../../shared/services/person.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ASSET_ENTITY_DIALOG_TYPES} from '../../model/asset-entity.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

const pleaseSelectMessage = 'Please Select';

export function ApplicationCreateComponent(template: string, model: any, metadata: any): any {
	@Component({
		selector: 'tds-application-create',
		template: template,
		providers: [
			{ provide: 'model', useValue: model }
		]
	})
	class ApplicationCreateComponent extends AssetCommonEdit implements OnInit, AfterViewInit {
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

		@ViewChild('controlSME1', {static: false}) public controlSME1: any;
		@ViewChild('controlSME2', {static: false}) public controlSME2: any;
		@ViewChild('controlAppOwner', {static: false}) public controlAppOwner: any;

		constructor(
			@Inject('model') model: any,
			activeDialog: UIActiveDialogService,
			userContextService: UserContextService,
			permissionService: PermissionService,
			assetExplorerService: AssetExplorerService,
			dialogService: UIDialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService,
			translatePipe: TranslatePipe
			) {
				super(model, activeDialog, userContextService, permissionService, assetExplorerService, dialogService, notifierService, tagService, metadata, promptService, translatePipe);
		}

		ngOnInit() {
			this.initModel();
			this.focusControlByName('assetName');
			this.model.asset.validation = this.defaultValidation;
		}

		ngAfterViewInit() {
			this.onFocusOutOfCancel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.asset.retireDate =   '';
			this.model.asset.maintExpDate =  '';

			this.model.asset.moveBundle = this.model.dependencyMap.moveBundleList[0];
			this.moveBundleList = this.model.dependencyMap.moveBundleList;
			this.model.asset.planStatus = this.model.planStatusOptions.find((plan: string) => plan === this.defaultPlanStatus);
			this.model.asset.assetClass = {
				name: ASSET_ENTITY_DIALOG_TYPES.APPLICATION
			};

			this.model.asset.sme = {id: null };
			this.model.asset.sme2 =  {id: null };
			this.model.asset.appOwner = {id: null };
			this.model.asset.shutdownBy = {id: null };
			this.model.asset.startupBy = {id: null };
			this.model.asset.testingBy = {id: null };

			this.model.asset.startupProc = '';
			this.model.asset.testProc = '';
			this.model.asset.latency = '';
			this.model.asset.environment = '';
			this.model.asset.criticality = '';

			this.model.asset.scale = { name: { value: '', text: ''} };

			this.persons.sme = { personId: null};
			this.persons.sme2 = { personId: null };
			this.persons.appOwner = { personId: null};

			this.preparePersonList();
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
		 * Search and copy over the Person List for SME 1
		 * @param filter
		 */
		public filterSME1Change(filter: any): void {
			this.model.sme1PersonList = this.model.sourcePersonList.filter((s) => {
				return s.fullName.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
			});
		}

		/**
		 * On focus open the dropdown
		 */
		public focusSME1(): void {
			this.controlSME1.toggle(true);
			this.controlSME2.toggle(false);
			this.controlAppOwner.toggle(false);
		}

		/**
		 * Search and copy over the Person List for SME 2
		 * @param filter
		 */
		public filterSME2Change(filter: any): void {
			this.model.sme2PersonList = this.model.sourcePersonList.filter((s) => {
				return s.fullName.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
			});
		}

		/**
		 * On focus open the dropdown
		 */
		public focusSME2(): void {
			this.controlSME1.toggle(false);
			this.controlSME2.toggle(true);
			this.controlAppOwner.toggle(false);
		}

		/**
		 * Search and copy over the Person List for App Owner
		 * @param filter
		 */
		public filterAppOwnerChange(filter: any): void {
			this.model.appOwnerPersonList = this.model.sourcePersonList.filter((s) => {
				return s.fullName.toLowerCase().indexOf(filter.toLowerCase()) !== -1;
			});
		}

		/**
		 * On focus open the dropdown
		 */
		public focusAppOwner(): void {
			this.controlSME1.toggle(false);
			this.controlSME2.toggle(false);
			this.controlAppOwner.toggle(true);
		}

		public onClose(event: any, dropdownlist: any): void {
			event.preventDefault();
			// Close the list if the component is no longer focused
			setTimeout(() => {
				if (!dropdownlist.wrapper.nativeElement.contains(document.activeElement)) {
					dropdownlist.toggle(false);
				}
			});
		}

		/**
		 * Add the person to the Asset Model, if the Person is "Add Person" it invokes the Dialog to add a new one
		 * @param person
		 * @param asset
		 * @param fieldName
		 * @param companies
		 * @param teams
		 * @param staffTypes
		 * @param modelListParameter
		 * @param dropdown
		 */
		onAddPerson(person: any, asset: string, fieldName: string, companies: any[], teams: any[], staffTypes: any[], modelListParameter: string, dropdown: any): void {
			if (person.personId !== this.addPersonItem.personId) {
				this.model.asset[fieldName].id = person.personId;
				dropdown.toggle(false);
				return;
			}

			dropdown.toggle(false);
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
					if (this.model.sourcePersonList && this.model[modelListParameter]) {
						this.model.sourcePersonList.push({personId: result.id, fullName: result.name});
						this.model[modelListParameter].push({personId: result.id, fullName: result.name});
					}
					this.model.asset[fieldName].id = result.id;
					this.updatePersonReferences();
				})
				.catch((error) => {
					// get back to previous value
					this.persons[fieldName] = { personId: this.model.asset[fieldName].id};
				});
		}

		/**
		 * Prepare the Person List with the Person Create if it has the permission
		 */
		preparePersonList() {
			this.model.personList.unshift(this.addPersonItem);
			// Save a copy of the Person List
			this.model.sourcePersonList = R.clone(this.model.personList);
			// Create each instance
			this.model.appOwnerPersonList = R.clone(this.model.sourcePersonList);
			this.model.sme1PersonList = R.clone(this.model.sourcePersonList);
			this.model.sme2PersonList = R.clone(this.model.sourcePersonList);
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
