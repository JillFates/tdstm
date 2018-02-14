import {ElementRef, Component, OnInit, ViewChild} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ActionType} from '../../model/data-script.model';
import {ProviderModel} from '../../model/provider.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: 'provider-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/provider-view-edit/provider-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class ProviderViewEditComponent implements OnInit {

	@ViewChild('providerNameElement', {read: ElementRef}) providerNameElement: ElementRef;
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
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService) {

		this.providerModel = Object.assign({}, this.originalModel);
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create Provider' : (this.modalType === ActionType.EDIT ? 'Provider Edit' : 'Provider Detail' );
		this.dataSignature = JSON.stringify(this.providerModel);
		this.providerName.next(this.providerModel.name);
	}

	/**
	 * Create Edit a Provider
	 */
	protected onSaveProvider(): void {
		this.dataIngestionService.saveProvider(this.providerModel).subscribe(
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
					this.dataIngestionService.validateUniquenessProviderByName(this.providerModel).subscribe(
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
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditProvider(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteProvider(): void {
		this.prompt.open('Confirmation Required', 'There are associated Datasources. Deleting this will not delete historical imports. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteProvider(this.providerModel.id).subscribe(
						(result) => {
							this.activeDialog.close(result);
						},
						(err) => console.log(err));
				}
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
}
