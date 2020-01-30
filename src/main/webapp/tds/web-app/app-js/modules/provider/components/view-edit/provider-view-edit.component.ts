import {
	ElementRef,
	Component,
	OnInit,
	ViewChild,
	Input, Output, EventEmitter,
} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {
	UIDialogService,
} from '../../../../shared/services/ui-dialog.service';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {ProviderModel} from '../../model/provider.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ProviderService} from '../../service/provider.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
import {ProviderAssociatedModel} from '../../model/provider-associated.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {Permission} from '../../../../shared/model/permission.model';
import {Dialog, DialogButtonType} from 'tds-component-library';
import {NgForm} from '@angular/forms';

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
	@Input() buttons: any;
	@Output() successEvent: EventEmitter<any> = new EventEmitter<any>();

	@ViewChild('providerNameElement', {read: ElementRef, static: true}) providerNameElement: ElementRef;
	@ViewChild('providerForm', {read: NgForm, static: true}) providerForm: NgForm;
	@ViewChild('providerContainer', {static: false}) providerContainer: ElementRef;

	public providerModel: ProviderModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	private dataSignature: string;
	protected isUnique = true;
	private providerName = new Subject<String>();

	constructor(
		public promptService: UIPromptService,
		private dialogService: UIDialogService,
		private prompt: UIPromptService,
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
						this.onCancelClose();
					}
				})
				.catch(error => console.log(error));
		} else {
			this.onCancelClose();
		}
	}

	public cancelEditDialog(): void {
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
						this.modalType = this.actionTypes.VIEW;
						this.modalTitle = this.getModalTitle(this.modalType);
					}
				})
				.catch(error => console.log(error));
		} else {
			this.modalType = this.actionTypes.VIEW;
			this.modalTitle = this.getModalTitle(this.modalType);
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditProvider(): void {
		this.modalType = this.actionTypes.EDIT;
		this.modalTitle = this.getModalTitle(this.modalType);
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteProvider(): void {
		this.providerService
			.deleteContext(this.providerModel.id)
			.subscribe((result: any) => {
				this.dialogService
					.extra(
						ProviderAssociatedComponent,
						[
							{
								provide: ProviderAssociatedModel,
								useValue: result,
							},
						],
						false,
						false
					)
					.then((toDelete: any) => {
						if (toDelete) {
							this.providerService
								.deleteProvider(this.providerModel.id)
								.subscribe(
									result => {
										this.onCancelClose(result);
									},
									err => console.log(err)
								);
						}
					})
					.catch(error => console.log('Closed'));
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
		super.onCancelClose();
	}
}
