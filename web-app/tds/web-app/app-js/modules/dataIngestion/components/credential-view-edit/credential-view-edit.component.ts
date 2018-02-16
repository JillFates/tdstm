import {Component, ViewChild, ViewChildren, HostListener, QueryList} from '@angular/core';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CredentialModel, AUTH_METHODS, REQUEST_METHOD} from '../../model/credential.model';
import {ProviderModel} from '../../model/provider.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ActionType} from '../../../../shared/model/data-list-grid.model';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {NgForm} from '@angular/forms';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import {CHECK_ACTION} from '../../../../shared/components/check-action/model/check-action.model';
import * as R from 'ramda';

declare var jQuery: any;

@Component({
	selector: 'credential-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/credential-view-edit/credential-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
		.invalid-form {
			color: red;
			font-weight: bold;
		}
		
		.script-error {
			margin-bottom: 18px;
		}
		#httpMethod {
			width: 75px;
		}
        .radio-aligned {
            margin: 4px 4px 0;
            vertical-align: top;
        }
		.label-detail {
            font-weight: normal;
			cursor: pointer;
		}
	`]
})
export class CredentialViewEditComponent {

	// Forms
	@ViewChild('apiActionForm') apiActionForm: NgForm;
	@ViewChild('apiActionReactionForm') apiActionReactionForm: NgForm;

	@ViewChild('apiActionProvider', { read: DropDownListComponent }) apiActionProvider: DropDownListComponent;
	@ViewChild('credentialStatus', { read: DropDownListComponent }) credentialStatus: DropDownListComponent;
	@ViewChild('apiActionAgentMethod', { read: DropDownListComponent }) apiActionAgentMethod: DropDownListComponent;
	@ViewChild('apiActionCredential', { read: DropDownListComponent }) apiActionCredential: DropDownListComponent;

	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;

	public codeMirrorComponent: CodeMirrorComponent;

	public credentialModel: CredentialModel;
	public providerList = new Array<ProviderModel>();
	public statusList = new Array<any>();
	public authMethodList = new Array<any>();
	public environmentList = new Array<any>();
	public httpMethodList = new Array<any>();
	public modalTitle: string;
	public actionTypes = ActionType;
	public requestMethod = REQUEST_METHOD;
	private dataSignature: string;
	public authMethods = AUTH_METHODS;
	public isEditing = false;
	public checkActionModel = CHECK_ACTION;
	constructor(
		public originalModel: CredentialModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService) {

		// Sub Objects are not being created, just copy
		this.credentialModel = R.clone(this.originalModel);

		if (this.modalType === ActionType.CREATE) {
			this.credentialModel.requestMethod = this.requestMethod.BASIC_AUTH;
		}

		this.dataSignature = JSON.stringify(this.credentialModel);

		this.getProviders();
		this.getAuthMethods();
		this.getCredentialEnumsConfig();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create Credential' : (this.modalType === ActionType.EDIT ? 'Credential Edit' : 'Credential Detail');
	}

	/**
	 * Get the List of Providers
	 */
	private getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({ id: 0, name: 'Select...' });
					this.credentialModel.provider = this.providerList[0];
					this.modifySignatureByProperty('provider');
				}
				this.providerList.push(...result);
			},
			(err) => console.log(err));
	}

	/**
	 * Get from the Server all Enums and do the Mapping by converting the Enums into Arrays of Native (String) Values
	 */
	private getCredentialEnumsConfig(): void {
		this.dataIngestionService.getCredentialEnumsConfig().subscribe(
			(result: any) => {
				this.environmentList = Object.keys(result['environment']).map(type => {
					return result['environment'][type];
				});
				this.statusList = Object.keys(result['status']).map(type => {
					return result['status'][type];
				});
				this.httpMethodList = Object.keys(result['httpMethod']).map(type => {
					return result['httpMethod'][type];
				});
				this.authMethodList = Object.keys(result['authenticationMethod']).map(type => {
					return result['authenticationMethod'][type];
				});
				if (this.modalType === ActionType.CREATE) {
					// Environments List Mapper
					this.credentialModel.environment = this.environmentList[0];
					this.modifySignatureByProperty('environment');
					// Status List Mapper
					this.credentialModel.status = this.statusList[0];
					this.modifySignatureByProperty('status');
					// HTTP Method List Mapper
					this.credentialModel.httpMethod = this.httpMethodList[0];
					this.modifySignatureByProperty('httpMethod');
					// Auth. Method List Mapper
					this.credentialModel.authMethod = this.authMethodList[0];
					this.modifySignatureByProperty('authenticationMethod');
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Set the possible Status for the Credential
	 */
	private getAuthMethods(): void {
		this.authMethodList = Object.keys(AUTH_METHODS).map(type => {
			return AUTH_METHODS[type];
		});
		if (this.modalType === ActionType.CREATE) {
			this.credentialModel.authMethod = this.authMethodList[0];
			this.modifySignatureByProperty('authMethod');
		}
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveCredential(): void {
		this.dataIngestionService.saveCredential(this.credentialModel).subscribe(
			(result: any) => {
				this.activeDialog.close(result);
			},
			(err) => console.log(err));

	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.credentialModel);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.activeDialog.dismiss();
					}
				});
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('document:keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditCredential(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteCredential(): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteAPIAction(this.credentialModel.id).subscribe(
						(result) => {
							this.activeDialog.close(result);
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Keep Data Signature Clean even when there are so many values incoming
	 * @param property
	 * @param value
	 */
	private modifySignatureByProperty(property: any): void {
		this.dataSignature = ObjectUtils.modifySignatureByProperty(this.dataSignature, property, this.credentialModel[property]);
	}
}