import { Component, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { APIActionModel} from '../../model/api-action.model';
import { ProviderModel } from '../../model/provider.model';
import { DataIngestionService } from '../../service/data-ingestion.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { DataScriptEtlBuilderComponent } from '../data-script-etl-builder/data-script-etl-builder.component';
import { ActionType } from '../../../../shared/model/data-list-grid.model';

@Component({
	selector: 'api-action-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/api-action-view-edit/api-action-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class APIActionViewEditComponent implements OnInit {

	@ViewChild('apiActionProvider', { read: DropDownListComponent }) apiActionProvider: DropDownListComponent;
	@ViewChild('apiActionAgent', { read: DropDownListComponent }) apiActionAgent: DropDownListComponent;
	public apiActionModel: APIActionModel;
	public providerList = new Array<ProviderModel>();
	public agentList = new Array<ProviderModel>();
	public modalTitle: string;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private isUnique = true;
	private datasourceName = new Subject<String>();

	constructor(
		public originalModel: APIActionModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService,
		private dialogService: UIDialogService) {

		this.apiActionModel = Object.assign({}, this.originalModel);
		this.getProviders();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create API Action' : (this.modalType === ActionType.EDIT ? 'API Action Edit' : 'API Action Detail');
		this.dataSignature = JSON.stringify(this.apiActionModel);
		this.datasourceName.next(this.apiActionModel.name);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.provider = this.providerList[0];
				}
				this.providerList.push(...result);
				this.dataSignature = JSON.stringify(this.apiActionModel);

			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		this.dataIngestionService.saveAPIAction(this.apiActionModel).subscribe(
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
					this.dataIngestionService.validateUniquenessAPIActionByName(this.apiActionModel).subscribe(
						(result: any) => {
							this.isUnique = result.isUnique;
						},
						(err) => console.log(err));
				}
			});
	}

	protected onValidateUniqueness(): void {
		this.datasourceName.next(this.apiActionModel.name);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.apiActionModel);
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
	protected changeToEditApiAction(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteApiAction(): void {
		this.prompt.open('Confirmation Required', 'There are Ingestion Batches that have used this Datasource. Deleting this will not delete the batches but will no longer reference a Datasource. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteDataScript(this.apiActionModel.id).subscribe(
						(result) => {
							this.activeDialog.close(result);
						},
						(err) => console.log(err));
				}
			});
	}

	protected onDataScriptDesigner(): void {
		this.dialogService.extra(DataScriptEtlBuilderComponent, [UIDialogService]).then(() => console.log('ok'), () => console.log('not ok'));
	}

}