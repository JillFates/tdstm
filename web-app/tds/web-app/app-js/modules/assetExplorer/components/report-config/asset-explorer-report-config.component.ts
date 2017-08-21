import { Component, Inject } from '@angular/core';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { FieldSettingsModel } from '../../../fieldSettings/model/field-settings.model';
import { StateService } from '@uirouter/angular';
import { Observable } from 'rxjs/Rx';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportModel } from '../../model/report.model';
import { ReportSpec, ReportColumn } from '../../model/report-spec.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { AssetExplorerReportSaveComponent } from '../report-save/asset-explorer-report-save.component';
import { AssetExplorerReportExportComponent } from '../report-export/asset-explorer-report-export.component';
import { Permission } from '../../../../shared/model/permission.model';

@Component({
	selector: 'asset-explorer-report-config',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-config/asset-explorer-report-config.component.html',
	styles: [`
		.pd-top-5 { padding-top:5px; }
		.disabled { border-top-color: #777 !important;}
		.disabled a { color:#777 !important; }
		.C { background-color: #F9FF90;}
        .I { background-color: #D4F8D4;}
        .N { background-color: #FFF;}
		.U { background-color: #F3F4F6;}
		li.active a { font-weight:bold;}
	`]
})
export class AssetExplorerReportConfigComponent {

	private dataSignature: string;
	// There will be more custom classes, but this are the list who has already an Icon
	assetClasses = ['APPLICATION', 'DEVICE', 'DATABASE', 'STORAGE'];
	filterModel = {
		assets: {
			'APPLICATION': false,
			'DATABASE': false,
			'DEVICE': false,
			'STORAGE': false
		},
		asset: 'all',
		selected: 'all',
		search: ''
	};
	collapsed = false;
	collapsedColumnsPreview = false;
	columnIndex = 0;
	rowIndex = 0;

	model: ReportModel;
	domains: DomainModel[] = [];
	filteredData: DomainModel[] = [];
	fields: FieldSettingsModel[] = [];
	position: any[] = [];
	constructor(
		@Inject('report') report: Observable<ReportModel>,
		private assetExpService: AssetExplorerService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private state: StateService,
		@Inject('fields') fields: Observable<DomainModel[]>) {
		Observable.zip(fields, report).subscribe((result: [DomainModel[], ReportModel]) => {
			this.domains = result[0];
			this.model = { ...result[1] };
			this.dataSignature = JSON.stringify(this.model);
			if (this.model.id !== 0) {
				this.updateFilterbyModel();
			}
		}, (err) => console.log(err));
	}

	protected updateFilterbyModel() {
		this.model.schema.domains.forEach((domain: string) => this.filterModel.assets[domain] = true);
		this.applyFilters();
	}

	protected updateModelbyFilter() {
		this.applyFilters();
		this.model.schema.domains = this.selectedAssetClasses();
		this.model.schema.columns = this.filteredData
			.map((d) => d.fields
				.filter((f) => f['selected'])
				.map((f) => {
					return {
						domain: d.domain,
						property: f.field,
						width: 50,
						locked: false,
						edit: false,
						label: f.label
					};
				}))
			.reduce((p, c) => p.concat(c), []);
	}

	/** Filter Methods */

	protected applyFilters(): void {
		this.applyAssetSelectFilter();
		this.applyAssetClassFilter();
		this.applyFieldFilter();
		this.applySelectedFilter();
		this.fields = this.mapFieldList();
		this.columnIndex = 0;
		this.rowIndex = 0;
		this.position = this.fields.map(x => this.fieldStyle(x['isTitle']));
	}

	protected applyAssetSelectFilter(): void {
		let classes = this.selectedAssetClasses();
		this.filteredData = this.domains
			.filter(x => classes.indexOf(x.domain.toUpperCase()) !== -1)
			.map(domain => {
				return { ...domain };
			});
	}

	protected applyAssetClassFilter(): void {
		if (this.filterModel.asset !== 'all') {
			this.filteredData = this.filteredData.filter(domain => domain.domain.toUpperCase() === this.filterModel.asset.toUpperCase());
		}
	}

	protected applyFieldFilter(): void {
		if (this.filterModel.search !== '') {
			let regx = new RegExp(this.filterModel.search, 'i');
			this.filteredData.forEach(domain => {
				domain.fields = domain.fields.filter(field =>
					regx.test(field.label) || regx.test(field.field));
			});
		}
	}

	protected applySelectedFilter(): void {
		if (this.filterModel.selected !== 'all') {
			this.filteredData.forEach(domain => {
				domain.fields = domain.fields.filter(field =>
					this.filterModel.selected === 'true' ? field['selected'] : !field['selected']);
			});
		}
	}

