import { Component, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';
import {DataScriptModel} from '../../model/data-script.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import { ScriptConsoleSettingsModel, ScriptTestResultModel, ScriptValidSyntaxResultModel } from '../../model/script-result.models';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {

	private collapsed = {
		code: true,
		sample: false
	};
	private script: string;
	private filename: string;
	private checking = false;
	private saving = false;
	private testing = false;
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();

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
		this.filename = 'service_now_applications.csv';
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
		this.dialogService.extra(DataScriptConsoleComponent, [{provide: ScriptConsoleSettingsModel, useValue: this.consoleSettings}], false, true)
			.then((result) => {/* on ok */} )
			.catch((result) => {/* on close/cancel */});
	}

	/**
	 * On Test Script button.
	 */
	private scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	private onTestScript(): void {
		this.testing = true;
		this.dataIngestionService.testScript(this.script, this.filename).subscribe( result => {
			this.testing = false;
			this.clearLogVariables();
			this.scriptTestResult = result.data;
			for (let domain of this.scriptTestResult.domains) {
				this.collapsed[domain] = false;
			}
			this.consoleSettings.scriptTestResult = this.scriptTestResult;
			if (this.scriptTestResult.isValid) {
				this.notifierService.broadcast({
					name: AlertType.SUCCESS,
					message: 'Valid Script'
				});
			} else {
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
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();
	private onCheckScriptSyntax(): void {
		this.checking = true;
		this.dataIngestionService.checkSyntax(this.script, this.filename).subscribe( result => {
			this.checking = false;
			this.clearLogVariables();
			this.scriptValidSyntaxResult = result.data;
			if (this.scriptValidSyntaxResult.validSyntax) {
				this.notifierService.broadcast({
					name: AlertType.SUCCESS,
					message: 'Valid Syntax'
				});
			} else {
				this.notifierService.broadcast({
					name: AlertType.DANGER,
					message: 'Invalid Syntax'
				});
			}
		});
	}

	private testHasErrors(): boolean {
		return !this.scriptTestResult.isValid && this.scriptTestResult.error && this.scriptTestResult.error.length > 0;
	}

	private syntaxHasErrors(): boolean {
		return !this.scriptValidSyntaxResult.validSyntax && this.scriptValidSyntaxResult.errors && this.scriptValidSyntaxResult.errors.length > 0;
	};

	private getSyntaxErrors(): string {
		let errors = this.scriptValidSyntaxResult.errors.map( error => {
			return `message: ${error.message} --> start line: ${error.startLine}, end line: ${error.endLine}, start column: ${error.startColumn}, endColumn: ${error.endColumn}, fatal: ${error.fatal}`;
		});
		return errors.join('\n');
	};

	private clearLogVariables(): void {
		this.scriptTestResult = new ScriptTestResultModel();
		this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		this.consoleSettings.scriptTestResult = new ScriptTestResultModel();
	}

}