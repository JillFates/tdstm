// Angular
import {
	Component,
	AfterViewInit,
	ViewChild,
	ElementRef,
	OnInit,
	Input,
	ComponentFactoryResolver
} from '@angular/core';
// Component
import { DataScriptSampleDataComponent } from '../sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../console/data-script-console.component';
import { CodeMirrorComponent } from '../../../../shared/modules/code-mirror/code-mirror.component';
import { FieldInfoType } from '../../../importBatch/components/record/import-batch-record-fields.component';
// Model
import { DataScriptModel, SampleDataModel } from '../../model/data-script.model';
import {
	ScriptConsoleSettingsModel,
	ScriptTestResultModel,
	ScriptValidSyntaxResultModel
} from '../../model/script-result.models';
import { ApiResponseModel } from '../../../../shared/model/ApiResponseModel';
import {
	CHECK_ACTION,
	OperationStatusModel,
} from '../../../../shared/components/check-action/model/check-action.model';
import {
	Dialog,
	DialogButtonType,
	DialogConfirmAction,
	DialogExit,
	DialogService,
	ModalSize
} from 'tds-component-library';
import { Permission } from '../../../../shared/model/permission.model';
import { isNullOrEmptyString } from '@progress/kendo-angular-grid/dist/es2015/utils';
// Service
import {
	DataScriptService,
	PROGRESSBAR_COMPLETED_STATUS,
	PROGRESSBAR_FAIL_STATUS,
} from '../../service/data-script.service';
import { ImportAssetsService } from '../../../importBatch/service/import-assets.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import { OBJECT_OR_LIST_PIPE } from '../../../../shared/pipes/utils.pipe';
import { FieldReferencePopupHelper } from '../../../../shared/components/field-reference-popup/field-reference-popup.helper';
// Other
import 'rxjs/add/operator/finally';
import { SelectEvent } from '@progress/kendo-angular-layout';
import { DataGridOperationsHelper } from '../../../../shared/utils/data-grid-operations.helper';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: 'data-script-etl-builder.component.html',
})
export class DataScriptEtlBuilderComponent extends Dialog implements OnInit, AfterViewInit {
	@Input() data: any;
	@ViewChild('codeMirror', { static: false })
	codeMirrorComponent: CodeMirrorComponent;
	@ViewChild('resizableForm', { static: false }) resizableForm: ElementRef;
	public collapsed = {
		code: true,
		sample: false,
		transform: false,
	};
	public script: string;
	public sampleDataModel: SampleDataModel = new SampleDataModel([], []);
	public operationStatus = {
		save: undefined,
		test: new OperationStatusModel(),
		syntax: new OperationStatusModel(),
	};
	public scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	public closeErrorsSection = false;
	public CHECK_ACTION = CHECK_ACTION;
	public MESSAGE_FIELD_WILL_BE_INITIALIZED: string;
	public dataScriptModel: DataScriptModel;
	protected sampleDataGridHelper: DataGridOperationsHelper;
	protected transformedDataGrids: Array<DataGridOperationsHelper>;
	protected testScriptProgress = {
		progressKey: null,
		currentProgress: 0,
	};
	protected OBJECT_OR_LIST_PIPE = OBJECT_OR_LIST_PIPE;
	protected FieldInfoType = FieldInfoType;
	protected fieldReferencePopupHelper: FieldReferencePopupHelper;
	private filename: string;
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();

