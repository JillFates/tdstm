// Angular
import {Component, ElementRef, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {AUTH_METHODS, CredentialModel, REQUEST_MODE} from '../../model/credential.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {ActionType} from '../../../../shared/model/data-list-grid.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
// Component
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
// Service
import {CredentialService} from '../../service/credential.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
// Other
import * as R from 'ramda';
import {Observable} from 'rxjs';

@Component({
	selector: 'credential-view-edit',
	templateUrl: 'credential-view-edit.component.html',
	styles: [
			`
            .has-error,
            .has-error:focus {
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
		`,
	],
})
export class CredentialViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	// Forms
	@ViewChild('credentialForm', {static: false}) credentialForm: NgForm;

	@ViewChild('apiActionProvider', {
		read: DropDownListComponent,
		static: true
	}) apiActionProvider: DropDownListComponent;
	@ViewChild('credentialStatus', {read: DropDownListComponent, static: true}) credentialStatus: DropDownListComponent;
	@ViewChild('apiActionAgentMethod', {
		read: DropDownListComponent,
		static: true
	}) apiActionAgentMethod: DropDownListComponent;
	@ViewChild('apiActionCredential', {
		read: DropDownListComponent,
		static: true
	})
	apiActionCredential: DropDownListComponent;

	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;
	@ViewChild('credentialsContainer', {static: false})
	credentialsContainer: ElementRef;

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
	public modalType = ActionType.VIEW;
	public validExpressionResult = {
		valid: true,
		error: '',
	};
	private requiredFields = [
		'name',
		'provider',
		'username',
		'authenticationUrl',
		'httpMethod',
		'validationExpression',
	];

	constructor(
		public dialogService: DialogService,
		private credentialService: CredentialService,
		private translatePipe: TranslatePipe
	) {
		super();
	}

	ngOnInit(): void {
		// Sub Objects are not being created, just copy
		this.credentialModel = R.clone(this.data.credentialModel);
		this.dataSignature = JSON.stringify(this.credentialModel);
		this.modalType = this.data.actionType;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditCredential.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => this.isFormInvalid(this.credentialForm) || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.onSaveCredential.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.modalType !== this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.onDeleteCredential.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => this.modalType === this.actionTypes.VIEW || this.modalType === this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.cancelEditDialog.bind(this)
		});

		this.getProviders();
		this.getAuthMethods();
		this.getCredentialEnumsConfig();

		// for create initialize the validations different
		if (this.modalType === ActionType.CREATE) {
			this.credentialModel.requestMode = this.requestMode.BASIC_AUTH;
			this.requiredFields.push('password');
		}

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
		});
	}

	/**
	 * Get the List of Providers
	 */
	private getProviders(): void {
		this.credentialService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({
						id: 0,
						name: this.translatePipe.transform(
							'GLOBAL.SELECT_PLACEHOLDER'
						),
					});
					this.credentialModel.provider = this.providerList[0];
					this.modifySignatureByProperty('provider');
				}
				this.providerList.push(...result);
			},
			err => console.log(err)
		);
	}

	/**
	 * Get from the Server all Enums and do the Mapping by converting the Enums into Arrays of Native (String) Values
	 */
	private getCredentialEnumsConfig(): void {
		this.credentialService.getCredentialEnumsConfig().subscribe(
			(result: any) => {
				this.environmentList = Object.keys(result['environment']).map(
					type => {
						return result['environment'][type];
					}
				);
				this.statusList = Object.keys(result['status']).map(type => {
					return result['status'][type];
				});
				this.httpMethodList = Object.keys(result['httpMethod']).map(
					type => {
						return result['httpMethod'][type];
					}
				);
				this.authMethodList = Object.keys(
					result['authenticationMethod']
				).map(type => {
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
			err => console.log(err)
		);
	}

	/**
	 * TODO: check if valid form should pass even though expression is invalid,
	 * dontiveros: I will change it to be invalid because the form is not saved if the expression result is not valid.
	 * @param credentialForm
	 */
	isFormInvalid(credentialForm: NgForm): boolean {
		return (
			!credentialForm.form.valid ||
			this.credentialModel.provider.id === 0 ||
			!this.validExpressionResult.valid ||
			!this.validateRequiredFields(this.credentialModel)
		);
	}

	/**
	 * Validate required fields before saving model
	 * @param model - The model to be saved
	 */
	public validateRequiredFields(model: CredentialModel): boolean {
		let returnVal = true;
		this.requiredFields.forEach(field => {
			if (!model[field]) {
				returnVal = false;
				return false;
			} else if (
				typeof model[field] === 'string' &&
				!model[field].replace(/\s/g, '').length
			) {
				returnVal = false;
				return false;
			}
		});
		return returnVal;
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
		if (
			this.credentialModel.authMethod === this.authMethods.COOKIE ||
			this.credentialModel.authMethod === this.authMethods.HEADER ||
			this.credentialModel.authMethod === this.authMethods.BASIC_AUTH
		) {
			this.validateExpressionCheck().subscribe(result => {
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
	 * Create Save or Update the Credential from the model
	 */
	private saveCredential(): void {
		this.credentialService.saveCredential(this.credentialModel).subscribe(
			(result: any) => {
				if (result && result.id) {
					super.onAcceptSuccess(result);
				}
			},
			err => console.log(err)
		);
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
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.credentialModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.credentialModel);
					this.modalType = this.actionTypes.VIEW;
					this.setTitle(this.getModalTitle(this.modalType));
				} else {
					this.onCancelClose();
				}
			});
		} else {
			if (!this.data.openFromList) {
				this.modalType = this.actionTypes.VIEW;
				this.setTitle(this.getModalTitle(this.modalType));
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditCredential(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteCredential(): void {
		this.dialogService.confirm(
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'
			),
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION'
			)
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
	}

	protected verifyCode(operationStatusModel: OperationStatusModel): void {
		this.credentialService
			.validateAuthentication(this.credentialModel.id)
			.subscribe(
				(result: any) => {
					if (!result) {
						operationStatusModel.state = this.checkActionModel.INVALID;
					} else {
						operationStatusModel.state = this.checkActionModel.VALID;
					}
				},
				err => console.log(err)
			);
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
			this.credentialService
				.validateExpressionCheck(
					this.credentialModel.validationExpression
				)
				.subscribe(
					(result: any) => {
						this.validExpressionResult = result;
						observer.next(result);
					},
					err => console.log(err)
				);
		});
	}

	/**
	 * Keep Data Signature Clean even when there are so many values incoming
	 * @param property
	 * @param value
	 */
	private modifySignatureByProperty(property: any): void {
		this.dataSignature = ObjectUtils.modifySignatureByProperty(
			this.dataSignature,
			property,
			this.credentialModel[property]
		);
	}

	protected isCheckSyntaxDisabled(): boolean {
		return this.operationStatusModel.state === CHECK_ACTION.VALID;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		// Every time we change the title, it means we switched to View, Edit or Create
		setTimeout(() => {
			// This ensure the UI has loaded since Kendo can change the signature of an object
			this.dataSignature = JSON.stringify(this.credentialModel);
		}, 800);

		if (modalType === ActionType.CREATE) {
			return 'Credential Create';
		}
		return modalType === ActionType.EDIT
			? 'Credential Edit'
			: 'Credential Detail';
	}
}
