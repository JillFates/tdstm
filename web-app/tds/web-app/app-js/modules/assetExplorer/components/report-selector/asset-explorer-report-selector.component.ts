import { Component, Inject, ViewChild, AfterViewInit, Input, ViewEncapsulation } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';
import { StateService } from '@uirouter/angular';

import { ReportGroupModel } from '../../model/report.model';
import { ViewType } from '../../model/report.model';
import { PermissionService } from '../../../../shared/services/permission.service';
import { Permission } from '../../../../shared/model/permission.model';

import { AssetExplorerStates } from '../../asset-explorer-routing.states';
@Component({
	selector: 'asset-explorer-report-selector',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/report-selector/asset-explorer-report-selector.component.html',
	encapsulation: ViewEncapsulation.None,
	styles: [
		`ul.k-list .k-item.k-state-selected,ul.k-list .k-item.k-state-selected:hover { color: #656565;  background-color: #ededed;}`
	]
})
export class AssetExplorerReportSelectorComponent implements AfterViewInit {
	@Input() open?= false;
	@ViewChild('kendoDropDown') dropdown: DropDownListComponent;
	private reports: ReportGroupModel[];
	private data: ReportGroupModel[];
	private search = '';

	constructor( @Inject('reports') reports: Observable<ReportGroupModel[]>, private stateService: StateService, private permissionService: PermissionService) {
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
			'fa-star text-yellow': item.type === ViewType.FAVORITES,
			'fa-folder': item.type !== ViewType.FAVORITES
		};
	}

	protected onFolderClick(item: ReportGroupModel) {
		item.open = !item.open;
	}

	/**
	 * Create a new Report
	 * @param {ReportGroupModel} item
	 */
	protected onCreateNew(item: ReportGroupModel): void {
		if (this.isCreateAvailable(item)) {
			this.stateService.go(AssetExplorerStates.REPORT_CREATE.name,
				{ system: item.type === ViewType.SYSTEM_VIEWS });
		}
	}

	/**
	 * Show/Hide the Create Button if not in All / Favorites / Recent
	 * @returns {boolean}
	 */
	protected isCreateVisible(item: ReportGroupModel): boolean {
		return (item.type !== ViewType.ALL &&
			item.type !== ViewType.FAVORITES &&
			item.type !== ViewType.RECENT) && this.isCreateAvailable(item);
	}

	/**
	 * Disable the Create Report if the user does not have the proper permission
	 * @returns {boolean}
	 */
	protected isCreateAvailable(item: ReportGroupModel): boolean {
		return item.type === ViewType.SYSTEM_VIEWS ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) :
			this.permissionService.hasPermission(Permission.AssetExplorerCreate);

	}
}