import {Component, Inject} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {DataScriptModel, ModalType, ModeType} from '../../model/data-script.model';
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
export class DataScriptViewEditComponent {

	public providerList: ProviderModel[];
	public modalTitle: string;
	public modeType = ModeType;
	private dataSignature: string;

	constructor(
		public dataScriptModel: DataScriptModel,
		public modalType: ModalType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private dataIngestionService: DataIngestionService) {
		this.getProviders();
		this.modalTitle = (this.modalType === ModalType.CREATE) ? 'Create' : 'Edit';
		this.dataSignature = JSON.stringify(this.dataScriptModel);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				this.providerList = result;
				if (this.modalType === ModalType.CREATE) {
					this.dataScriptModel.provider = this.providerList[0];
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onCreateDataScript(): void {
		// this.dataIngestionService.saveDataScript();
		console.log(this.dataScriptModel);
	}

	/**
	 * Pass the number of selected rows
	 * @param event
	 */
	protected onSelectView(selectedView: any): void {
		this.dataScriptModel.view = selectedView;
	}

	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.dataScriptModel);
	}

	cancelCloseDialog(): void {
		if(this.isDirty()) {
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
}