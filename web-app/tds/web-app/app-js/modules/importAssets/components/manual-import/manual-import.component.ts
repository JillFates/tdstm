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

	constructor( private importAssetsService: ImportAssetsService, private notifier: NotifierService) { }

	ngOnInit(): void {
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			this.actionOptions = result.actions;
			this.dataScriptOptions = result.dataScripts;
		});
	}

	/**
	 * Fetch button clicked event.
	 * Calls the process of fetch.
	 */
	private onFetch(): void {
		this.fetchInProcess = true;
		this.fetchResult = null;
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		// this.selectedScriptOption = null;
		this.importAssetsService.postFetch(this.selectedActionOption).subscribe( (result) => {
			this.fetchResult = result;
			if (result.status === 'error') {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: result.errors[0]
				});
			}
			this.fetchInProcess = false;
		} );
	}

	/**
	 * Event when action script select changes its value.
	 * @param event
	 */
	private onActionScriptChange(event: any): void {
		let matchedScript = this.dataScriptOptions.find( script => script.id === event.defaultDataScriptId );
		if (matchedScript) {
			this.selectedScriptOption = matchedScript;
		}
	}

	/**
	 * Transform button clicked event.
	 * Calls Transform process on endpoint.
	 */
	private onTransform(): void {
		this.transformInProcess = true;
		this.transformResult = null;
		this.transformFileContent = null;
		this.importResult = null;
		this.importAssetsService.postTransform(this.selectedScriptOption, this.fetchResult.filename).subscribe( (result) => {
			this.transformResult = result;
			if (result.status === 'error') {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: result.errors[0]
				});
			}
			this.transformInProcess = false;
		} );
	}

	/**
	 * Import button clicked event.
	 * Calls Import process on endpoint.
	 */
	private onImport(): void {
		this.importInProcess = true;
		this.importResult = null;
		this.importAssetsService.postImport(this.transformResult.filename).subscribe( (result) => {
			this.importResult = result;
			this.importInProcess = false;
		});
	}

	/**
	 * Gets the raw data of the view data fetch file content result.
	 * @returns {string}
	 */
	private getFetchFileContentValue(): string {
		if (this.fetchFileContent) {
			return JSON.stringify(this.fetchFileContent);
		} else {
			return '';
		}
	}

	/**
	 * Gets the raw data of the view data transform file content result.
	 * @returns {string}
	 */
	private getTransformFileContentValue(): string {
		if (this.transformFileContent) {
			return JSON.stringify(this.transformFileContent);
		} else {
			return '';
		}
	}

	/**
	 * View data clicked event.
	 * Calls endpoint to get file content result.
	 * @param {string} type : can be either 'Fetch' or 'Transform'
	 */
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

	/**
	 * Close clicked event.
	 * Cleans out results for fetch and transform.
	 */
	private onCloseFileContents(): void {
		this.fetchFileContent = null;
		this.transformFileContent = null;
		this.viewDataType = null;
	}

	/**
	 * Clear clicked event.
	 * Clears out most of results peformed on the page.
	 */
	private onClear(): void {
		this.fetchResult = null;
		this.fetchFileContent = null;
		this.transformResult = null;
		this.transformFileContent = null;
		this.viewDataType = null;
		this.importResult = null;
	}

}