import {
	Component,
	Inject,
	ViewChild,
	AfterViewInit,
	Input,
	Output,
	ViewEncapsulation,
	EventEmitter,
	ElementRef
} from '@angular/core';
import {Observable} from 'rxjs';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';

import {ViewGroupModel} from '../../model/view.model';
import {ViewType} from '../../model/view.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';

import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {Router} from '@angular/router';

@Component({
	selector: 'asset-explorer-view-selector',
	exportAs: 'assetExplorerViewSelector',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-selector/asset-explorer-view-selector.component.html',
	encapsulation: ViewEncapsulation.None,
	styles: [
			`ul.k-list .k-item.k-state-selected, ul.k-list .k-item.k-state-selected:hover {
            color: #656565;
            background-color: #ededed;
        }`
	]
})
export class AssetExplorerViewSelectorComponent implements AfterViewInit {
	@Input() open ? = false;
	@Input() nameAsUrl ? = true;
	@Input() showCreate ? = true;
	@Input() isDisabled ? = false;
	@Output() onSelectView = new EventEmitter<any>();
	@ViewChild('kendoDropDown') dropdown: DropDownListComponent;
	@ViewChild('viewSelectorFilter') viewSelectorFilter: ElementRef;
	private reports: ViewGroupModel[];
	public data: ViewGroupModel[];
	private searchFilterSelector = '';
	public defaultItem = {
		name: 'Saved Views',
		default: true
	};
	public selectedItem = '';

	constructor(
		private router: Router,
		private service: AssetExplorerService,
		private permissionService: PermissionService) {
		service.getReports().subscribe((result) => {
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
		setTimeout(() => {
			this.dropdown.toggle(!this.dropdown.isOpen)
			setTimeout( () => {
				if (this.dropdown.isOpen && this.viewSelectorFilter) {
					this.viewSelectorFilter.nativeElement.focus();
				}
			}, 300);
		});
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
			// TODO: STATE SERVICE GO
			// this.stateService.go(AssetExplorerStates.REPORT_CREATE.name,
			// 	{system: item.type === ViewType.SYSTEM_VIEWS});
			this.router.navigate(['asset', 'views', 'create']);
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

	public loadData() {
		this.service.getReports()
			.subscribe(result => {
				this.data = result as ViewGroupModel[];
			});
	}

	protected onFocusOut($event): void {
		if (this.dropdown.isOpen) {
			this.selectedItem = this.defaultItem.name;
			this.onToggle();
		}
	}
}
