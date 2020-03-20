import {Component, ComponentFactoryResolver, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
import {NgForm} from '@angular/forms';
import {ModelModel} from '../model/model.model';
import {ActionType} from '../../dataScript/model/data-script.model';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../shared/services/permission.service';
import {ModelService} from '../service/model.service';
import {Permission} from '../../../shared/model/permission.model';

@Component({
	selector: 'model-view-edit',
	templateUrl: 'model-view-edit.component.html'
})

export class ModelViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('modelForm', {read: NgForm, static: true}) modelForm: NgForm;

	public modelModel: ModelModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	public manufacturerList;
	public assetTypeList;
	public sourceTDS = false;
	public modelConnectors: any[] = [];
	public modelAkas: any[] = [];
	public powerTypes = ['Amp', 'Watts'];
	public modelAkasDisplay: string;
	public manufacturerName: string;
	public usizeList: number[] = [];
	public userList: any[] = [];
	// AKA
	public aliasControls = [];
	public aliasDeleted = [];
	public aliasUpdated = [];
	public aliasAdded = [];
	public displayedAliasErrorSpans = [];
	@ViewChildren('aliasSpan') aliasSpanElements: QueryList<any>;
	protected hasOnlyUniqueAlias = true;

	private dataSignature: string;
	protected isUnique = true;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private modelService: ModelService,
		private permissionService: PermissionService
	) {
		super();
	}

	ngOnInit(): void {
		this.modelModel = Object.assign({}, this.data.modelModel);
		this.manufacturerList = this.data.manufacturerList;
		this.modalType = this.data.actionType;
		this.modalTitle = this.getModalTitle(this.modalType);

		this.getModelDetails();

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			disabled: () => !this.permissionService.hasPermission(Permission.ModelEdit),
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditModel.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.modelForm.form.valid || !this.isUnique || this.isEmptyValue() || !this.modelForm.form.dirty,
			type: DialogButtonType.ACTION,
			action: this.onSaveModel.bind(this)
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
	}

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.modelModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.modelModel);
					this.modalType = this.actionTypes.VIEW;
					this.setTitle(this.getModalTitle(this.modalType));
				} else if (result.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
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

	private getModelDetails(): void {
		this.modelService.getModelDetails(this.modelModel.id)
			.subscribe((response: any) => {
				console.log(`Model Detail response ${response}`);
				this.modelModel = response.data.modelInstance;
				this.modelConnectors = response.data.modelConnectors;
				this.modelAkas = response.data.modelAkas;
				this.modelAkasDisplay = (this.modelAkas.length > 0) ? this.modelAkas.join(',') : '';
				this.sourceTDS = this.modelModel.sourceTDS === 1;
				this.manufacturerName = this.manufacturerList.find(m => m.id === this.modelModel.manufacturer['id']).name;
				this.usizeList = response.data.usizeList;
				this.assetTypeList = response.data.assetTypes;
				this.aliasControls = (this.modalType === this.actionTypes.EDIT) ? (this.modelAkas) ? this.modelAkas : [] : [];
				this.dataSignature = JSON.stringify(this.modelModel);
				this.userList = response.data.userList;
			});
	}

	/**
	 * Add alias to collection
	 */
	public addAlias(): void {
		this.aliasControls.push('');
	}

	/**
	 * Remove alias from collection
	 */
	public removeAlias(index: number, itemId: number): void {
		this.modelForm.form.markAsDirty();
		this.aliasDeleted.push(itemId);
		this.aliasControls.splice(index, 1);

		this.aliasSpanElements.toArray().splice(index, 1);
		this.displayedAliasErrorSpans.splice(index, 1);
		this.hasOnlyUniqueAlias = (this.displayedAliasErrorSpans.length === 0);
	}

	/**
	 * Add alias value to collection
	 */
	public focusOutAlias(event: any, item: any, index: number): void {
		this.modelForm.form.markAsDirty();
		if (item) {
			if (event.target.value !== item.name) {
				this.aliasUpdated.push({id: item.id, name: event.target.value});
			} else {
				const arrayItem = this.aliasAdded.find(i => i === event.target.value);
				if (!arrayItem) {
					this.aliasAdded.push(event.target.value);
				}
			}
		} else {
			const alreadyAdded = this.aliasAdded.find(i => i.index === index);
			if (alreadyAdded) {
				this.aliasAdded[index].name = event.target.value;
			} else {
				this.aliasAdded.push({index, name: event.target.value});
			}
		}
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.CREATE) {
			return 'Model Create';
		}
		return modalType === ActionType.EDIT
			? 'Model Edit'
			: 'Model Detail';
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditModel(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Create Edit a Manufacturer
	 */
	protected onSaveModel(): void {
		// this.modelModel.alias = this.alias.join(',');
		// const aliasAddedMap = this.aliasAdded.map(i => i.name);
		this.modelService.saveModel(this.modelModel)
			.subscribe(
			(result: any) => {
				this.onAcceptSuccess(result);
			},
			err => console.log(err)
		);
	}

	/**
	 * Verify if the Name is Empty
	 * @returns {boolean}
	 */
	protected isEmptyValue(): boolean {
		let term = '';
		if (this.modelModel.modelName) {
			term = this.modelModel.modelName.trim();
		}
		return term === '';
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.modelModel);
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

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}