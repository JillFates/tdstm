import {Component, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Dialog, DialogButtonType} from 'tds-component-library';
import {ReplaySubject} from 'rxjs';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';
import {ManufacturerService} from '../../../modules/manufacturer/service/manufacturer.service';
import {ConnectorColumns, IConnector} from '../../../modules/manufacturer/model/connector.model';
import {IManufacturerModel, ManufacturerModelColumns} from '../../../modules/manufacturer/model/manufacturer-model.model';
import {ManufacturerColumns} from '../../../modules/manufacturer/model/manufacturer-export.model';

enum SHEET_NAMES {
	MANUFACTURERS = 'Manufacturer',
	MODELS = 'Models',
	CONNECTORS = 'Connectors'
}
@Component({
	selector: 'tds-export-manufacturer-models',
	template: `
		<div class="export-mfg-models-container">
				<form (ngSubmit)="export()" class="clr-form">
						<div class="clr-row">
						</div>
						<div class="row">
								<div class="clr-col-6 clr-align-self-center clr-display-inline">
                    <label
                            class="clr-control-label clr-control-label-sm inline"
                            [style.margin-right.rem]="'0.5'"
                            [style.font-weight]="'bold'"
                            for="export-file-name">File Name: </label>
                    <input
				                    class="clr-input"
                            id="export-file-name"
                            type="text"
                            name="export-file-name"
                            [(ngModel)]="exportFileName"
                            (ngModelChange)="updateCheckboxValue($event)"
                    />
								</div>
                <div class="clr-col-6 clr-align-self-center clr-display-inline">
                    <clr-checkbox-wrapper class="inline">
                        <label
                                class="clr-control-label clr-control-label-sm inline"
                                [style.font-weight]="'bold'"
                                for="tds-models-only">Only TDS models</label>
                        <input
                                clrCheckbox
                                id="tds-models-only"
                                type="checkbox"
                                name="tds-models-only"
                                [(ngModel)]="tdsModelsOnly"
                        />
                    </clr-checkbox-wrapper>
                </div>
						</div>
				</form>
        <kendo-excelexport
				        *ngFor="let data of (dataToExport$ | async); let i = index;"
				        [data]="data"
				        fileName="{{exportFileName + '.xlsx'}}"
				        #excelExport>
            <kendo-excelexport-column
				            *ngFor="let column of columns[i]"
				            [field]="column.property"
				            [title]="column.label"
				            [width]="column.width">
            </kendo-excelexport-column>
        </kendo-excelexport>
		</div>
	`
})
export class ExportManufacturerModelsComponent  extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChildren(ExcelExportComponent) excelExportComponents: QueryList<ExcelExportComponent>;
	formData = {
		tdsModelsOnly: 0
	};
	tdsModelsOnly = false;
	exportFileName = 'export-data-file-name';
	dataToExport$: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
	columns: any[] = [];

	constructor(private manufacturerService: ManufacturerService) {
		super();
	}

	ngOnInit(): void {

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onDismiss.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'export',
			text: 'Export',
			show: () => !!this.exportFileName,
			type: DialogButtonType.CONTEXT,
			action: this.export.bind(this)
		});
		this.columns = this.data && this.data.modelsOnly ? [ManufacturerModelColumns] : [ManufacturerColumns, ManufacturerModelColumns, ConnectorColumns];
	}

	updateCheckboxValue(checked: any): void {
		this.formData.tdsModelsOnly = checked ? 1 : 0;
	}

	export(): void {
		this.manufacturerService.getManufacturerModelExportData(this.tdsModelsOnly).subscribe(res => {
			const data = res.body;
			if (this.data && this.data.modelsOnly) {
				const modelsToExport = this.mapModels(data && data.models);
				this.dataToExport$.next([modelsToExport]);
				setTimeout(() => {
					const options = this.excelExportComponents.toArray().map(c => c.workbookOptions());
					Promise.all(options).then(workBooks => {
						workBooks[0].sheets[0].name = SHEET_NAMES.MODELS;
						this.excelExportComponents.first.save(workBooks[0]);
					})
				}, 500);
			} else {
				const connectorsToExport = this.mapConnectors(data && data.connectors);
				const modelsToExport = this.mapModels(data && data.models);
				const manufacturersToExport = data.manufacturers;
				this.dataToExport$.next([manufacturersToExport, modelsToExport, connectorsToExport]);
				setTimeout(() => {
					const options = this.excelExportComponents.toArray().map(c => c.workbookOptions());
					Promise.all(options).then(workBooks => {
						for (let i = 0; i < options.length - 1; i++) {
							workBooks[0].sheets = workBooks[0].sheets.concat(workBooks[i + 1].sheets);
						}
						workBooks[0].sheets[0].name = SHEET_NAMES.MANUFACTURERS;
						workBooks[0].sheets[1].name = SHEET_NAMES.MODELS;
						workBooks[0].sheets[2].name = SHEET_NAMES.CONNECTORS;
						this.excelExportComponents.first.save(workBooks[0]);
					})
				}, 500);
			}
		});
	}

	mapConnectors(connectors: IConnector[]): any[] {
		return connectors.map((c: IConnector) => {
			const modelId = c.model && c.model.id;
			const modelName = c.model && c.model.name;
			delete c.model;
			return {
				...c,
				modelId,
				modelName
			};
		});
	}

	mapModels(models: IManufacturerModel[]): any[] {
		return models.map((c: IManufacturerModel) => {
			const manufacturerId = c.manufacturer && c.manufacturer.id;
			const manufacturerName = c.manufacturer && c.manufacturer.name;
			delete c.manufacturer;
			return {
				...c,
				manufacturerId,
				manufacturerName
			};
		});
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		super.onCancelClose();
	}
}