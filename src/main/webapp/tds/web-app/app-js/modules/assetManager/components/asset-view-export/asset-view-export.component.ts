import {Component, ViewChild} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';
import {FieldImportance} from '../../../fieldSettings/model/field-settings.model';
import {AssetExportModel} from '../../../assetExplorer/model/asset-export-model';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'asset-explorer-view-export',
	template: `
        <div class="modal-content">
            <div class="modal-header">
                <button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close"><span aria-hidden="true">×</span></button>
                <h4 class="modal-title">Export to Excel</h4>
            </div>
            <div class="modal-body">
                <form name="exportForm" role="form" data-toggle="validator" #exportForm='ngForm'>
                    <div class="box-body">
                        <div class="form-group">
                            <label for="fileName" class="control-label">File Name </label>
                            <input type="text" class="form-control" id="fileName" [(ngModel)]="fileName" name="fileName" required [ngClass]="{'has-error': !exportForm.form.valid}"/>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer form-group-center">
                <tds-button-export class="btn-primary pull-left" (click)="getExportData()" [disabled]="!exportForm.form.valid">
                </tds-button-export>
                <kendo-excelexport [data]="dataToExport" fileName="{{exportFileName + '.xlsx'}}" #excelexport>
                    <kendo-excelexport-column *ngFor="let column of columns" [field]="column.name" [title]="column.title" [locked]="column.locked" [cellOptions]="column.cell">
										</kendo-excelexport-column>
                </kendo-excelexport>
                <tds-button-cancel class="pull-right"  (click)="cancelCloseDialog()"></tds-button-cancel>
            </div>
        </div>
	`,
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class AssetViewExportComponent {
	public columns: any[];
	public fileName = 'asset_explorer';
	public exportFileName = '';
	public dataToExport: any[] = [];
	private allProperties = false;
	private fieldImportance = new FieldImportance();
	@ViewChild('excelexport', {static: false}) public excelexport: ExcelExportComponent;

	constructor(public assetExportModel: AssetExportModel, public activeDialog: UIActiveDialogService, private assetExpService: AssetExplorerService) {

		let configuredColumns = {...this.assetExportModel.assetQueryParams.filters.columns};

		this.columns = Object.keys(configuredColumns).map((key) => {
			let definition = {
				name: this.getPropertyColumnField(configuredColumns[key]['domain'], '_', configuredColumns[key]['property']),
				title: configuredColumns[key]['label'],
				width: configuredColumns[key]['width'],
				locked: configuredColumns[key]['locked'],
				cell: {
					background: this.getImportanceColor(configuredColumns[key])
				}
			};

			return definition;
		});

		this.getFileName();
	}

	/**
	 * Get the Color Schema for the Selected Field
	 * @param model
	 * @returns {string}
	 */
	private getImportanceColor(model: any): string {
		let domain = this.assetExportModel.domains.find(r => r.domain.toLocaleLowerCase() === model.domain.toLocaleLowerCase());
		let field = domain.fields.find(r => r.label.toLocaleLowerCase() === model.label.toLocaleLowerCase());
		return this.fieldImportance[field.imp].color;
	}

	/**
	 * Transform the Data information
	 */
	public getExportData(): void {
		if (!this.assetExportModel.queryId) {
			this.assetExpService.previewQuery(this.assetExportModel.assetQueryParams)
				.subscribe(result => {
					this.exportFileName = this.fileName + '-' + DateUtils.getTimestamp();
					this.onExportDataResponse(result['assets']);
				}, err => console.log(err));
		} else {
			this.assetExpService.query(this.assetExportModel.queryId, this.assetExportModel.assetQueryParams)
				.subscribe(result => {
					this.exportFileName = this.fileName + '-' + DateUtils.getTimestamp();
					this.onExportDataResponse(result['assets']);
				}, err => console.log(err));
		}
	}

	/**
	 * Get the file Name to export the file
	 */
	private getFileName(): void {
		this.assetExpService.getFileName(this.assetExportModel.viewName)
			.subscribe(result => {
				this.fileName = result ? result : '';
			}, err => console.log(err));
	}

	private getPropertyColumnField(domain: string, separator: string, property: string): string {
		let domainDefinition = domain.toLocaleLowerCase();
		return domainDefinition + separator + property;
	}

	protected onExportDataResponse(results: any): void {
		this.dataToExport = results;
		this.prepareAssetTagsData();
		setTimeout(() => {
			this.excelexport.save();
			this.activeDialog.close();
		}, 500);
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * Builds a string value for Assets Tags fields in order to be excel exported.
	 */
	private prepareAssetTagsData() {
		const match = this.columns.find(col => col.name === 'common_tagAssets');
		if (match) {
			this.dataToExport.forEach(item => {
				const tags = [...item['common_tagAssets']];
				let tagsValue = '';
				tags.forEach((tag, index) => {
					tagsValue += `${tag.name}`;
					if (index < (tags.length - 1) ) {
						tagsValue += ', ';
					}
				});
				item['common_tagAssets'] = tagsValue;
			});
		}
	}
}
