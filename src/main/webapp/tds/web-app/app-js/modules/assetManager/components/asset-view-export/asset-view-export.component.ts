// Angular
import {Component, Input, OnInit, ViewChild} from '@angular/core';
// Model
import {FieldImportance} from '../../../fieldSettings/model/field-settings.model';
import {AssetExportModel} from '../../../assetExplorer/model/asset-export-model';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {Dialog, DialogButtonType} from 'tds-component-library';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {ExcelExportComponent} from '@progress/kendo-angular-excel-export';
import {AssetExplorerService} from '../../service/asset-explorer.service';
import * as R from 'ramda';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {EventsService} from '../../../event/service/events.service';
import {UserContextService} from '../../../auth/service/user-context.service';

@Component({
	selector: 'asset-explorer-view-export',
	template: `
		<form name="exportForm" role="form" data-toggle="validator" #exportForm='ngForm'>
			<div class="box-body">
				<div class="form-group">
					<label for="fileName" class="control-label">File Name </label>
					<input type="text" class="form-control" id="fileName" [(ngModel)]="fileName" name="fileName" required [ngClass]="{'has-error': !exportForm.form.valid}"/>
				</div>
                <div class="form-group">
                    <label for="exportIdCheckbox" class="aligned-label pointer">
                        <input type="checkbox"
                               class="aligned-label pointer"
                               id="exportIdCheckbox"
                               name="exportIdCheckbox"
                               [(ngModel)]="isIdFieldExported">
                        Include ID column
                    </label>
                </div>
			</div>
		</form>
        <kendo-excelexport [data]="dataToExport" fileName="{{exportFileName + '.xlsx'}}" #excelexport>
            <kendo-excelexport-column *ngFor="let column of columns" [field]="column.name" [title]="column.title"
                                      [locked]="column.locked" [cellOptions]="column.cell">
            </kendo-excelexport-column>
        </kendo-excelexport>
	`,
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class AssetViewExportComponent extends Dialog implements OnInit {
	@Input() data: any;

	public columns: any[];
	public fileName = 'asset_explorer';
	public exportFileName = '';
	public dataToExport: any[] = [];
	private allProperties = false;
	private fieldImportance = new FieldImportance();
	public isIdFieldExported: boolean;
	private readonly ID_FIELD_OBJECT = {
		domain: 'common',
		edit: false,
		filter: '',
		label: 'Id',
		locked: false,
		property: 'id',
		width: 210
	};
	@ViewChild('excelexport', {static: false}) public excelexport: ExcelExportComponent;

	public assetExportModel: AssetExportModel;

	constructor(
		private assetExpService: AssetExplorerService,
		private userContextService: UserContextService
	) {
		super();
		this.isIdFieldExported = true;  // by default mark the id as exportable
	}

	ngOnInit(): void {
		this.assetExportModel = R.clone(this.data.assetExportModel);
		this.userContextService.getUserContext().subscribe((userContext: UserContextModel) => {
			this.fileName = userContext.project.name + '-' + this.data.assetExportModel.viewName;
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'export',
			text: 'Export',
			show: () => true,
			type: DialogButtonType.CONTEXT,
			action: this.getExportData.bind(this)
		});

		/*
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
		 */
	}

	/**
	 * Determines if the column domain and property match the id column definition
	 * @param column
	 */
	isIdColumn(column: any): boolean {
		return column.domain ===  this.ID_FIELD_OBJECT.domain && column.property === this.ID_FIELD_OBJECT.property;
	}

	/**
	 * Get the Color Schema for the Selected Field
	 * @param model
	 * @returns {string}
	 */
	private getImportanceColor(model: any): string {
		if (model.notFound) {
			return this.fieldImportance['U'].color;
		}
		let domain = this.assetExportModel.domains.find(r => r.domain.toLocaleLowerCase() === model.domain.toLocaleLowerCase());
		let field = domain.fields.find(r => r.label.toLocaleLowerCase() === model.label.toLocaleLowerCase());
		return this.fieldImportance[field.imp].color;
	}

	/**
	 * Transform the Data information
	 */
	public getExportData(): void {
		this.setColumns();

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
			super.onCancelClose();
		}, 500);
	}

	cancelCloseDialog(): void {
		super.onCancelClose();
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

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
	/**
	 * Get the configured columns
	 * if user choose export the id, add that column if not present
	 * otherwise remove it even is present in the columns view definition
	 */
	private getConfiguredColumns(): any {
		let configuredColumns = [...this.assetExportModel.assetQueryParams.filters.columns];

		// user wants to export the id
		if (this.isIdFieldExported) {
			const hasIdColumn = configuredColumns.find((column: any) => this.isIdColumn(column));
			// if doesn't have the column, add it
			if (!hasIdColumn) {
				// check if the configuration is using locked columns
				// if so, mark the id column as locked
				const hasLockedColumns = configuredColumns.find((column: any) => column.locked);
				this.ID_FIELD_OBJECT.locked = Boolean(hasLockedColumns);
				configuredColumns.unshift(this.ID_FIELD_OBJECT);
			}
		} else {
			// user doesn't want to export id column, so remove it from the results
			configuredColumns = configuredColumns.filter((column: any) => !this.isIdColumn(column))
		}

		return configuredColumns;
	}

	/**
	 * Set the columns to be exported based on the configured columns
	 */
	setColumns(): void {
		this.columns = this.getConfiguredColumns()
			.map((column: any) => {
				return {
					name: this.getPropertyColumnField(column['domain'], '_', column['property']),
					title: column['label'],
					width: column['width'],
					locked: column['locked'],
					cell: {
						background: this.getImportanceColor(column)
					}
				};
			});
	}
}
