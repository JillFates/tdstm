// Angular
import {
	ElementRef,
	Component,
	OnInit,
	ViewChild,
	Input, Output, EventEmitter, ComponentFactoryResolver,
} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {ActionType} from '../../../dataScript/model/data-script.model';
import {ProviderModel} from '../../model/provider.model';
import {Permission} from '../../../../shared/model/permission.model';
// Component
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
// Service
import {ProviderService} from '../../service/provider.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
// Other
import {Subject} from 'rxjs/Subject';

@Component({
	selector: 'provider-view-edit',
	templateUrl: 'provider-view-edit.component.html',
	styles: [
			`
            .has-error,
            .has-error:focus {
                border: 1px #f00 solid;
            }
		`,
	],
})
export class ProviderViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('providerForm', {read: NgForm, static: true}) providerForm: NgForm;
	@ViewChild('providerNameElement', {static: false}) providerNameElement: ElementRef;

	public providerModel: ProviderModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	private dataSignature: string;
	protected isUnique = true;
	private providerName = new Subject<String>();

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private providerService: ProviderService,
		private permissionService: PermissionService
	) {
		super();
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveProvider(): void {
		this.providerService.saveProvider(this.providerModel).subscribe(
			(result: any) => {
				this.onAcceptSuccess(result);
			},
			err => console.log(err)
		);
	}

	ngOnInit(): void {
		this.providerModel = Object.assign({}, this.data.providerModel);
		this.modalType = this.data.actionType;
		this.modalTitle = this.getModalTitle(this.modalType);
		this.dataSignature = JSON.stringify(this.providerModel);
		this.providerName.next(this.providerModel.name);

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
			disabled: () => !this.providerForm.form.valid || !this.isUnique || this.isEmptyValue() || !this.providerForm.form.dirty,
			type: DialogButtonType.ACTION,
			action: this.onSaveProvider.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.modalType !== this.actionTypes.CREATE,
			disabled: () => !this.permissionService.hasPermission(Permission.ProviderDelete),
			type: DialogButtonType.ACTION,
			action: this.onDeleteProvider.bind(this)
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

		this.providerName
			.debounceTime(800) // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged() // ignore if next search term is same as previous
			.subscribe(term => {
				if (term) {
					term = term.trim();
				}
				if (term && term !== '') {
					this.providerModel.name = this.providerModel.name.trim();
					this.providerService
						.validateUniquenessProviderByName(this.providerModel)
						.subscribe(
							(result: any) => {
								this.isUnique = result.isUnique;
							},
							err => console.log(err)
						);
				}
			});

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
			console.log('element:', this.providerNameElement);
			super.onSetUpFocus(this.providerNameElement);
		});
	}

	protected onValidateUniqueness(): void {
		this.providerName.next(this.providerModel.name);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.providerModel);
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
					this.providerModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.providerModel);
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

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditProvider(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteProvider(): void {
		this.providerService
			.deleteContext(this.providerModel.id)
			.subscribe((result: any) => {
				this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: ProviderAssociatedComponent,
					data: {
						providerAssociatedModel: result,
					},
					modalConfiguration: {
						title: 'Confirmation Required',
						draggable: true,
						modalSize: ModalSize.MD
					}
				}).subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						this.providerService
							.deleteProvider(this.providerModel.id)
							.subscribe(
								result => {
									this.onCancelClose(result);
								},
								err => console.log(err)
							);
					}
				});
			});
	}

	/**
	 * Verify if the Name is Empty
	 * @returns {boolean}
	 */
	protected isEmptyValue(): boolean {
		let term = '';
		if (this.providerModel.name) {
			term = this.providerModel.name.trim();
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
			return 'Provider Create';
		}
		return modalType === ActionType.EDIT
			? 'Provider Edit'
			: 'Provider Detail';
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
