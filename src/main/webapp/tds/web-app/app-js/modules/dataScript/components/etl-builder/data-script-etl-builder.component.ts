import {Component, AfterViewInit, ViewChild, ElementRef} from '@angular/core';
import 'rxjs/add/operator/finally';

import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../console/data-script-console.component';
import {DataScriptModel, SampleDataModel} from '../../model/data-script.model';
import {
	DataScriptService, PROGRESSBAR_COMPLETED_STATUS, PROGRESSBAR_FAIL_STATUS
} from '../../service/data-script.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import { ScriptConsoleSettingsModel, ScriptTestResultModel, ScriptValidSyntaxResultModel } from '../../model/script-result.models';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {ImportAssetsService} from '../../../importBatch/service/import-assets.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {OBJECT_OR_LIST_PIPE} from '../../../../shared/pipes/utils.pipe';
import { isNullOrEmptyString } from '@progress/kendo-angular-grid/dist/es2015/utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {FieldInfoType} from '../../../importBatch/components/record/import-batch-record-fields.component';
import {FieldReferencePopupHelper} from '../../../../shared/components/field-reference-popup/field-reference-popup.helper';
import {FieldReferencePopupComponent} from '../../../../shared/components/field-reference-popup/field-reference-popup.component';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: 'data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {
	@ViewChild('codeMirror') codeMirrorComponent: CodeMirrorComponent;
	@ViewChild('resizableForm') resizableForm: ElementRef;
	public collapsed = {
		code: true,
		sample: false,
		transform: false,
	};
	public script: string;
	private filename: string;
	public isWindowMaximized = false;
	private initialWindowStyle = null;
	public modalOptions: DecoratorOptions;
	public sampleDataModel: SampleDataModel = new SampleDataModel([], []);
	protected sampleDataGridHelper: DataGridOperationsHelper;
	public operationStatus = {
		save: undefined,
		test: new OperationStatusModel(),
		syntax: new OperationStatusModel(),
	};
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();
	public scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	protected transformedDataGrids: Array<DataGridOperationsHelper>;
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();
	public closeErrorsSection = false;
	public CHECK_ACTION = CHECK_ACTION;
	protected testScriptProgress = {
		progressKey: null,
		currentProgress: 0,
	};
	public MESSAGE_FIELD_WILL_BE_INITIALIZED: string;
	protected OBJECT_OR_LIST_PIPE = OBJECT_OR_LIST_PIPE;
	protected FieldInfoType = FieldInfoType;
	protected fieldReferencePopupHelper: FieldReferencePopupHelper;

	constructor(
		private translatePipe: TranslatePipe,
		private dialogService: UIDialogService,
		public dataScriptModel: DataScriptModel,
		private dataIngestionService: DataScriptService,
		private importAssetsService: ImportAssetsService,
		private preferenceService: PreferenceService,
		private notifierService: NotifierService,
		private promptService: UIPromptService) {
			super('#etlBuilder');
			this.script = '';
			this.modalOptions = { isFullScreen: true, isResizable: true, sizeNamePreference: PREFERENCES_LIST.DATA_SCRIPT_SIZE };
			this.loadETLScript();
			this.fieldReferencePopupHelper = new FieldReferencePopupHelper();
	}

	ngAfterViewInit(): void {
		this.MESSAGE_FIELD_WILL_BE_INITIALIZED =  this.translatePipe.transform('DATA_INGESTION.DATASCRIPT.DESIGNER.FIELD_WILL_BE_INITIALIZED');
		this.preferenceService.getDataScriptDesignerSize()
			.subscribe((size: {width: number, height: number}) => {
				size.width = size.width >= window.innerWidth - 5 ? window.innerWidth : size.width;
				size.height = size.height >= window.innerHeight - 5 ? window.innerHeight : size.height;
				this.isWindowMaximized = (size.height === window.innerHeight && size.width === window.innerWidth);
			});

		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

	/**
	 * Loads the Script from API call, If a previous sampleFilename exists or has been uploaded before then it loads the
	 * content calling the #extractSampleDataFromFile method.
     */
	private loadETLScript(): void {
		this.dataIngestionService.getETLScript(this.dataScriptModel.id).subscribe((result: ApiResponseModel) => {
			if (result.status === ApiResponseModel.API_SUCCESS) {
				this.dataScriptModel = result.data.dataScript;
				this.filename = this.dataScriptModel.sampleFilename;
				this.script =  this.dataScriptModel.etlSourceCode ? this.dataScriptModel.etlSourceCode.slice(0) : '';
				if (this.filename && this.filename.length > 0) {
					this.extractSampleDataFromFile(this.dataScriptModel.originalSampleFilename);
				}
			}
		}, error => console.log(error));
	}

	/**
	 * Used to determine if the Refresh Sample Data button appears on the page
	 * @return true if a JSON file has been uploaded and available
	 */
	public showSampleDataRefresh(): boolean  {
		return (!isNullOrEmptyString(this.dataScriptModel.sampleFilename) && this.dataScriptModel.sampleFilename.endsWith('.json'));
	}

	/**
	 * On Test Script button.
	 */
	public onTestScript(): void {
		this.clearLogVariables('test');
		this.clearSyntaxErrors();
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
					this.scriptTestResult.error = response.data.data.message;
					this.addSyntaxErrors([response.data.data.startLine - 1]);

					// On Success
				} else if (currentProgress === 100 && response.data.status === PROGRESSBAR_COMPLETED_STATUS) {
					setTimeout( () => {
						this.clearSyntaxErrors();
						let scripTestFilename = response.data.detail;
						this.operationStatus.test.state = CHECK_ACTION.VALID;
						this.scriptTestResult = new ScriptTestResultModel();
						this.scriptTestResult.isValid = true;
						this.importAssetsService.getFileContent(scripTestFilename)
							.subscribe(result => {
								this.scriptTestResult.domains = result.domains;
								this.transformedDataGrids = [];
								this.scriptTestResult.domains.forEach( (domain,  index) => {
									this.transformedDataGrids[index] = new DataGridOperationsHelper(domain.data);
									// console.log(`${domain.domain}-${index.toString()}`);
								});
								this.scriptTestResult.consoleLog = result.consoleLog;
								this.consoleSettings.scriptTestResult = this.scriptTestResult;
								// Finally re-load the Sample Data Preview if working with JSON files.
								if (this.showSampleDataRefresh()) {
									this.reloadSampleData();
								}
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
	public onCheckScriptSyntax(): void {
		this.clearSyntaxErrors();
		this.clearLogVariables('syntax');
		this.dataIngestionService.checkSyntax(this.script, this.filename).subscribe( result => {
			this.scriptValidSyntaxResult = result.data;
			this.operationStatus.syntax.state = this.scriptValidSyntaxResult.validSyntax ? CHECK_ACTION.VALID : CHECK_ACTION.INVALID;
			// mark on code mirror error syntax if present.
			const errorLines: Array<number> = this.scriptValidSyntaxResult.errors.map( error => {
				return error.startLine - 1;
			});
			if (errorLines.length > 0) {
				this.addSyntaxErrors(errorLines);
			} else {
				this.clearSyntaxErrors();
			}
		});
	}

	public cancelCloseDialog($event): void {
		if ($event && $event.target && $event.target.classList.contains(FieldReferencePopupComponent.POPUP_ESC_TRIGGER_CLASS)) {
			return;
		}
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

	public onSave(): void {
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

	public isScriptDirty(): boolean {
		return (this.dataScriptModel && (this.dataScriptModel.etlSourceCode !== this.script));
	}

	private onScriptChange(event: { newValue: string, oldValue: string }) {
		this.operationStatus.save = undefined;
		this.operationStatus.syntax.value = event.newValue;
		this.operationStatus.test.value = event.newValue;
	}

	public toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	public onLoadSampleData(): void {
		this.dialogService.extra(DataScriptSampleDataComponent, [
			{provide: 'etlScript', useValue: this.dataScriptModel}
		])
			.then((filename: {temporaryFileName: string, originalFileName: string}) => {
				this.filename = filename.temporaryFileName;
				this.extractSampleDataFromFile(filename.originalFileName);
			})
			.catch((err) => {
				console.log('SampleDataDialog error occurred..');
				if (err) {
					console.log(err);
				}
		});
	}

	/**
	 * On refresh sample data button click.
	 */
	protected reloadSampleData(): void {
		this.extractSampleDataFromFile(this.dataScriptModel.originalSampleFilename);
	}

	/**
	 * Call API and get the Sample Data content based on the FileName that has been already Uploaded or used.
	 */
	private extractSampleDataFromFile(originalFileName?: string) {
		this.clearLogVariables('sampleData');
		const rootNode = this.extractRootNode();
		this.dataIngestionService.getSampleData(this.dataScriptModel.id, this.filename, originalFileName, rootNode).subscribe((result) => {
			this.sampleDataModel = result;
			if (this.sampleDataModel.data) {
				this.sampleDataGridHelper = new DataGridOperationsHelper(this.sampleDataModel.data ? this.sampleDataModel.data : []);
				this.dataIngestionService.getETLScript(this.dataScriptModel.id).subscribe((result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.dataScriptModel.originalSampleFilename = result.data.dataScript.originalSampleFilename;
						this.dataScriptModel.sampleFilename = result.data.dataScript.sampleFilename;
					}
				}, error => console.log(error));
			}
		});
	}

	/**
	 * Search and extracts the rootNode value if present from the current script code.
	 * It assumes that rootNode syntax will be: rootNode 'myRootNode'
	 * @returns {string}
	 */
	private extractRootNode(): string {
		let match = this.script.match(/\s*rootNode\s*(?:"|')(.*)(?:"|').*/);
		let result = '';
		if (match !== null && match[1] !== null) {
			result = match[1];
		}
		return result;
	}

	/**
	 * On View Console button open the console dialog.
	 */
	public onViewConsole(): void {
		this.dialogService.extra(DataScriptConsoleComponent, [{provide: ScriptConsoleSettingsModel, useValue: this.consoleSettings}], false, true)
			.then((result) => {/* on ok */} )
			.catch((result) => {/* on close/cancel */});
	}

	public testHasErrors(): boolean {
		return !this.scriptTestResult.isValid && this.scriptTestResult.error && this.scriptTestResult.error.length > 0;
	}

	public sampleDataHasErrors(): boolean {
		return this.sampleDataModel && this.sampleDataModel.errors && this.sampleDataModel.errors.length > 0;
	}

	public syntaxHasErrors(): boolean {
		return !this.scriptValidSyntaxResult.validSyntax && this.scriptValidSyntaxResult.errors && this.scriptValidSyntaxResult.errors.length > 0;
	}

	public closeErrors(): void {
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
		if (operation === 'sampleData') {
			this.sampleDataModel = null;
		}
	}

	public isCheckSyntaxDisabled(): boolean {
		return !this.script || !this.filename;
	}

	public isTestDisabled(): boolean {
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
	protected getInitOrValue(dataItem): string {
		if (dataItem.value) {
			return dataItem.value;
		} else {
			return (dataItem.init || '');
		}
	}

	/**
	 * Clears out ALL the Syntax Error class of the given line numbers stored in currentErrorLines.
	 * If the codeMirrorComponent object is empty, it does nothing.
	 */
	private clearSyntaxErrors(): void {
		if (this.codeMirrorComponent) {
			this.codeMirrorComponent.clearSyntaxErrors();
		}
	}

	/**
	 * Adds Syntax Error class to the given line numbers (lines index starts at 0)
	 * If the codeMirrorComponent object is empty, it does nothing.
	 * @param {Array<number>} lineNumbers
	 */
	private addSyntaxErrors(lineNumbers: Array<number>) {
		if (this.codeMirrorComponent) {
			this.codeMirrorComponent.addSyntaxErrors(lineNumbers);
		}
	}
}