	public showSampleData = false;
	public showTransformedData = [];

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private translatePipe: TranslatePipe,
		private dialogService: DialogService,
		private dataIngestionService: DataScriptService,
		private importAssetsService: ImportAssetsService,
		private permissionService: PermissionService
	) {
		super();
	}

	ngOnInit(): void {
		this.dataScriptModel = Object.assign({}, this.data.dataScriptModel);
		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			disabled: () => !this.isUpdateAvailable() || !this.isScriptDirty() || (this.operationStatus.save && this.operationStatus.save !== 'success'),
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});
		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});
		this.script = '';
		this.loadETLScript();
		this.fieldReferencePopupHelper = new FieldReferencePopupHelper();
		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
	}

	ngAfterViewInit(): void {
		this.MESSAGE_FIELD_WILL_BE_INITIALIZED = this.translatePipe.transform(
			'DATA_INGESTION.DATASCRIPT.DESIGNER.FIELD_WILL_BE_INITIALIZED'
		);
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);
	}

	/**
	 * Used to determine if the Refresh Sample Data button appears on the page
	 * @return true if a JSON file has been uploaded and available
	 */
	public showSampleDataRefresh(): boolean {
		return (
			!isNullOrEmptyString(this.dataScriptModel.sampleFilename) &&
			this.dataScriptModel.sampleFilename.endsWith('.json')
		);
	}

	/**
	 * On Test Script button.
	 */
	public onTestScript(): void {
		this.clearLogVariables('test');
		this.clearSyntaxErrors();
		this.operationStatus.test.state = CHECK_ACTION.IN_PROGRESS;
		this.dataIngestionService
			.testScript(this.script, this.filename)
			.subscribe(
				(result: ApiResponseModel) => {
					if (
						result.status === ApiResponseModel.API_SUCCESS &&
						result.data.progressKey
					) {
						this.testScriptProgress.progressKey =
							result.data.progressKey;
						this.setProgressLoop();
					} else {
						this.operationStatus.test.state = CHECK_ACTION.INVALID;
					}
				},
				error =>
					(this.operationStatus.test.state = CHECK_ACTION.INVALID)
			);
	}

	/**
	 * On Check Script Syntax button.
	 */
	public onCheckScriptSyntax(): void {
		this.clearSyntaxErrors();
		this.clearLogVariables('syntax');
		this.dataIngestionService
			.checkSyntax(this.script, this.filename)
			.subscribe(result => {
				this.scriptValidSyntaxResult = result.data;
				this.operationStatus.syntax.state = this.scriptValidSyntaxResult
					.validSyntax
					? CHECK_ACTION.VALID
					: CHECK_ACTION.INVALID;
				// mark on code mirror error syntax if present.
				const errorLines: Array<number> = this.scriptValidSyntaxResult.errors.map(
					error => {
						return error.startLine - 1;
					}
				);
				if (errorLines.length > 0) {
					this.addSyntaxErrors(errorLines);
				} else {
					this.clearSyntaxErrors();
				}
			});
	}

	public cancelCloseDialog(): void {
		if (this.isScriptDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					if (result) {
						this.onCancelClose(result);
					}
				}
			});
		} else {
			const result = {
				updated: this.operationStatus.save === 'success',
				newEtlScriptCode: this.script,
			};
			this.onCancelClose(result);
		}
	}

	public onSave(): void {
		this.operationStatus.save = 'progress';
		this.clearLogVariables();
		this.dataIngestionService
			.saveScript(this.dataScriptModel.id, this.script)
			.subscribe(
				result => {
					if (result) {
						this.dataScriptModel.etlSourceCode = this.script.slice(
							0
						);
						this.operationStatus.save = 'success';
					} else {
						this.operationStatus.save = 'fail';
					}
				},
				err => {
					console.log(err);
				}
			);
	}

	public isScriptDirty(): boolean {
		return this.dataScriptModel.etlSourceCode
			? this.dataScriptModel.etlSourceCode !== this.script
			: this.script.length > 0;
	}

	public toggleSection(section: string) {
		this.collapsed[section] = !this.collapsed[section];
	}

	public onLoadSampleData(): void {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: DataScriptSampleDataComponent,
			data: {
				etlScript: this.dataScriptModel
			},
			modalConfiguration: {
				title: 'Sample Data',
				draggable: true,
				modalSize: ModalSize.LG
			}
		}).subscribe(
			(filename: {
				temporaryFileName: string;
				originalFileName: string;
				status: string
			}) => {
				if (filename.status !== DialogExit.CLOSE) {
					this.filename = filename.temporaryFileName;
					this.extractSampleDataFromFile(filename.originalFileName);
				}
			});
	}

	/**
	 * On View Console button open the console dialog.
	 */
	public onViewConsole(): void {
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: DataScriptConsoleComponent,
			data: {
				consoleSettingsModel: this.consoleSettings
			},
			modalConfiguration: {
				title: 'View console?',
				draggable: true,
				modalSize: ModalSize.LG
			}
		}).subscribe((result: any) => {
			//
		});
	}

	public testHasErrors(): boolean {
		return (
			!this.scriptTestResult.isValid &&
			this.scriptTestResult.error &&
			this.scriptTestResult.error.length > 0
		);
	}

	public sampleDataHasErrors(): boolean {
		return (
			this.sampleDataModel &&
			this.sampleDataModel.errors &&
			this.sampleDataModel.errors.length > 0
		);
	}

	public syntaxHasErrors(): boolean {
		return (
			!this.scriptValidSyntaxResult.validSyntax &&
			this.scriptValidSyntaxResult.errors &&
			this.scriptValidSyntaxResult.errors.length > 0
		);
	}

	public closeErrors(): void {
		this.closeErrorsSection = true;
	}

	public isCheckSyntaxDisabled(): boolean {
		return !this.script || !this.filename;
	}

	public isTestDisabled(): boolean {
		return (
			!this.script ||
			!this.filename ||
			this.operationStatus.test.state === CHECK_ACTION.IN_PROGRESS
		);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * On refresh sample data button click.
	 */
	protected reloadSampleData(): void {
		this.extractSampleDataFromFile(
			this.dataScriptModel.originalSampleFilename
		);
	}

	/**
	 * if value is present return value otherwise returns init
	 */
	protected getInitOrValue(dataItem): string {
		if (dataItem.value !== undefined && dataItem.value !== null) {
			return dataItem.value;
		} else {
			return dataItem.init || '';
		}
	}

	protected canLoadSampleData(): boolean {
		return this.permissionService.hasPermission(
			Permission.ETLScriptLoadSampleData
		);
	}

	protected isUpdateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ETLScriptUpdate);
	}

	/**
	 * Loads the Script from API call, If a previous sampleFilename exists or has been uploaded before then it loads the
	 * content calling the #extractSampleDataFromFile method.
	 */
	private loadETLScript(): void {
		this.dataIngestionService
			.getETLScript(this.dataScriptModel.id)
			.subscribe(
				(result: ApiResponseModel) => {
					if (result.status === ApiResponseModel.API_SUCCESS) {
						this.dataScriptModel = result.data.dataScript;
						this.filename = this.dataScriptModel.sampleFilename;
						this.script = this.dataScriptModel.etlSourceCode
							? this.dataScriptModel.etlSourceCode.slice(0)
							: '';
						if (this.filename && this.filename.length > 0) {
							this.extractSampleDataFromFile(
								this.dataScriptModel.originalSampleFilename
							);
						}
					}
				},
				error => console.log(error)
			);
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
		this.dataIngestionService
			.getJobProgress(this.testScriptProgress.progressKey)
			.subscribe((response: ApiResponseModel) => {
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
				} else if (
					currentProgress === 100 &&
					response.data.status === PROGRESSBAR_COMPLETED_STATUS
				) {
					setTimeout(() => {
						this.clearSyntaxErrors();
						let scripTestFilename = response.data.detail;
						this.operationStatus.test.state = CHECK_ACTION.VALID;
						this.scriptTestResult = new ScriptTestResultModel();
						this.scriptTestResult.isValid = true;
						this.importAssetsService
							.getFileContent(scripTestFilename)
							.subscribe(result => {
								const data = (result && result.data) || {};
								this.scriptTestResult.domains = data.domains || [];
								this.transformedDataGrids = [];
								// Load first domain data only from the array.
								if (this.scriptTestResult.domains.length) {
									this.loadDataForDomain(0);
								}
								this.scriptTestResult.consoleLog = data.consoleLog;
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
					}, 2000);
				}
			});
	}

	/**
	 * On tab domain change, load it's data if hasn't been loaded yet.
	 * @param $event
	 */
	onDomainTabSelected($event: SelectEvent) {
		this.loadDataForDomain($event.index);
	}

	/**
	 * Returns the correct title name for the domain tab.
	 * @param domain
	 */
	getDomainTabTitle(domain: any): string {
		let title = domain.domain;
		return domain.dataSize || domain.dataSize === 0 ? `${title} (${domain.dataSize})` : title;
	}

	/**
	 * If current domain doesn't have [data], then it needs to be loaded from a file.
	 * @param tabIndex
	 */
	private loadDataForDomain(tabIndex: number): void {
		const domain: any = this.scriptTestResult.domains[tabIndex];
		if (!domain.data && domain.outputFilename) {
			this.importAssetsService.getFileContent(domain.outputFilename)
				.subscribe((result: any) => {
					this.transformedDataGrids[tabIndex] = new DataGridOperationsHelper(result.data ? result.data : []);
					this.showTransformedData[tabIndex] = false;
					domain.data = result.data;
				});
		} else {
			this.transformedDataGrids[tabIndex] = new DataGridOperationsHelper(domain.data ? domain.data : []);
			this.showTransformedData[tabIndex] = false;
		}
	}

	private onScriptChange(event: { newValue: string; oldValue: string }) {
		this.operationStatus.save = undefined;
		this.operationStatus.syntax.value = event.newValue;
		this.operationStatus.test.value = event.newValue;
	}

	/**
	 * Call API and get the Sample Data content based on the FileName that has been already Uploaded or used.
	 */
	private extractSampleDataFromFile(originalFileName?: string) {
		this.clearLogVariables('sampleData');
		const rootNode = this.extractRootNode();
		this.dataIngestionService
			.getSampleData(
				this.dataScriptModel.id,
				this.filename,
				originalFileName,
				rootNode
			)
			.subscribe(result => {
				this.sampleDataModel = result;
				if (this.sampleDataModel.data) {
					this.sampleDataGridHelper = new DataGridOperationsHelper(
						this.sampleDataModel.data
							? this.sampleDataModel.data
							: []
					);
					this.dataIngestionService
						.getETLScript(this.dataScriptModel.id)
						.subscribe(
							(result: ApiResponseModel) => {
								if (
									result.status ===
									ApiResponseModel.API_SUCCESS
								) {
									this.dataScriptModel.originalSampleFilename =
										result.data.dataScript.originalSampleFilename;
									this.dataScriptModel.sampleFilename =
										result.data.dataScript.sampleFilename;
								}
							},
							error => console.log(error)
						);
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

	private getSyntaxErrors(): string {
		let errors = this.scriptValidSyntaxResult.errors.map(error => {
			return `message: ${ error.message } --> start line: ${ error.startLine }, end line: ${ error.endLine }, start column: ${ error.startColumn }, endColumn: ${ error.endColumn }, fatal: ${ error.fatal }`;
		});
		return errors.join('\n');
	}

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

	/**
	 * Based on modalType action returns the corresponding title
	 */
	private getModalTitle(): string {
		setTimeout(() => {
			if (this.codeMirrorComponent) {
				this.codeMirrorComponent.instance.focus();
				this.codeMirrorComponent.instance.setCursor(this.codeMirrorComponent.instance.lineCount(), 0);
			}
		}, 1000);
		return `ETL Script Edit - ${ this.dataScriptModel.provider.name } / ${ this.dataScriptModel.name } `;
	}

	/**
	 * Clears the filter on supports
	 */
	public showFilterSampleData(): void {
		if (this.showSampleData) {
			this.showSampleData = false;
			this.sampleDataGridHelper.clearAllFilters(this.sampleDataModel.columns);
		} else {
			this.showSampleData = true;
		}
	}

	/**
	 * Hide show filters per data result
	 * @param index
	 */
	public showFiltersTransformedData(index: number, columns: any): void {
		if (this.showTransformedData[index]) {
			this.showTransformedData[index] = false;
			this.sampleDataGridHelper.clearAllFilters(columns);
		} else {
			this.showTransformedData[index] = true;
		}
	}
}
