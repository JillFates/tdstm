import {Component, OnInit} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {DataScriptModel, ActionType, DataScriptMode} from '../../model/data-script.model';
import {ProviderModel} from '../../model/provider.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: 'data-script-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-view-edit/data-script-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class DataScriptViewEditComponent implements OnInit {

	public dataScriptModel: DataScriptModel;
	public providerList: ProviderModel[];
	public modalTitle: string;
	public dataScriptMode = DataScriptMode;
	public actionTypes = ActionType;
	private dataSignature: string;
	private isUnique = true;
	private datasourceName = new Subject<String>();

	constructor(
		public originalModel: DataScriptModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService) {

		this.dataScriptModel = Object.assign({}, this.originalModel);
		this.getProviders();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create Data Script' : (this.modalType === ActionType.EDIT ? 'Data Script Edit' : 'Data Script Detail' );
		this.dataSignature = JSON.stringify(this.dataScriptModel);
		this.datasourceName.next(this.dataScriptModel.name);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				this.providerList = result;
				if (this.modalType === ActionType.CREATE) {
					this.dataScriptModel.provider = this.providerList[0];
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveDataScript(): void {
		this.dataIngestionService.saveDataScript(this.dataScriptModel).subscribe(
			(result: any) => {
				this.activeDialog.close(result);
			},
			(err) => console.log(err));
	}

	ngOnInit(): void {
		this.datasourceName
			.debounceTime(800)        // wait 300ms after each keystroke before considering the term
			.distinctUntilChanged()   // ignore if next search term is same as previous
			.subscribe(term => {
				if (term && term !== '') {
					this.dataIngestionService.validateUniquenessDataScriptByName(this.dataScriptModel).subscribe(
						(result: any) => {
							this.isUnique = result.isUnique;
						},
						(err) => console.log(err));
				}
			});
	}

	protected onValidateUniqueness(): void {
		this.datasourceName.next(this.dataScriptModel.name);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.dataScriptModel);
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
	protected changeToEditDataScript(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteDataScript(): void {
		this.prompt.open('Confirmation Required', 'There are Ingestion Batches that have used this Datasource. Deleting this will not delete the batches but will no longer reference a Datasource. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteDataScript(this.dataScriptModel.id).subscribe(
						(result) => {
							this.activeDialog.close(result);
						},
						(err) => console.log(err));
				}
			});
	}
}