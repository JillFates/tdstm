import { Component, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';
import {DataScriptModel} from '../../model/data-script.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

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
	private filename: string;
	private checking = false;
	private saving = false;
	private testing = false;
	private scriptErrorLog: string;
	private consoleSettings: any = {
		top : '30px',
		left : '30px',
		height: '200px',
		width: '500px',
		log: null
	};

	ngAfterViewInit(): void {
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

	constructor(
		private dialogService: UIDialogService,
		private dataScriptModel: DataScriptModel,
		private dataIngestionService: DataIngestionService,
		private notifierService: NotifierService,
		private promptService: UIPromptService) {
		super('#etlBuilder');
		this.script =  this.dataScriptModel.etlSourceCode ? this.dataScriptModel.etlSourceCode.slice(0) : '';
	}

	/**
	 * On EscKey Pressed close the dialog.
	 */
	onEscKeyPressed(): void {
		this.cancelCloseDialog();
	}

	protected cancelCloseDialog(): void {
		if (this.isScriptDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.dismiss();
					}
				});
		} else {
			this.dismiss();
		}

	}

	private onSave(): void {
		this.saving = true;
		this.dataIngestionService.saveScript(this.dataScriptModel.id, this.script).subscribe(
			(result) => {
				this.saving = false;
				if (result) {
					this.dataScriptModel.etlSourceCode = this.script.slice(0);
					this.notifierService.broadcast({
						name: AlertType.SUCCESS,
						message: 'Script saved successfully.'
					});
				} else {
					this.notifierService.broadcast({
						name: AlertType.DANGER,
						message: 'Script not saved.'
					});
				}
			},
			(err) => console.log(err));
	}

	private isScriptDirty(): boolean {
		if (!this.dataScriptModel.etlSourceCode) {
			return this.script.length > 0;
		}
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
			.then(() => {
				// by the moment is hardcoded, this will be retrieved by the load sample data process..
				this.filename = 'service_now_applications.csv';
			})
			.catch((err) => {
			console.log('SampleDataDialog closed');
			if (err) {
				console.log(err);
			}
		});
	}

	/**
	 * On View Console button open the console dialog.
	 */
	protected onViewConsole(): void {
		this.dialogService.extra(DataScriptConsoleComponent, [{provide: 'consoleSettings', useValue: this.consoleSettings}], false, true)
			.then((result) => {/* on ok */} )
			.catch((result) => {/* on close/cancel */});
	}

	/**
	 * On Test Script button.
	 */
	private onTestScript(): void {
		this.testing = true;
		this.dataIngestionService.testScript(this.script, this.filename).subscribe( result => {
			this.testing = false;
			this.consoleSettings.log = null;
			this.scriptErrorLog = null;
			this.consoleSettings.log = result.data.consoleLog;
			if (result.data.isValid) {
				this.notifierService.broadcast({
					name: AlertType.SUCCESS,
					message: 'Valid Script'
				});
			} else {
				this.scriptErrorLog = result.data.error;
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: 'Invalid Script'
				});
			}
		});
	}

	/**
	 * On Check Script Syntax button.
	 */
	private onCheckScriptSyntax(): void {
		this.checking = true;
		this.dataIngestionService.checkSyntax(this.script, this.filename).subscribe( result => {
			this.checking = false;
			this.consoleSettings.log = null;
			this.scriptErrorLog = null;
			if (result.data.validSyntax) {
				this.notifierService.broadcast({
					name: AlertType.SUCCESS,
					message: 'Valid Syntax'
				});
			} else {
				let errors = result.data.errors.map( error => {
					return `message: ${error.message} --> start line: ${error.startLine}, end line: ${error.endLine}, start column: ${error.startColumn}, endColumn: ${error.endColumn}, fatal: ${error.fatal}`;
				});
				this.scriptErrorLog = errors.join('\n');
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: 'Invalid Syntax'
				});
			}
		});
	}

}