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
	private operationStatus = {
		save: undefined,
		test: undefined,
		syntax: undefined,
	};
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();
	private scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();

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

	/**
	 * On Test Script button.
	 */
	private onTestScript(): void {
		this.operationStatus.test = 'progress';
		this.clearLogVariables('test');
		this.dataIngestionService.testScript(this.script, this.filename).subscribe( result => {
			this.scriptTestResult = result.data;
			this.operationStatus.test = this.scriptTestResult.isValid ? 'success' : 'fail';
			for (let domain of this.scriptTestResult.domains) {
				this.collapsed[domain] = false;
			}
			this.consoleSettings.scriptTestResult = this.scriptTestResult;
		});
	}

	/**
	 * On Check Script Syntax button.
	 */
	private onCheckScriptSyntax(): void {
		this.operationStatus.syntax = 'progress';
		this.clearLogVariables('syntax');
		this.dataIngestionService.checkSyntax(this.script, this.filename).subscribe( result => {
			this.scriptValidSyntaxResult = result.data;
			this.operationStatus.syntax = this.scriptValidSyntaxResult.validSyntax ? 'success' : 'fail';
		});
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
		this.operationStatus.save = 'progress';
		this.clearLogVariables();
		this.dataIngestionService.saveScript(this.dataScriptModel.id, this.script).subscribe(
			(result) => {
				if (result) {
					this.dataScriptModel.etlSourceCode = this.script.slice(0);
					this.operationStatus.save = 'success';
				} else {
					this.operationStatus.save = 'fail';
				}
			},
			(err) => {
				console.log(err);
			});
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

	private onScriptChange(event: { newValue: string, oldValue: string }) {
		// this.scriptChanged = true;
		// this.scriptTestResult = new ScriptTestResultModel();
		// this.consoleSettings.scriptTestResult = new ScriptTestResultModel();
		// this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		this.operationStatus.save = this.operationStatus.syntax = this.operationStatus.test = undefined;
	}

	protected toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	protected onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [])
			.then((filename) => {
				this.filename = filename;
			})
			.catch((err) => {
				console.log('SampleDataDialog error occurred..');
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

	private testHasErrors(): boolean {
		return !this.scriptTestResult.isValid && this.scriptTestResult.error && this.scriptTestResult.error.length > 0;
	}

	private syntaxHasErrors(): boolean {
		return !this.scriptValidSyntaxResult.validSyntax && this.scriptValidSyntaxResult.errors && this.scriptValidSyntaxResult.errors.length > 0;
	}

	private closeErrorsSection = false;
	private closeErrors(): void {
		this.closeErrorsSection = true;
	}

	private getSyntaxErrors(): string {
		let errors = this.scriptValidSyntaxResult.errors.map( error => {
			return `message: ${error.message} --> start line: ${error.startLine}, end line: ${error.endLine}, start column: ${error.startColumn}, endColumn: ${error.endColumn}, fatal: ${error.fatal}`;
		});
		return errors.join('\n');
	};

	private clearLogVariables(operation?: string): void {
		this.closeErrorsSection = false;
		if (operation === 'save') {
			this.scriptTestResult = new ScriptTestResultModel();
			this.consoleSettings.scriptTestResult = new ScriptTestResultModel();
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
		if (operation === 'test') {
			this.scriptTestResult = new ScriptTestResultModel();
			this.consoleSettings.scriptTestResult = new ScriptTestResultModel();
		}
		if (operation === 'syntax') {
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
	}

}