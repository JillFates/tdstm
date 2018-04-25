import {Component, ViewChild} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';
import {DomainModel} from '../../../fieldSettings/model/domain.model';
import {FieldImportance} from '../../../fieldSettings/model/field-settings.model';
import {AssetExportModel} from '../../model/asset-export-model';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'asset-explorer-view-export',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-export/asset-explorer-view-export.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class AssetExplorerViewExportComponent {
	private columns: any[];
	protected fileName = 'asset_explorer';
	protected exportFileName = '';
	protected dataToExport: any[] = [];
	private allProperties = false;

	@ViewChild('excelexport') public excelexport: ExcelExportComponent;

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
		return FieldImportance[field.imp].color;
	}

	/**
	 * Transform the Data information
	 */
	private getExportData(): void {
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
				this.fileName = result;
			}, err => console.log(err));
	}

	private getPropertyColumnField(domain: string, separator: string, property: string): string {
		let domainDefinition = domain.toLocaleLowerCase();
		return domainDefinition + separator + property;
	}

	protected onExportDataResponse(results: any): void {
		this.dataToExport = results;
		setTimeout(() => {
			this.excelexport.save();
			this.activeDialog.close();
		}, 500);
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}
}