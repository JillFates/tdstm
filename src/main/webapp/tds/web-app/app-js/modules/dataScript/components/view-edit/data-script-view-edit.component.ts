// Angular
import {
	Component,
	HostListener,
	OnInit,
	ViewChild,
	ElementRef, Input
} from '@angular/core';
// Component
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
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
import {Dialog, DialogButtonType} from 'tds-component-library';
// Service
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {
	UIActiveDialogService,
	UIDialogService,
} from '../../../../shared/services/ui-dialog.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DataScriptService} from '../../service/data-script.service';
// Other
import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/mergeMap';
import {NgForm} from '@angular/forms';

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
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataScriptService,
		private dialogService: UIDialogService,
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
					this.activeDialog.close(result);
				},
				err => err)
		;
	}

	protected onValidateUniqueness(): void {
		this.datasourceName.next(this.dataScriptModel.name);
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(
		event: KeyboardEvent
	) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
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

	private focusForm() {
		this.dataScriptContainer.nativeElement.focus();
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService
				.open(
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
					),
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
					),
					this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL')
				)
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					} else {
						this.focusForm();
					}
				})
				.catch(error => console.log(error));
		} else {
			if (this.etlScriptCode.updated) {
				this.activeDialog.close();
			} else {
				this.activeDialog.dismiss();
			}
		}
	}

	protected cancelEditDialog(): void {
		if (this.isDirty()) {
			this.promptService
				.open(
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
					),
					this.translatePipe.transform(
						'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
					),
					this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL')
				)
				.then(confirm => {
					if (confirm) {
						this.dataScriptModel = Object.assign({}, this.data.dataScriptModel);
						this.changeToViewDataScript();
					} else {
						this.focusForm();
					}
				})
				.catch(error => console.log(error));
		} else {
			this.changeToViewDataScript();
		}
	}

	protected changeToViewDataScript(): void {
		this.modalType = this.actionTypes.VIEW;
		this.modalTitle = 'ETL Script Detail';
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
	 * @param dataItem
	 */
	protected onDeleteDataScript(): void {
		this.dataIngestionService
			.validateDeleteScript(this.dataScriptModel.id)
			.subscribe(
				result => {
					if (result && result['canDelete']) {
						this.prompt
							.open(
								'Confirmation Required',
								'Do you want to proceed?',
								'Yes',
								'No'
							)
							.then(res => {
								if (res) {
									this.deleteDataScript();
								}
							});
					} else {
						this.prompt
							.open(
								'Confirmation Required',
								'There are Ingestion Batches that have used this DataScript. Deleting this will not delete the batches but will no longer reference a DataScript. Do you want to proceed?',
								'Yes',
								'No'
							)
							.then(res => {
								if (res) {
									this.deleteDataScript();
								}
							});
					}
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
					this.activeDialog.close(result);
				},
				err => console.log(err)
			);
	}

	/**
	 * Open the DataScript Designer
	 */
	protected onDataScriptDesigner(): void {
		this.dialogService
			.extra(
				DataScriptEtlBuilderComponent,
				[
					UIDialogService,
					{
						provide: DataScriptModel,
						useValue: this.dataScriptModel,
					},
				],
				false,
				false
			)
			.then(result => {
				if (result.updated) {
					this.etlScriptCode.updated = result.updated;
					this.etlScriptCode.code = result.newEtlScriptCode;
				}
			})
			.catch(error => console.log('Cancel datascript designer'));
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
