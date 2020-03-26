// Angular
import {Component, ComponentFactoryResolver, Inject, OnInit, ViewChild} from '@angular/core';
// Model
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
// Component
import {AssetCommonEdit} from '../asset/asset-common-edit';
import {AddPersonComponent} from '../../../../shared/components/add-person/add-person.component';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Service
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {TagService} from '../../../assetTags/service/tag.service';
import {PersonService} from '../../../../shared/services/person.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import * as R from 'ramda';
import {DialogService, ModalSize} from 'tds-component-library';

export function ApplicationEditComponent(template: string, editModel: any, metadata: any, parentDialog: any): any {
	@Component({
		selector: 'tds-application-edit',
		template: template,
		providers: [
			{ provide: 'model', useValue: editModel }
		]
	})
	class ApplicationShowComponent extends AssetCommonEdit implements OnInit {
		defaultItem = {fullName: 'Please Select', personId: null};
		addPersonItem = {fullName: 'Add person', personId: -1};
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
			componentFactoryResolver: ComponentFactoryResolver,
			userContextService: UserContextService,
			permissionService: PermissionService,
			assetExplorerService: AssetExplorerService,
			dialogService: DialogService,
			notifierService: NotifierService,
			tagService: TagService,
			promptService: UIPromptService,
			translatePipe: TranslatePipe,
			) {
				super(componentFactoryResolver, model, userContextService, permissionService, assetExplorerService, dialogService, notifierService, tagService, metadata, translatePipe, parentDialog);
		}

		ngOnInit() {
			this.initModel();
			this.focusControlByName('assetName');
			this.onFocusOutOfCancel();
		}

		/**
		 * Init model with necessary changes to support UI components.
		 */
		private initModel(): void {
			this.model.asset = R.clone(editModel.asset);
			this.model.asset.sme = this.model.asset.sme || { id: null };
			this.model.asset.sme2 = this.model.asset.sme2 || { id: null };
			this.model.asset.shutdownBy = {id : this.model.asset.shutdownBy || null };
			this.model.asset.startupBy = {id: this.model.asset.startupBy || null };
			this.model.asset.testingBy = {id: this.model.asset.testingBy || null };
			this.model.asset.appOwner = this.model.asset.appOwner || { id: null };
			if (this.model.asset.scale === null) {
				this.model.asset.scale = {
					name: { value: '', text: ''}
				};
			}
			this.updatePersonReferences();
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

			this.model.asset[source].id = targetId;
			this.model.asset[target].id = sourceId;

			this.updatePersonReferences();
		}

		/**
		 * On Update button click save the current model form.
		 * Method makes proper model modification to send the correct information to
		 * the endpoint.
		 */
		public onUpdate(): void {
			const modelRequest   = R.clone(this.model);

			modelRequest.asset.moveBundleId = modelRequest.asset.moveBundle.id;
			delete modelRequest.asset.moveBundle;

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

			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: AddPersonComponent,
				data: {
					personModel: personModel
				},
				modalConfiguration: {
					title: 'Person Create',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-add-person-dialog'
				}
			}).subscribe((data: any) => {
				if (this.model.sourcePersonList && this.model[modelListParameter]) {
					this.model.sourcePersonList.push({personId: data.id, fullName: data.name});
					this.model[modelListParameter].push({personId: data.id, fullName: data.name});
				}
				this.model.asset[fieldName].id = data.id;
				this.updatePersonReferences();
			});
		}

		/**
		 * Prepare the Person List with the Person Create if it has the permission
		 */
		preparePersonList() {
			if (this.permissionService.hasPermission('PersonCreate')) {
				this.model.personList.unshift(this.addPersonItem);
			}
			// Save a copy of the Person List
			this.model.sourcePersonList = R.clone(this.model.personList);
			// Create each instance
			this.model.appOwnerPersonList = R.clone(this.model.sourcePersonList);
			this.model.sme1PersonList = R.clone(this.model.sourcePersonList);
			this.model.sme2PersonList = R.clone(this.model.sourcePersonList);
		}

		updatePersonReferences(): void {
			this.persons.sme = { personId: this.model.asset.sme.id};
			this.persons.sme2 = { personId: this.model.asset.sme2.id};
			this.persons.appOwner = { personId: this.model.asset.appOwner.id};
		}

		/**
			allows to delete the application assets
		*/
		onDeleteAsset() {

			this.deleteAsset(this.model.asset.id);
		}
	}

	return ApplicationShowComponent;
}
