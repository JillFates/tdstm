import { Component, Inject } from '@angular/core';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { StateService } from '@uirouter/angular';

import { Observable } from 'rxjs/Rx';

import { AssetExplorerStates } from '../../asset-explorer-routing.states';
import { ReportModel } from '../../model/report.model';
import { AssetExplorerReportSaveComponent } from '../report-save/asset-explorer-report-save.component';
import { AssetExplorerReportExportComponent } from '../report-export/asset-explorer-report-export.component';

@Component({
	moduleId: module.id,
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
	`]
})
export class AssetExplorerReportConfigComponent {

	assetClasses = ['APPLICATION', 'DEVICE', 'DATABASE', 'STORAGE'];
	selectedAssetClasses = [];
	model: ReportModel;
	domains: DomainModel[] = [];
	filteredData: DomainModel[] = [];
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

	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private stateService: StateService,
		@Inject('fields') fields: Observable<DomainModel[]>) {
		this.model = new ReportModel();
		fields.subscribe((result) => {
			this.domains = result;
		}, (err) => console.log(err));
	}

	protected onSaveAs(): void {
		this.dialogService.open(AssetExplorerReportSaveComponent, [
			{ provide: ReportModel, useValue: this.model }
		]);
	}

	protected onExport(): void {
		this.dialogService.open(AssetExplorerReportExportComponent, [
			{ provide: ReportModel, useValue: this.model }
		]);
	}

	protected onFavorite() {
		this.model.favorite = !this.model.favorite;
	}

	protected onBackToStart(): void {
		this.stateService.go(AssetExplorerStates.REPORT_SELECTOR.name);
	}

	protected isAssetSelected(): boolean {
		return Object.keys(this.filterModel.assets)
			.filter(key => this.filterModel.assets[key]).length !== 0;
	}

	protected onAssetSelect(): void {
		this.selectedAssetClasses = Object.keys(this.filterModel.assets)
			.filter(key => this.filterModel.assets[key]);
		this.filteredData = this.domains
			.filter(x => this.selectedAssetClasses.indexOf(x.domain.toUpperCase()) !== -1)
			.map(domain => {
				return { ...domain };
			});
	}

	protected ApplyFilters(): void {
		this.onAssetSelect();
		this.applyAssetClassFilter();
		this.applyFieldFilter();
		this.applySelectedFilter();
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

	protected onClearTextFilter() {
		this.filterModel.search = '';
		this.ApplyFilters();
	}

	protected applyAssetClassFilter(): void {
		if (this.filterModel.asset !== 'all') {
			this.filteredData = this.filteredData.filter(domain => domain.domain.toUpperCase() === this.filterModel.asset.toUpperCase());
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

}