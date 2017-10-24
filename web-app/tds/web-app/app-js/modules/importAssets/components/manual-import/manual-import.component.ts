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
	private selectedActionOption = -1;
	private selectedScriptOption = -1;
	private fetchResult: any;
	private fetchInProcess = false;
	private transformResult: any;
	private transformInProcess = false;
	private importResult: any;
	private importInProcess = false;
	private fetchFileContent: any;
	private transformFileContent: any;
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
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		// this.selectedScriptOption = null;
		this.importAssetsService.postFetch(this.selectedActionOption).subscribe( (result) => {
			setTimeout(() => {
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
		this.transformFileContent = null;
		this.importResult = null;
		this.importAssetsService.postTransform(this.selectedScriptOption, this.fetchResult.filename).subscribe( (result) => {
			setTimeout(() => {
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
				this.importResult = result;
				this.importInProcess = false;
			}, this.HARD_CODED_DELAY);
		});
	}

	private getFetchFileContentValue(): string {
		if (this.fetchFileContent) {
			return JSON.stringify(this.fetchFileContent);
		} else {
			return '';
		}
	}

	private getTransformFileContentValue(): string {
		if (this.transformFileContent) {
			return JSON.stringify(this.transformFileContent);
		} else {
			return '';
		}
	}

	private onViewData(type: string): void {
		this.viewDataType = type;
		if (this.viewDataType === 'FETCH') {
			this.fetchFileContent = null;
			this.importAssetsService.getFileContent(this.fetchResult.filename).subscribe((result) => {
				this.fetchFileContent = result;
			});
		} else {
			this.transformFileContent = null;
			this.importAssetsService.getFileContent(this.transformResult.filename).subscribe((result) => {
				this.transformFileContent = result;
			});
		}
	}

	private onCloseFileContents(): void {
		this.fetchFileContent = null;
		this.transformFileContent = null;
		this.viewDataType = null;
	}

	private onClear(): void {
		this.fetchResult = null;
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.viewDataType = null;
		this.importResult = null;
	}

}