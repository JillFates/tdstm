import {Component, Inject, ViewChild, OnInit, AfterViewInit, Input, Output, ViewEncapsulation, EventEmitter} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {StateService} from '@uirouter/angular';

import {ViewGroupModel} from '../../model/view.model';
import {ViewType} from '../../model/view.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';

import {AssetExplorerService} from '../../service/asset-explorer.service';
import {AssetExplorerStates} from '../../asset-explorer-routing.states';

@Component({
	selector: 'asset-explorer-view-selector',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-selector/asset-explorer-view-selector.component.html',
	encapsulation: ViewEncapsulation.None,
	styles: [
			`ul.k-list .k-item.k-state-selected, ul.k-list .k-item.k-state-selected:hover {
            color: #656565;
            background-color: #ededed;
        }`
	]
})
export class AssetExplorerViewSelectorComponent implements OnInit, AfterViewInit {
	@Input() open? = false;
	@Input() nameAsUrl? = true;
	@Input() showCreate? = true;
	@Input() isDisabled? = false;
	@Output() onSelectView = new EventEmitter<any>();
	@ViewChild('kendoDropDown') dropdown: DropDownListComponent;
	private reports: ViewGroupModel[];
	private data: ViewGroupModel[];
	private searchFilterSelector = '';
	public defaultItem = {
		name: 'Saved Views',
		default: true
	};
	public selectedItem = '';

	constructor(private service: AssetExplorerService, private stateService: StateService, private permissionService: PermissionService) {
	}

	ngOnInit(): void {
		this.service.getReports()
			.subscribe((result) => {
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
		let regex = new RegExp(this.searchFilterSelector, 'i');
		this.data = this.reports.map((reportGrp) => {
			let item = {...reportGrp};
			item.items = item.items.filter(report => regex.test(report.name));
			return item;
		});
	}

	protected getFolderStyle(item: ViewGroupModel) {
		return {
			'fa-star text-yellow': item.type === ViewType.FAVORITES,
			'fa-folder': item.type !== ViewType.FAVORITES
		};
	}

	protected onFolderClick(item: ViewGroupModel) {
		item.open = !item.open;
	}

	/**
	 * Create a new Report
	 * @param {ViewGroupModel} item
	 */
	protected onCreateNew(item: ViewGroupModel): void {
		if (this.isCreateAvailable(item)) {
			this.stateService.go(AssetExplorerStates.REPORT_CREATE.name,
				{system: item.type === ViewType.SYSTEM_VIEWS});
		}
	}

	/**
	 * Show/Hide the Create Button if not in All / Favorites / Recent
	 * @returns {boolean}
	 */
	protected isCreateVisible(item: ViewGroupModel): boolean {
		return (this.showCreate && item.type !== ViewType.ALL &&
			item.type !== ViewType.FAVORITES &&
			item.type !== ViewType.RECENT) && this.isCreateAvailable(item);
	}

	/**
	 * Disable the Create View if the user does not have the proper permission
	 * @returns {boolean}
	 */
	protected isCreateAvailable(item: ViewGroupModel): boolean {
		return item.type === ViewType.SYSTEM_VIEWS ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate) :
			this.permissionService.hasPermission(Permission.AssetExplorerCreate);

	}

	/**
	 * When select the value, changes the title of the Select to reflect the selected item.
	 * @param item
	 */
	protected onSelectItem(parent: any, item: any): void {
		this.selectedItem = parent.name + ' > ' + item.name;
		this.onSelectView.next({ id: item.id, name: item.name});
		this.onToggle();
	}
}