import { Component, Inject, ViewChild, AfterViewInit, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';
import { StateService } from '@uirouter/angular';

import { ReportGroupModel } from '../../model/report-group.model';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';
@Component({
	moduleId: module.id,
	selector: 'asset-explorer-report-selector',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-selector/asset-explorer-report-selector.component.html'
})
export class AssetExplorerReportSelectorComponent implements AfterViewInit {
	@Input() open?= false;
	@Input('create-new') createNewLink?= true;
	@ViewChild('kendoDropDown') dropdown: DropDownListComponent;
	private reports: ReportGroupModel[];
	private data: ReportGroupModel[];
	private search = '';

	constructor( @Inject('reports') reports: Observable<ReportGroupModel[]>, private stateService: StateService) {
		reports.subscribe((result) => {
			this.data = result;
			this.reports = result.slice();
		});
	}

	ngAfterViewInit(): void {
		if (this.open) {
			this.dropdown.toggle(true);
		}
	}

	protected onAction(e): void {
		e.prevented = true;
	}

	protected onToggle() {
		setTimeout(() => this.dropdown.toggle(!this.dropdown.isOpen));
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

	protected onCreateNew(): void {
		this.stateService.go(AssetExplorerStates.REPORT_CREATE.name);
	}
}