	/**Miscellaneus Methods */
	private mapFieldList(): FieldSettingsModel[] {
		return this.filteredData
			.reduce((p: FieldSettingsModel[], c: DomainModel) => {
				if (c.fields.length > 0) {
					let domainTitle = new FieldSettingsModel();
					domainTitle['domain'] = c.domain;
					domainTitle['isTitle'] = true;
					p.push(domainTitle);
				}
				return p.concat(c.fields);
			}, []);
	}

	protected selectedAssetClasses(): string[] {
		return Object.keys(this.filterModel.assets).filter(key => this.filterModel.assets[key]);
	}

	protected fieldStyle(isTitle: boolean): any {
		if (isTitle && this.columnIndex !== 0) {
			this.columnIndex += 1;
			this.rowIndex = 0;
		} else if (this.rowIndex >= 15) {
			this.columnIndex += 1;
			this.rowIndex = 0;
		}
		let result = {
			'height': '25px',
			'width': '160px',
			'top': this.rowIndex * 25 + 'px',
			'left': this.columnIndex * 160 + 'px',
			'position': 'absolute',
			'margin': '0px',
			'padding-left': '10px',
			'padding-bottom': '5px'
		};
		this.rowIndex += 1;
		return result;
	}

	protected openSaveDialog(): void {
		this.dialogService.open(AssetExplorerReportSaveComponent, [
			{ provide: ReportModel, useValue: this.model }
		]).then(result => {
			console.log(result);
			this.model = result;
			this.dataSignature = JSON.stringify(this.model);
			setTimeout(() => {
				this.state.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
				console.log('here');
			});
		}).catch(result => {
			console.log('error');
		});
	}

	/** Validation and Permission Methods */

	protected isAssetSelected(): boolean {
		return this.model.schema.domains.length > 0;
	}

	protected isColumnSelected(): boolean {
		return this.model.schema.columns.length > 0;
	}

	protected isValid(): boolean {
		return this.isAssetSelected() && this.isColumnSelected();
	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.model);
		if (this.state && this.state.$current && this.state.$current.data) {
			this.state.$current.data.hasPendingChanges = result;
		}
		return result;
	}

	protected isSaveAvailable(): boolean {
		return this.model.id ?
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
				this.model.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerEdit) :
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) :
				this.model.isOwner && this.permissionService.hasPermission(Permission.AssetExplorerCreate);
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.id ?
			this.model.isSystem ?
				this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
				this.permissionService.hasPermission(Permission.AssetExplorerSaveAs) :
			this.isSaveAvailable();
	}

	protected isSystemSaveAvailable(edit): boolean {
		return edit ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs);
	}

	/**
	 * Get the Icon of the Asset class, if does not exist use the Other generic icon
	 * @param {string} assetName
	 * @returns {string}
	 */
	protected getAssetIcon(assetName: string): string {
		let validAssetIconClass = this.assetClasses.find((r) => r === assetName);
		if (!validAssetIconClass) {
			validAssetIconClass = 'other';
		}
		return validAssetIconClass.toLocaleLowerCase() + '_menu';
	}

	/** Dialog and view Actions methods */

	protected onSaveAs(): void {
		if (this.isSaveAsAvailable()) {
			this.openSaveDialog();
		}
	}

	protected onSave() {
		if (this.isSaveAvailable()) {
			if (this.model.id) {
				this.assetExpService.saveReport(this.model);
			} else {
				this.openSaveDialog();
			}
		}
	}

	protected onExport(): void {
		this.dialogService.open(AssetExplorerReportExportComponent, [
			{ provide: ReportModel, useValue: this.model }
		]);
	}

	protected onFavorite() {
		// this.model.favorite = !this.model.favorite;
		console.log('no empty');
	}

	protected onBackToStart(): void {
		this.state.go(AssetExplorerStates.REPORT_SELECTOR.name);
	}

	protected onPreview(): void {
		this.model.schema.columns = this.filteredData
			.map((d) => d.fields
				.filter((f) => f['selected'])
				.map((f) => {
					return {
						domain: d.domain,
						property: f.field,
						width: 50,
						locked: false,
						edit: false,
						label: f.label
					};
				}))
			.reduce((p, c) => p.concat(c), []);
	}

	protected onClearTextFilter() {
		this.filterModel.search = '';
		this.applyFilters();
	}

	protected onToggleConfig(): void {
		this.collapsed = !this.collapsed;
	}

	protected onToggleSelectedColumnsPreview(): void {
		this.collapsedColumnsPreview = !this.collapsedColumnsPreview;
	}
}