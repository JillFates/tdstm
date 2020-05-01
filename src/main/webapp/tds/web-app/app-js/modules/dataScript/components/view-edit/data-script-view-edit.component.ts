// Angular
import {
	Component,
	OnInit,
	ViewChild,
	ElementRef, Input, ComponentFactoryResolver
} from '@angular/core';
import {NgForm} from '@angular/forms';
// Component
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {DataScriptEtlBuilderComponent} from '../etl-builder/data-script-etl-builder.component';
// Model
import {
	DataScriptModel,
	ActionType,
	DataScriptMode,
} from '../../model/data-script.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {Permission} from '../../../../shared/model/permission.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Service
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DataScriptService} from '../../service/data-script.service';
// Other
import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/mergeMap';

const DEBOUNCE_MILLISECONDS = 800;

@Component({
	selector: 'data-script-view-edit',
	templateUrl: 'data-script-view-edit.component.html',
	styles: [
			`
            .has-error,
            .has-error:focus {
                border: 1px #f00 solid;
            }
		`,
	],
})
export class DataScriptViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('etlScriptCreateName', {static: false}) etlScriptCreateName: ElementRef;
	@ViewChild('dataScriptForm', {read: NgForm, static: true}) dataScriptForm: NgForm;

	@ViewChild('dataScriptProvider', {
		read: DropDownListComponent,
		static: true,
	})
	dataScriptProvider: DropDownListComponent;
	@ViewChild('dataScriptContainer', {static: false})
	dataScriptContainer: ElementRef;
	public dataScriptModel: DataScriptModel;
	public providerList = new Array<ProviderModel>();
	public modalTitle: string;
	public dataScriptMode = DataScriptMode;
	public actionTypes = ActionType;
	private dataSignature: string;
	private isUnique = true;
	private datasourceName = new Subject<String>();
	private etlScriptCode = {
		updated: false,
		code: null,
	};
	public modalType = ActionType.VIEW;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private dataIngestionService: DataScriptService,
		private translatePipe: TranslatePipe,
		private permissionService: PermissionService
	) {
		super();
	}

	ngOnInit(): void {
		this.dataScriptModel = Object.assign({}, this.data.dataScriptModel);
		this.modalType = this.data.actionType;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			disabled: () => !this.permissionService.hasPermission(Permission.ProviderUpdate),
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditDataScript.bind(this)
		});

		this.buttons.push({
			name: 'etlScript',
			icon: 'code',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			type: DialogButtonType.ACTION,
			action: this.onDataScriptDesigner.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.dataScriptForm.form.valid || !this.isUnique || (this.dataScriptModel.provider && !this.dataScriptModel.provider.id) || !this.isDirty(),
			type: DialogButtonType.ACTION,
			action: this.onSaveDataScript.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.modalType !== this.actionTypes.CREATE,
			disabled: () => !this.isDeleteAvailable,
			type: DialogButtonType.ACTION,
			action: this.onDeleteDataScript.bind(this)
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
		this.modalTitle =
			this.modalType === ActionType.CREATE
				? 'ETL Script Create'
				: this.modalType === ActionType.EDIT
				? 'ETL Script Edit'
				: 'ETL Script Detail';
		// ignore etl script from this context
		let copy = {...this.dataScriptModel};
		this.etlScriptCode.code = copy.etlSourceCode;
		delete copy.etlSourceCode;
		this.dataSignature = JSON.stringify(copy);
		this.datasourceName.next(this.dataScriptModel.name);

		const notEmptyViewName$: Observable<String> = this.datasourceName
			.debounceTime(DEBOUNCE_MILLISECONDS)
			.distinctUntilChanged()
			.filter((name: string) => Boolean(name && name.trim()));

		notEmptyViewName$
			.flatMap(() =>
				this.dataIngestionService.validateUniquenessDataScriptByName(
					this.dataScriptModel
				)
			)
			.subscribe(
				(isUnique: boolean) => (this.isUnique = isUnique),
				(error: Error) => console.log(error.message)
			);

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
			this.onSetUpFocus(this.etlScriptCreateName);
		});
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.dataScriptModel.provider = {
						id: 0,
						name: this.translatePipe.transform(
							'GLOBAL.SELECT_PLACEHOLDER'
						),
					};
				}
				this.providerList.push(...result);
				let copy = {...this.dataScriptModel};
				this.etlScriptCode.code = copy.etlSourceCode;
				delete copy.etlSourceCode;
				this.dataSignature = JSON.stringify(copy);
				setTimeout(() => {
					// Delay issues on Auto Focus
					if (this.dataScriptProvider) {
						this.dataScriptProvider.focus();
					}
				}, 500);
			},
			err => console.log(err)
		);
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveDataScript(): void {
		this.dataScriptModel.etlSourceCode = this.etlScriptCode.code;
		this.dataIngestionService
			.saveDataScript(this.dataScriptModel)
			.subscribe(
				(result: any) => {
					this.onAcceptSuccess(result);
				},
				err => err)
		;
	}

	/**
	 * Validates if action name is unique.
	 */
	protected onValidateUniqueness(): void {
		this.datasourceName.next(this.dataScriptModel.name);
	}

	/**
	 * Validates and returns true if action name input is valid, false otherwise.
	 */
	isNameInvalid(): boolean {
		return !this.dataScriptForm.form.valid && this.dataScriptForm.touched
			&& (!this.dataScriptModel.name || this.dataScriptModel.name.length <= 0);
	}

	/**
	 * Verify the Object has not changed
	 * - Ignore etl-script changes
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		let copy = {...this.dataScriptModel};
		this.etlScriptCode.code = copy.etlSourceCode;
		delete copy.etlSourceCode;
		return this.dataSignature !== JSON.stringify(copy);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	protected cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					this.dataScriptModel = Object.assign({}, this.data.dataScriptModel);
					this.changeToViewDataScript();
				} else if (result.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
					this.onCancelClose();
				}
			});
		} else {
			if (!this.data.openFromList) {
				this.changeToViewDataScript();
			} else {
				this.onCancelClose();
			}
		}
	}

	protected changeToViewDataScript(): void {
		this.modalType = this.actionTypes.VIEW;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditDataScript(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Delete the selected DataScript
	 */
	protected onDeleteDataScript(): void {
		this.dataIngestionService
			.validateDeleteScript(this.dataScriptModel.id)
			.subscribe(
				result => {
					let confirmMessage = '';
					if (result && result['canDelete']) {
						confirmMessage = this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION');
					} else {
						confirmMessage = 'There are Ingestion Batches that have used this DataScript. Deleting this will not delete the batches but will no longer reference a DataScript. Do you want to proceed?';
					}
					this.dialogService.confirm(
						this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'),
						confirmMessage
					)
						.subscribe((data: any) => {
							if (data.confirm === DialogConfirmAction.CONFIRM) {
								this.deleteDataScript();
							}
						});
				},
				err => console.log(err)
			);
	}

	/**
	 * Execute the Service to delete the DataScript
	 */
	private deleteDataScript(): void {
		this.dataIngestionService
			.deleteDataScript(this.dataScriptModel.id)
			.subscribe(
				result => {
					this.onCancelClose(result);
				},
				err => console.log(err)
			);
	}

	/**
	 * Open the DataScript Designer
	 */
	protected onDataScriptDesigner(): void {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: DataScriptEtlBuilderComponent,
			data: {
				dataScriptModel: this.dataScriptModel
			},
			modalConfiguration: {
				title: 'ETL Script',
				draggable: true,
				resizable: true,
				modalSize: ModalSize.XL
			}
		}).subscribe((result: any) => {
			if (result.updated) {
				this.etlScriptCode.updated = result.updated;
				this.etlScriptCode.code = result.newEtlScriptCode;
			}
		});
	}

	protected isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderDelete);
	}

	protected isUpdateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProviderUpdate);
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
			let copy = {...this.dataScriptModel};
			this.etlScriptCode.code = copy.etlSourceCode;
			delete copy.etlSourceCode;
			this.dataSignature = JSON.stringify(copy);
		}, 800);
		if (modalType === ActionType.CREATE) {
			return 'ETL Script Create';
		}
		return modalType === ActionType.EDIT
			? 'ETL Script Edit'
			: 'ETL Script Detail';
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
