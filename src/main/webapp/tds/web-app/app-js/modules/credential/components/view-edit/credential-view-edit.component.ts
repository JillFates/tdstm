import {Component, ViewChild, ViewChildren, HostListener, QueryList, ElementRef, Inject} from '@angular/core';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {CredentialModel, AUTH_METHODS, REQUEST_MODE} from '../../model/credential.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {CredentialService} from '../../service/credential.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ActionType} from '../../../../shared/model/data-list-grid.model';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {NgForm} from '@angular/forms';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import * as R from 'ramda';
import {Observable} from 'rxjs';

declare var jQuery: any;

@Component({
	selector: 'credential-view-edit',
	templateUrl: 'credential-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
        .invalid-form {
            color: red;
            font-weight: bold;
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
        .check-action {
            margin-left: 12px !important;
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
	@ViewChild('credentialsContainer') credentialsContainer: ElementRef;

	public codeMirrorComponent: CodeMirrorComponent;
	public credentialModel: CredentialModel;
	public providerList = new Array<ProviderModel>();
	public statusList = new Array<any>();
	public authMethodList = new Array<any>();
	public environmentList = new Array<any>();
	public httpMethodList = new Array<any>();
	public modalTitle: string;
	public actionTypes = ActionType;
	public requestMode = REQUEST_MODE;
	private dataSignature: string;
	public authMethods = AUTH_METHODS;
	public isEditing = false;
	public checkActionModel = CHECK_ACTION;
	public operationStatusModel = new OperationStatusModel();
	public validExpressionResult = {
		valid: true,
		error: ''
	};
	constructor(
		public originalModel: CredentialModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private credentialService: CredentialService) {

		// Sub Objects are not being created, just copy
		this.credentialModel = R.clone(this.originalModel);

		if (this.modalType === ActionType.CREATE) {
			this.credentialModel.requestMode = this.requestMode.BASIC_AUTH;
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
		this.credentialService.getProviders().subscribe(
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
		this.credentialService.getCredentialEnumsConfig().subscribe(
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
	 * Execute the flow to create/save/update a credential
	 */
	protected onSaveCredential(): void {
		// Cookie and Header requires an extra validation before to save the credential
		if (this.credentialModel.authMethod === this.authMethods.COOKIE ||
			this.credentialModel.authMethod === this.authMethods.HEADER ||
			this.credentialModel.authMethod === this.authMethods.BASIC_AUTH) {
			this.validateExpressionCheck().subscribe((result) => {
				if (result) {
					this.validExpressionResult = result;
					if (this.validExpressionResult.valid) {
						this.saveCredential();
					}
				}
			});
		} else {
			// For other Auth methods rather than Cookie or Header
			this.saveCredential();
		}
	}

	/**
	 * Createm Save or Update the Credential forom the model
	 */
	private saveCredential(): void {
		this.credentialService.saveCredential(this.credentialModel).subscribe(
			(result: any) => {
				if (result && result.id) {
					this.activeDialog.close(result);
				}
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
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.close(null);
					} else {
						this.focusForm();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.close(null);
		}
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditCredential(): void {
		this.modalType = this.actionTypes.EDIT;
		this.focusForm();
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteCredential(): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.credentialService.deleteCredential(this.credentialModel.id).subscribe(
						(result) => {
							this.activeDialog.dismiss(result);
						},
						(err) => console.log(err));
				}
			});
	}

	protected verifyCode(operationStatusModel: OperationStatusModel): void {
		this.credentialService.validateAuthentication(this.credentialModel.id).subscribe(
			(result: any) => {
				if (!result) {
					operationStatusModel.state = this.checkActionModel.INVALID;
				} else {
					operationStatusModel.state = this.checkActionModel.VALID;
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Listener that wait a subscribe, so it can be attached as a callback-promise to the save of the credential.
	 */
	protected onValidateExpression(): void {
		this.validateExpressionCheck().subscribe();
	}

	/**
	 * Verify against the endpoint if the expression is valid.
	 * @returns {Observable<any>}
	 */
	private validateExpressionCheck(): Observable<any> {
		return new Observable(observer => {
			this.credentialService.validateExpressionCheck(this.credentialModel.validationExpression).subscribe(
				(result: any) => {
					this.validExpressionResult = result;
					observer.next(result);
				},
				(err) => console.log(err));
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

	private focusForm() {
		this.credentialsContainer.nativeElement.focus();
	}

	protected isCheckSyntaxDisabled(): boolean {
		return this.operationStatusModel.state === CHECK_ACTION.VALID;
	}
}