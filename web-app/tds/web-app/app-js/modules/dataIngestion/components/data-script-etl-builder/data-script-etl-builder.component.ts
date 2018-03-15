import {Component, Output, EventEmitter, AfterViewInit, ViewChild, ElementRef} from '@angular/core';
import { UIExtraDialog, UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { DataScriptSampleDataComponent } from '../data-script-sample-data/data-script-sample-data.component';
import { DataScriptConsoleComponent } from '../data-script-console/data-script-console.component';
import {DataScriptModel, SampleDataModel} from '../../model/data-script.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import { PreferenceService } from '../../../../shared/services/preference.service';
import { ScriptConsoleSettingsModel, ScriptTestResultModel, ScriptValidSyntaxResultModel } from '../../model/script-result.models';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';

const DATA_SCRIPT_SIZE_PREFERENCE = 'DataScriptSize';

@Component({
	selector: 'data-script-etl-builder',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/data-script-etl-builder/data-script-etl-builder.component.html'
})
export class DataScriptEtlBuilderComponent extends UIExtraDialog implements AfterViewInit {
	@ViewChild('codeMirror') codeMirrorComponent: CodeMirrorComponent;
	@ViewChild('resizableForm') resizableForm: ElementRef;
	public width = 1120;
	public height = 600;
	private collapsed = {
		code: true,
		sample: false
	};
	private script: string;
	private filename: string;
	private sampleDataModel: SampleDataModel = new SampleDataModel([], []);

	private operationStatus = {
		save: undefined,
		test: new OperationStatusModel(),
		syntax: new OperationStatusModel(),
	};
	private consoleSettings: ScriptConsoleSettingsModel = new ScriptConsoleSettingsModel();
	private scriptTestResult: ScriptTestResultModel = new ScriptTestResultModel();
	private scriptValidSyntaxResult: ScriptValidSyntaxResultModel = new ScriptValidSyntaxResultModel();

	ngAfterViewInit(): void {
		setTimeout(() => {
			this.collapsed.code = false;
		}, 300);

		this.preferenceService.getPreference(DATA_SCRIPT_SIZE_PREFERENCE)
			.subscribe((result: any) => {
				try {
					const size = this.preferenceService.preferences[DATA_SCRIPT_SIZE_PREFERENCE];
					if (size) {
						let measure: string[] = size.split('x');
						if (measure.length) {
							this.width = Number(measure.length &&  measure.shift() || this.width);
							this.height = Number(measure.length &&  measure.shift() || this.height);
						}
					}
				} catch (e) {
					console.log(`Wrong format getting ${DATA_SCRIPT_SIZE_PREFERENCE}`);
				}
			}, (error) => {
				console.log(error);
			});
	}

	constructor(
		private dialogService: UIDialogService,
		private dataScriptModel: DataScriptModel,
		private dataIngestionService: DataIngestionService,
		private notifierService: NotifierService,
		private promptService: UIPromptService,
		private preferenceService: PreferenceService) {
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
		this.clearLogVariables('test');
		this.dataIngestionService.testScript(this.script, this.filename).subscribe( result => {
			this.scriptTestResult = result.data;
			this.scriptTestResult.domains = result.data.data.domains;
			this.operationStatus.test.state = this.scriptTestResult.isValid ? CHECK_ACTION.VALID : CHECK_ACTION.INVALID;
			for (let domain of this.scriptTestResult.domains) {
				this.collapsed[domain.domain] = false;
			}
			this.consoleSettings.scriptTestResult = this.scriptTestResult;
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
			this.saveSizeDataScriptDesigner()
				.then(() => this.close(result))
				.catch((error) => console.log(error));
		}
	}

	private saveSizeDataScriptDesigner(): Promise<any> {
		return new Promise((resolve, reject) => {
			const unit = 'px';
			const style = this.resizableForm.nativeElement.style;
			const width = (style && style.width || this.width.toString()).replace(unit, '');
			const height = (style && style.height || this.height.toString()).replace(unit, '');
			this.preferenceService.setPreference(DATA_SCRIPT_SIZE_PREFERENCE, `${width}x${height}`)
				.subscribe((data) => resolve(data),
					(error) => reject(error));
		});
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
			// also clean syntax results
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
		if (operation === 'syntax') {
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
			// also clean test results
			this.scriptValidSyntaxResult = new ScriptValidSyntaxResultModel();
		}
	}
}