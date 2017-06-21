import { Component, Inject, ViewChild, AfterViewInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';

import { ReportGroupModel } from '../../model/report-group.model';

@Component({
	moduleId: module.id,
	selector: 'asset-explorer-report-selector',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-selector/asset-explorer-report-selector.component.html'
})
export class AssetExplorerReportSelectorComponent implements AfterViewInit {
	@ViewChild('kendoDropDown') dropdown: DropDownListComponent;
	private reports: ReportGroupModel[];
	private data: ReportGroupModel[];
	private search = '';

	constructor( @Inject('reports') reports: Observable<ReportGroupModel[]>) {
		reports.subscribe((result) => {
			this.data = result;
			this.reports = result.slice();
		});
	}

	ngAfterViewInit(): void {
		this.dropdown.toggle(true);
	}

	protected onClose(e): void {
		e.prevented = true;
	}

	protected onSearch(): void {
		let regex = new RegExp(this.search, 'i');
		this.data = this.reports.map((reportGrp) => {
			let item = { ...reportGrp };
			item.items = item.items.filter(report => regex.test(report.name));
			return item;
		});
	}

	protected getFolderStyle(item: ReportGroupModel) {
		return {
			'fa-folder': item.items.length === 0,
			'fa-folder-o': item.items.length !== 0 && !item.open,
			'fa-folder-open-o': item.items.length !== 0 && item.open
		};
	}

	protected onFolderClick(item: ReportGroupModel) {
		item.open = !item.open;
	}
}