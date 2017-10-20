import {Component, OnInit} from '@angular/core';
import {ImportAssetsService} from '../../service/import-assets.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {AlertType} from '../../../../shared/model/alert.model';

@Component({
	selector: 'manual-import',
	templateUrl: '../tds/web-app/app-js/modules/importAssets/components/manual-import/manual-import.component.html'
})
export class ManualImportComponent implements OnInit {

	private actionOptions = [];
	private dataScriptOptions = [];
	private selectedActionOption: any;
	private selectedScriptOption: any;
	private fetchResult: any;
	private fetchInProcess = false;
	private transformResult: any;
	private transformInProcess = false;
	private importResult: any;
	private importInProcess = false;
	private fileContent: any;
	private viewDataType: string;
	private HARD_CODED_DELAY = 1000;

	constructor( private importAssetsService: ImportAssetsService, private notifier: NotifierService) { }

	ngOnInit(): void {
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			console.log(result);
			this.actionOptions = result.actions;
			this.dataScriptOptions = result.dataScripts;
		});
	}

	private onFetch(): void {
		this.fetchInProcess = true;
		this.fetchResult = null;
		this.transformResult = null;
		// this.selectedScriptOption = null;
		this.importAssetsService.postFetch(this.selectedActionOption).subscribe( (result) => {
			setTimeout(() => {
				console.log(result);
				this.fetchResult = result;
				if (result.status === 'error') {
					this.notifier.broadcast({
						name: AlertType.DANGER,
						message: result.errors[0]
					});
				}
				this.fetchInProcess = false;
			}, this.HARD_CODED_DELAY );
		} );
	}

	private onActionScriptChange(event: any): void {
		let matchedScript = this.dataScriptOptions.find( script => script.id === event.defaultDataScriptId );
		if (matchedScript) {
			this.selectedScriptOption = matchedScript;
		}
	}

	private onTransform(): void {
		this.transformInProcess = true;
		this.transformResult = null;
		this.importResult = null;
		this.importAssetsService.postTransform(this.selectedScriptOption, this.fetchResult.filename).subscribe( (result) => {
			setTimeout(() => {
				console.log(result);
				this.transformResult = result;
				if (result.status === 'error') {
					this.notifier.broadcast({
						name: AlertType.DANGER,
						message: result.errors[0]
					});
				}
				this.transformInProcess = false;
				}, this.HARD_CODED_DELAY);
		} );
	}

	private onImport(): void {
		this.importInProcess = true;
		this.importResult = null;
		this.importAssetsService.postImport(this.transformResult.filename).subscribe( (result) => {
			setTimeout(() => {
				console.log(result);
				this.importResult = result;
				this.importInProcess = false;
			}, this.HARD_CODED_DELAY);
		});
	}

	private getFileContentValue(): string {
		if (this.fileContent) {
			return JSON.stringify(this.fileContent);
		} else {
			return '';
		}
	}

	private onViewData(type: string): void {
		this.fileContent = null;
		this.viewDataType = type;
		if (this.viewDataType === 'FETCH') {
			this.importAssetsService.getFileContent(this.fetchResult.filename).subscribe((result) => {
				this.fileContent = result;
			});
		} else {
			this.importAssetsService.getFileContent(this.transformResult.filename).subscribe((result) => {
				this.fileContent = result;
			});
		}
	}

	private onClearTextArea(): void {
		this.fileContent = null;
		this.viewDataType = null;
	}

}