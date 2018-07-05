import {Component, AfterViewInit, ViewChild, ElementRef} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/finally';

import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';
import {DataScriptModel, SampleDataModel} from '../../model/data-script.model';
import {
	DataIngestionService, PROGRESSBAR_COMPLETED_STATUS, PROGRESSBAR_FAIL_STATUS
} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PREFERENCES_LIST } from '../../../../shared/services/preference.service';
import { ScriptConsoleSettingsModel, ScriptTestResultModel, ScriptValidSyntaxResultModel } from '../../model/script-result.models';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {ImportAssetsService} from '../../../importAssets/service/import-assets.service';
import {PROGRESSBAR_INTERVAL_TIME} from '../../../../shared/model/constants';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {
	@ViewChild('codeMirror') codeMirrorComponent: CodeMirrorComponent;
	@ViewChild('resizableForm') resizableForm: ElementRef;
	private collapsed = {
		code: true,
		sample: false,
		transform: false,
	};
	private script: string;
	private filename: string;
	private isWindowMaximized = false;
	private initialWindowStyle = null;
	private modalOptions: DecoratorOptions;
	private sampleDataModel: SampleDataModel = new SampleDataModel([], []);
	private operationStatus = {
		save: undefined,
		test: new OperationStatusModel(),
		syntax: new OperationStatusModel(),
	};
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();
	private scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();
	private closeErrorsSection = false;
	protected CHECK_ACTION = CHECK_ACTION;
	protected testScriptProgress = {
		progressKey: null,
		currentProgress: 0,
	};
	private testScripInterval: any;
	public MESSAGE_FIELD_WILL_BE_INITIALIZED: string;

	ngAfterViewInit(): void {
		this.MESSAGE_FIELD_WILL_BE_INITIALIZED =  this.translatePipe.transform('DATA_INGESTION.DATASCRIPT.DESIGNER.FIELD_WILL_BE_INITIALIZED');
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

	constructor(
		private translatePipe: TranslatePipe,
		private dialogService: UIDialogService,
		private dataScriptModel: DataScriptModel,
		private dataIngestionService: DataIngestionService,
		private importAssetsService: ImportAssetsService,
		private notifierService: NotifierService,
		private promptService: UIPromptService) {
		super('#etlBuilder');
		this.script =  this.dataScriptModel.etlSourceCode ? this.dataScriptModel.etlSourceCode.slice(0) : '';
		this.modalOptions = { isFullScreen: true, isResizable: true, sizeNamePreference: PREFERENCES_LIST.DATA_SCRIPT_SIZE };
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
		this.clearLogVariables('test');
		this.operationStatus.test.state = CHECK_ACTION.IN_PROGRESS;
		this.dataIngestionService.testScript(this.script, this.filename).subscribe( (result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS && result.data.progressKey) {
				this.testScriptProgress.progressKey = result.data.progressKey;
				this.setProgressLoop();
			} else {
				this.operationStatus.test.state = CHECK_ACTION.INVALID;
			}
		}, error => this.operationStatus.test.state = CHECK_ACTION.INVALID);
	}

	/**
	 * Initializes the Progress loop.
	 */
	private setProgressLoop(): void {
		this.testScriptProgress.currentProgress = 0;
		this.progressLoop();
	}

	/**
	 * Progress loop, this function is called recursively until the progress finish.
	 */
	private progressLoop(): void {
		this.dataIngestionService.getJobProgress(this.testScriptProgress.progressKey)
			.subscribe( (response: ApiResponseModel) => {
				let currentProgress = response.data.percentComp;
				this.testScriptProgress.currentProgress = currentProgress;
				// On Fail
				if (response.data.status === PROGRESSBAR_FAIL_STATUS) {
					this.scriptTestResult = new ScriptTestResultModel();
					this.operationStatus.test.state = CHECK_ACTION.INVALID;
					this.scriptTestResult.isValid = false;
					this.scriptTestResult.error = response.data.detail;
					// On Success
				} else if (currentProgress === 100 && response.data.status === PROGRESSBAR_COMPLETED_STATUS) {
					setTimeout( () => {
						let scripTestFilename = response.data.detail;
						this.operationStatus.test.state = CHECK_ACTION.VALID;
						this.scriptTestResult = new ScriptTestResultModel();
						this.scriptTestResult.isValid = true;
						this.importAssetsService.getFileContent(scripTestFilename)
							.subscribe(result => {
								this.scriptTestResult.domains = result.domains;
								this.scriptTestResult.consoleLog = result.consoleLog;
								this.consoleSettings.scriptTestResult = this.scriptTestResult;
							});
					}, 500);
				} else {
					setTimeout(() => {
						this.progressLoop();
					}, 2000)
				}
			});
	}

	/**
	 * On Check Script Syntax button.
	 */
	private onCheckScriptSyntax(): void {
		this.clearLogVariables('syntax');
		this.dataIngestionService.checkSyntax(this.script, this.filename).subscribe( result => {
			this.scriptValidSyntaxResult = result.data;
			this.operationStatus.syntax.state = this.scriptValidSyntaxResult.validSyntax ? CHECK_ACTION.VALID : CHECK_ACTION.INVALID;
			// mark on code mirror error syntax if present.
			const errorLines: Array<number> = this.scriptValidSyntaxResult.errors.map( error => {
				return error.startLine - 1;
			});
			if (errorLines.length > 0) {
				this.codeMirrorComponent.addSyntaxErrors(errorLines);
			} else {
				this.codeMirrorComponent.clearSyntaxErrors();
			}
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
			const result = {
				updated: this.operationStatus.save === 'success',
				newEtlScriptCode: this.script
			};

			this.close(result);
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
		this.operationStatus.save = undefined;
		this.operationStatus.syntax.value = event.newValue;
		this.operationStatus.test.value = event.newValue;
	}

	protected toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	protected onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [])
			.then((filename) => {
				this.filename = filename;
				this.extractSampleDataFromFile();
			})
			.catch((err) => {
				console.log('SampleDataDialog error occurred..');
				if (err) {
					console.log(err);
				}
		});
	}

	/**
	 * Call API and get the Sample Data based on the FileName already Uploaded
	 */
	private extractSampleDataFromFile() {
		this.dataIngestionService.getSampleData(this.filename).subscribe((result) => {
			this.sampleDataModel = result;
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
			// also clean syntax results
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
		if (operation === 'syntax') {
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
			// also clean test results
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
	}

	protected isCheckSyntaxDisabled(): boolean {
		return !this.script || !this.filename;
	}

	protected isTestDisabled(): boolean {
		return !this.script || !this.filename || this.operationStatus.test.state === CHECK_ACTION.IN_PROGRESS;
	}

	protected maximizeWindow() {
		const { width, height } = this.resizableForm.nativeElement.style;
		this.initialWindowStyle = { width, height };
		this.isWindowMaximized = true;
	}

	protected restoreWindow() {
		this.isWindowMaximized = false;
	}

	/**
	 * if value is present return value otherwise returns init
	 */
	public getInitOrValue(dataItem): string {
		if (dataItem.value) {
			return dataItem.value;
		}
		return (dataItem.init || '');
	}
}