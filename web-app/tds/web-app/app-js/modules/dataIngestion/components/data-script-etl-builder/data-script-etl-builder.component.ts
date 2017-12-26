import { Component, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';
import {DataScriptModel} from '../../model/data-script.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {

	private collapsed = {
		code: true,
		sample: false,
		transformed: false
	};
	private script: string;
	private saving = false;

	ngAfterViewInit(): void {
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

	constructor(private dialogService: UIDialogService, private dataScriptModel: DataScriptModel, private dataIngestionService: DataIngestionService, private notifier: NotifierService) {
		super('#etlBuilder');
		this.script = this.dataScriptModel.etlSourceCode.slice(0);
	}

	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	private onSave(): void {
		this.saving = true;
		this.dataIngestionService.saveScript(this.dataScriptModel.id, this.script).subscribe(
			(result) => {
				this.saving = false;
				if (result) {
					this.dataScriptModel.etlSourceCode = this.script.slice(0);
					this.notifier.broadcast({
						name: AlertType.SUCCESS,
						message: 'Script saved successfully.'
					});
				} else {
					this.notifier.broadcast({
						name: AlertType.DANGER,
						message: 'Script not saved.'
					});
				}
			},
			(err) => console.log(err));
	}

	private isScriptDirty(): boolean {
		if (this.dataScriptModel.etlSourceCode !== this.script) {
			return true;
		} else {
			return false;
		}
	}

	private onCodeChange(event: { newValue: string, oldValue: string }) {
		// ...
	}

	protected toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	protected onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

	protected onViewConsole(): void {
		this.dialogService.extra(DataScriptConsoleComponent, [])
			.then(() => console.log('ok'))
			.catch(() => console.log('still ok'));
	}

}