// Angular
import {
	ElementRef,
	Component,
	OnInit,
	ViewChild,
	Input, Output, EventEmitter, ComponentFactoryResolver, QueryList, ViewChildren,
} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {Permission} from '../../../../shared/model/permission.model';
// Component
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Other
import {Subject} from 'rxjs/Subject';
import {ManufacturerModel} from '../../model/manufacturer.model';
import {ManufacturerService} from '../../service/manufacturer.service';

@Component({
	selector: 'manufacturer-view-edit',
	templateUrl: 'manufacturer-view-edit.component.html',
	styles: [
			`
			.has-error,
			.has-error:focus {
				border: 1px #f00 solid;
			}
		`,
	],
})
export class ManufacturerViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('manufacturerForm', {read: NgForm, static: true}) manufacturerForm: NgForm;

	public manufacturerModel: ManufacturerModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	private dataSignature: string;
	protected isUnique = true;
	protected isAliasUnique = true;
	protected uniqueAliases: string[] = [];
	protected currentIndex: number;
	private manufacturerName = new Subject<String>();
	private manufacturerAliasName = new Subject<String>();
	public alias = [];
	public aliasPristineList = [];
	public aliasControls = [];
	public aliasDeleted = [];
	public aliasUpdated = [];
	public aliasAdded = [];
	public displayedAliasErrorSpans = [];
	@ViewChildren('aliasSpan') aliasSpanElements: QueryList<any>;
	protected hasOnlyUniqueAlias = true;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private manufacturerService: ManufacturerService,
		private permissionService: PermissionService
	) {
		super();
	}

	/**
	 * Create Edit a Manufacturer
	 */
	protected onSaveManufacturer(): void {
		this.manufacturerModel.alias = this.alias.join(',');
		const aliasAddedMap = this.aliasAdded.map(i => i.name);
		this.manufacturerService.saveManufacturer(this.manufacturerModel, aliasAddedMap.toString(), this.aliasDeleted.toString(), this.aliasUpdated).subscribe(
			(result: any) => {
				this.onAcceptSuccess(result);
			},
			err => console.log(err)
		);
	}

	ngOnInit(): void {
		this.manufacturerModel = Object.assign({}, this.data.manufacturerModel);
		this.modalType = this.data.actionType;
		this.modalTitle = this.getModalTitle(this.modalType);
		this.dataSignature = JSON.stringify(this.manufacturerModel);
		this.manufacturerName.next(this.manufacturerModel.name);
		this.alias = (this.manufacturerModel.alias !== '') ? this.manufacturerModel.alias.split(',') : [];
		this.aliasPristineList = (this.modalType === this.actionTypes.EDIT) ? (this.manufacturerModel.aliases) ? this.manufacturerModel.aliases : [] : [];
		this.aliasControls = (this.modalType === this.actionTypes.EDIT) ? (this.manufacturerModel.aliases) ? this.manufacturerModel.aliases : [] : [];

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			disabled: () => !this.permissionService.hasPermission(Permission.ProviderUpdate),
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditProvider.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.manufacturerForm.form.valid || !this.isUnique || this.isEmptyValue() || !this.manufacturerForm.form.dirty || !this.hasOnlyUniqueAlias,
			type: DialogButtonType.ACTION,
			action: this.onSaveManufacturer.bind(this)
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

		this.manufacturerName
			.debounceTime(800) // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged() // ignore if next search term is same as previous
			.subscribe(term => {
				if (term) {
					term = term.trim();
				}
				if (term && term !== '') {
					this.manufacturerModel.name = this.manufacturerModel.name.trim();
					this.manufacturerService
						.validateUniquenessManufacturerByName(this.manufacturerModel)
						.subscribe(
							(result: any) => {
								this.isUnique = result.isUnique;
							},
							err => console.log(err)
						);
				}
			});

		this.manufacturerAliasName
			.debounceTime(1000) // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged() // ignore if next search term is same as previous
			.subscribe(term => {
				if (term) {
					term = term.trim();
				}
				if (term && term !== '') {
					this.manufacturerModel.name = this.manufacturerModel.name.trim();
					this.manufacturerService
						.validateUniquenessManufacturerAliasByName(this.manufacturerModel.id, this.manufacturerModel.name, term.toString())
						.subscribe(
							(result: any) => {
								if (result.isValid) {
									this.aliasSpanElements.toArray()[this.currentIndex].nativeElement.style.cssText = 'display: none;';
									this.uniqueAliases.push(term.toString());
									this.displayedAliasErrorSpans.splice(this.currentIndex, 1);
								} else {
									this.aliasSpanElements.toArray()[this.currentIndex].nativeElement.style.cssText = 'color: red; font-weight: bold; display: block;';
									this.displayedAliasErrorSpans.push(this.currentIndex);
								}
								this.hasOnlyUniqueAlias = (this.displayedAliasErrorSpans.length === 0);
							},
							err => console.log(err)
						);
				}
			});

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
		});
	}

	protected onValidateUniqueness(): void {
		this.manufacturerName.next(this.manufacturerModel.name);
	}

	protected onValidateAliasUniqueness(name: string, index: number): void {
		this.currentIndex = index;
		this.manufacturerAliasName.next(name);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.manufacturerModel);
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

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.manufacturerModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.manufacturerModel);
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
	protected changeToEditProvider(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Verify if the Name is Empty
	 * @returns {boolean}
	 */
	protected isEmptyValue(): boolean {
		let term = '';
		if (this.manufacturerModel.name) {
			term = this.manufacturerModel.name.trim();
		}
		return term === '';
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.CREATE) {
			return 'Manufacturer Create';
		}
		return modalType === ActionType.EDIT
			? 'Manufacturer Edit'
			: 'Manufacturer Detail';
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
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
}
