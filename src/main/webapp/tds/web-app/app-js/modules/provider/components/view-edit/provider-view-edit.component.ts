import {ElementRef, Component, OnInit, ViewChild, HostListener, Inject} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {ProviderModel} from '../../model/provider.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {KEYSTROKE} from '../../../../shared/model/constants';
import {ProviderService} from '../../service/provider.service';
import {ProviderAssociatedComponent} from '../provider-associated/provider-associated.component';
import {ProviderAssociatedModel} from '../../model/provider-associated.model';

@Component({
	selector: 'provider-view-edit',
	templateUrl: 'provider-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class ProviderViewEditComponent implements OnInit {

	@ViewChild('providerNameElement', {read: ElementRef}) providerNameElement: ElementRef;
	@ViewChild('providerContainer') providerContainer: ElementRef;
	public providerModel: ProviderModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	private dataSignature: string;
	private isUnique = true;
	private providerName = new Subject<String>();

	constructor(
		public originalModel: ProviderModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private dialogService: UIDialogService,
		private prompt: UIPromptService,
		private providerService: ProviderService) {

		this.providerModel = Object.assign({}, this.originalModel);
		this.modalTitle = this.getModalTitle(this.modalType);
		this.dataSignature = JSON.stringify(this.providerModel);
		this.providerName.next(this.providerModel.name);
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveProvider(): void {
		this.providerService.saveProvider(this.providerModel).subscribe(
			(result: any) => {
				this.activeDialog.close(result);
			},
			(err) => console.log(err));
	}

	ngOnInit(): void {
		this.providerName
			.debounceTime(800)        // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged()   // ignore if next search term is same as previous
			.subscribe(term => {
				if (term) {
					term = term.trim();
				}
				if (term && term !== '') {
					this.providerModel.name = this.providerModel.name.trim();
					this.providerService.validateUniquenessProviderByName(this.providerModel).subscribe(
						(result: any) => {
							this.isUnique = result.isUnique;
						},
						(err) => console.log(err));
				}
			});
		setTimeout(() => { // Delay issues on Auto Focus
			if (this.providerNameElement) {
				this.providerNameElement.nativeElement.focus();
			}
		}, 500);
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
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					} else {
						this.focusForm();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditProvider(): void {
		this.modalType = this.actionTypes.EDIT;
		this.modalTitle = this.getModalTitle(this.modalType);
		this.focusForm();
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteProvider(): void {
		this.providerService.deleteContext(this.providerModel.id).subscribe((result: any) => {
			this.dialogService.extra(ProviderAssociatedComponent,
				[{provide: ProviderAssociatedModel, useValue: result}],
				false, false)
				.then((toDelete: any) => {
					if (toDelete) {
						this.providerService.deleteProvider(this.providerModel.id).subscribe(
							(result) => {
								this.activeDialog.close(result);
							},
							(err) => console.log(err));
					}
				})
				.catch(error => console.log('Closed'));
		});
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

	private focusForm() {
		this.providerContainer.nativeElement.focus();
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.CREATE) {
			return 'Create Provider';
		}
		return (modalType === ActionType.EDIT ? 'Provider Edit' : 'Provider Detail' );
	}
}
