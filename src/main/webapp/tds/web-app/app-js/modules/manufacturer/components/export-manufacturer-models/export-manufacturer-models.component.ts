import {AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Dialog, DialogButtonType} from 'tds-component-library';
import {ReplaySubject} from 'rxjs';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';

@Component({
	selector: 'tds-export-manufacturer-models',
	template: `
		<div class="export-mfg-models-container">
				<form (ngSubmit)="export()" class="clr-form">
						<div class="clr-row">
								<div class="clr-col-12 clr-align-self-center">
                    <clr-checkbox-wrapper class="inline">
                        <label
                                class="clr-control-label clr-control-label-sm inline"
                                for="tds-models-only">Only TDS models</label>
                        <input
                                clrCheckbox
                                id="tds-models-only"
                                type="checkbox"
                                name="tds-models-only"
                                [(ngModel)]="tdsModelsOnly"
                                (ngModelChange)="updateCheckboxValue($event)"
                        />
                    </clr-checkbox-wrapper>
								</div>
						</div>
						<div class="row">
								<div class="clr-col-12 clr-align-self-center">
                    <label
                            class="clr-control-label clr-control-label-sm inline"
                            for="export-file-name">File Name: </label>
                    <input
                            id="export-file-name"
                            type="text"
                            name="export-file-name"
                            [(ngModel)]="exportFileName"
                            (ngModelChange)="updateCheckboxValue($event)"
                    />
								</div>
						</div>
				</form>
        <kendo-excelexport *ngIf="data" [data]="dataToExport$ | async" fileName="{{exportFileName + '.xlsx'}}" #excelExport>
            <div *ngIf="columns">
                <kendo-excelexport-column *ngFor="let column of columns" [field]="column.name" [title]="column.title"
                                          [locked]="column.locked" [cellOptions]="column.cell">
                </kendo-excelexport-column>
            </div>
        </kendo-excelexport>
		</div>
	`
})
export class ExportManufacturerModelsComponent  extends Dialog implements OnInit, AfterViewInit, OnChanges {
	@Input() data: any;
	@ViewChild('excelExport', { static: false }) excelExport: ExcelExportComponent;
	formData = {
		tdsModelsOnly: 0
	};
	tdsModelsOnly: boolean;
	exportFileName: string;
	dataToExport$: ReplaySubject<any> = new ReplaySubject<any>(1);
	columns: any[] = [];

	constructor() {
		// Import Export constructor
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
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.export.bind(this)
		});
	}

	ngAfterViewInit(): void {
		if (this.data) {
			this.loadAll();
		}
	}

	loadAll(): void {
		this.data.manufacturerExportModel.loadData().then(res => {
			this.dataToExport$.next(res);
		}).catch(err => console.error('Error loading manufacturers: ', err));
		this.columns = this.data.manufacturerExportModel.columnModel;
	}

	ngOnChanges(changes: SimpleChanges): void {
		if (changes) {
			if (changes.data && !changes.firstChange) {
				this.loadAll();
			}
		}
	}

	updateCheckboxValue(checked: any): void {
		this.formData.tdsModelsOnly = checked ? 1 : 0;
	}

	export(): void {
		console.log('Form Data: ');
		console.table(this.formData);
		this.excelExport.save();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		super.onCancelClose();
	}
}