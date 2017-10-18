import {Component, Inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ImportAssetsService} from '../../service/import-assets.service';

@Component({
	selector: 'manual-import',
	templateUrl: '../tds/web-app/app-js/modules/importAssets/components/manual-import/manual-import.component.html'
})
export class ManualImportComponent implements OnInit {

	private manualOptions = [];
	private dataScriptOptions = [];
	private selectedManualOption: any;
	private selectedScriptOption: any;

	constructor( private importAssetsService: ImportAssetsService) {
	}

	ngOnInit(): void {
		this.importAssetsService.getManualOptions().subscribe( (result) => {
			this.manualOptions = result;
		});
		this.importAssetsService.getDataScriptOptions().subscribe( (result) => {
			this.dataScriptOptions = result;
		});
	}

	private fetchResult: any;
	private onFetch(): void {
		this.importAssetsService.postFetch(this.selectedManualOption).subscribe( (result) => {
			this.fetchResult = result;
		} );
	}

	private fileContent: any;
	private onViewData(type: string): void {
		this.fileContent = null;
		if (type === 'FETCH') {
			this.importAssetsService.getFileContent(this.fetchResult.filename, this.fetchResult.extension).subscribe((result) => {
				this.fileContent = result;
			});
		} else {
			this.importAssetsService.getFileContent(this.transformResult.outputFilename, this.transformResult.outputFilenameExtension).subscribe((result) => {
				this.fileContent = result;
			});
		}
	}

	private transformResult: any;
	private onTransform(): void {
		this.importAssetsService.postTransform(this.selectedScriptOption).subscribe( (result) => {
			this.transformResult = result;
		} );
	}

	private importResult: any;
	private onImport(): void {
		this.importAssetsService.postImport(this.transformResult.outputFilename).subscribe( (result) => {
			this.importResult = result;
		} );
	}

	private onClearTextArea(): void {
		this.fileContent = null;
	}
}