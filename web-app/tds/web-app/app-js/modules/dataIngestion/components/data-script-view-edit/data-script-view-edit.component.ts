import { Component, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';
import { UIActiveDialogService, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptModel, ActionType, DataScriptMode } from '../../model/data-script.model';
import { ProviderModel } from '../../model/provider.model';
import { DataIngestionService } from '../../service/data-ingestion.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { DataScriptEtlBuilderComponent } from '../data-script-etl-builder/data-script-etl-builder.component';

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

	@ViewChild('dataScriptProvider', { read: DropDownListComponent }) dataScriptProvider: DropDownListComponent;
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
		code: null
	};

	constructor(
		public originalModel: DataScriptModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService,
		private dialogService: UIDialogService) {

		this.dataScriptModel = Object.assign({}, this.originalModel);
		this.getProviders();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create DataScript' : (this.modalType === ActionType.EDIT ? 'DataScript Edit' : 'DataScript Detail');
		// ignore etl script from this context
		let copy = {...this.dataScriptModel};
		this.etlScriptCode.code = copy.etlSourceCode;
		delete copy.etlSourceCode;
		this.dataSignature = JSON.stringify(copy);
		this.datasourceName.next(this.dataScriptModel.name);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({ id: 0, name: 'Select...' });
					this.dataScriptModel.provider = this.providerList[0];
				}
				this.providerList.push(...result);
				let copy = {...this.dataScriptModel};
				this.etlScriptCode.code = copy.etlSourceCode;
				delete copy.etlSourceCode;
				this.dataSignature = JSON.stringify(copy);
				setTimeout(() => { // Delay issues on Auto Focus
					if (this.dataScriptProvider) {
						this.dataScriptProvider.focus();
					}
				}, 500);

			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveDataScript(): void {
		this.dataScriptModel.etlSourceCode = this.etlScriptCode.code;
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
			.filter((term) => term && term.trim())
			.subscribe(term => {
					this.dataIngestionService.validateUniquenessDataScriptByName(this.dataScriptModel).subscribe(
						(result: any) => {
							this.isUnique = result.isUnique;
						},
						(err) => console.log(err));
			});
	}

	protected onValidateUniqueness(): void {
		this.datasourceName.next(this.dataScriptModel.name);
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
			if (this.etlScriptCode.updated) {
				this.activeDialog.close();
			} else {
				this.activeDialog.dismiss();
			}
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditDataScript(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteDataScript(): void {
		this.dataIngestionService.validateDeleteScript(this.dataScriptModel.id).subscribe(
			(result) => {
				if (result && result['canDelete']) {
					this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
						.then((res) => {
							if (res) {
								this.deleteDataScript();
							}
						});
				} else {
					this.prompt.open('Confirmation Required', 'There are Ingestion Batches that have used this DataScript. Deleting this will not delete the batches but will no longer reference a DataScript. Do you want to proceed?', 'Yes', 'No')
						.then((res) => {
							if (res) {
								this.deleteDataScript();
							}
						});
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Execute the Service to delete the DataScript
	 */
	private deleteDataScript(): void {
		this.dataIngestionService.deleteDataScript(this.dataScriptModel.id).subscribe(
			(result) => {
				this.activeDialog.close(result);
			},
			(err) => console.log(err));
	}

	/**
	 * Open the DataScript Designer
	 */
	protected onDataScriptDesigner(): void {
		this.dialogService.extra(DataScriptEtlBuilderComponent,
			[UIDialogService,
				{provide: DataScriptModel, useValue: this.dataScriptModel}])
			.then((result) => {
				if (result.updated) {
					this.etlScriptCode.updated = result.updated;
					this.etlScriptCode.code = result.newEtlScriptCode;
				}
			});
	}
}
