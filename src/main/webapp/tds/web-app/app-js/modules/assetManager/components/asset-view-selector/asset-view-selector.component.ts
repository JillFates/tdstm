import {
	Component,
	ViewChild,
	AfterViewInit,
	Input,
	Output,
	ViewEncapsulation,
	EventEmitter,
	ElementRef
} from '@angular/core';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';

import {ViewGroupModel} from '../../../assetExplorer/model/view.model';
import {ViewType} from '../../../assetExplorer/model/view.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Permission} from '../../../../shared/model/permission.model';

import {AssetExplorerService} from '../../service/asset-explorer.service';
import {Router} from '@angular/router';

@Component({
	selector: 'tds-asset-view-selector',
	exportAs: 'tdsAssetViewSelector',
	template: `
        <kendo-dropdownlist #kendoDropDown="kendoDropDownList"
                            [defaultItem]="defaultItem"
                            [data]="data"
                            [disabled]="isDisabled"
                            (close)="onAction($event)"
                            (open)="onAction($event)"
                            [valueField]="'name'"
                            (click)="onToggle()"
                            [textField]="'name'"
                            class="asset-explorer-view-selector-component"
                            style="width: 266px;">
            <ng-template kendoDropDownListValueTemplate let-dataItem>
		<span style="cursor:pointer;width:100%;">
			<div *ngIf="selectedItem !== ''; else noneSelectedItem">
				{{selectedItem}}
			</div>
			<ng-template #noneSelectedItem>
				{{dataItem.name}}
			</ng-template>
		</span>
            </ng-template>
            <ng-template kendoDropDownListHeaderTemplate>
                <div class="has-feedback" style="margin-top:-20px;">
                    <input #viewSelectorFilter
                           (focusout)="onFocusOut()"
                           type="text"
                           class="form-control"
                           (keyup)="onSearch()"
                           name="searchFilterSelector"
                           [(ngModel)]='searchFilterSelector'
                           placeholder="Search"
                           aria-describedby="search">
                    <i class="fa fa-search form-control-feedback" aria-hidden="true"></i>
                </div>
            </ng-template>
            <ng-template kendoDropDownListItemTemplate let-dataItem>
                <div class="container" *ngIf="!dataItem.default; else default">
                    <div class="row" (click)="onFolderClick(dataItem)">
                        <i class="fa" [ngClass]="getFolderStyle(dataItem)"></i> {{dataItem.name}}
                        <span class="label label-primary pull-right">{{dataItem.items.length}}</span>
                    </div>
                    <div class="row" style="margin-top:5px;" *ngIf="dataItem.open">
                        <li *ngIf="isCreateVisible(dataItem)">
                            <a (click)="onCreateNew(dataItem)" class="btn"><i class="fa fa-plus-square"></i> Create New</a>
                        </li>
                        <li>
                            <ul style="padding-left:10px;margin-top:5px;word-wrap:break-word" *ngFor="let value of dataItem.items">
                                <a [routerLink]="['/asset','views',value.id,'show']" (click)="onFocusOut()">
                                    <i class="fa fa-file-text-o"></i> {{value.name}}</a>
                            </ul>
                        </li>
                    </div>
                </div>
                <ng-template #default>
                </ng-template>
            </ng-template>
        </kendo-dropdownlist>
	`,
	encapsulation: ViewEncapsulation.None,
	styles: [
			`ul.k-list .k-item.k-state-selected, ul.k-list .k-item.k-state-selected:hover {
            color: #656565;
            background-color: #ededed;
        }`
	]
})
export class AssetViewSelectorComponent implements AfterViewInit {
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
