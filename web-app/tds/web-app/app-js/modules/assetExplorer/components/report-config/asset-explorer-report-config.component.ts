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
		imp: 'all',
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

	protected onLoadAssetFields(): void {
		this.selectedAssetClasses = Object.keys(this.filterModel.assets)
			.filter(key => this.filterModel.assets[key]);
	}
	protected ApplyFilters(): void {
		this.filteredData = this.domains.slice()
			.filter(x => this.selectedAssetClasses.indexOf(x.domain.toUpperCase()) !== -1);
		this.ApplyAssetClassFilter();
		this.ApplyFieldFilter();
	}

	protected ApplyAssetClassFilter(): void {
		if (this.filterModel.asset !== 'all') {
			this.filteredData = this.filteredData.filter(domain => domain.domain === this.filterModel.asset);
		}
	}

	protected ApplyFieldFilter(): void {
		if (this.filterModel.search !== '') {
			this.filteredData.forEach(domain => {
				domain.fields = domain.fields.filter(field =>
					field.label.indexOf(this.filterModel.search) !== -1 ||
					field.field.indexOf(this.filterModel.search) !== -1);
			});
		}
	}

	protected onSelectToggle() {
		let selected = 0;
		let total = 0;
		this.filteredData.forEach(domain => {
			total += domain.fields.length;
			selected += domain.fields.filter((field: any) => field.selected).length;
		});
		let uncheck: boolean = selected !== 0 && selected === total;

		this.filteredData.forEach(domain => {
			domain.fields.forEach((field: any) => {
				field.selected = !uncheck;
			});
		});

	}

	protected toggleClass() {
		let selected = 0;
		let total = 0;

		this.filteredData.forEach(domain => {
			total += domain.fields.length;
			selected += domain.fields.filter((field: any) => field.selected).length;
		});

		return {
			'fa-square-o': selected === 0,
			'fa-plus-square-o': selected !== 0 && selected !== total,
			'fa-check-square-o': selected !== 0 && selected === total
		};
	}
}