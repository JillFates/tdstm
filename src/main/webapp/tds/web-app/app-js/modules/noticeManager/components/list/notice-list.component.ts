// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Components
import {NoticeViewEditComponent} from '../view-edit/notice-view-edit.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {WindowService} from '../../../../shared/services/window.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {NoticeService} from '../../service/notice.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {GridColumnModel} from '../../../../shared/model/data-list-grid.model';
// Model
import {Permission} from '../../../../shared/model/permission.model';
import {NoticeColumnModel, NoticeModel, Notices, NoticeTypes,
		NOTICE_TYPE_PRE_LOGIN, NOTICE_TYPE_POST_LOGIN, PostNoticeResponse, NOTICE_TYPE_MANDATORY} from '../../model/notice.model';
import {ActionType} from '../../../../shared/model/action-type.enum';
import {YesNoList} from '../../../../shared/model/constants';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
// Kendo
import {GridDataResult, CellClickEvent} from '@progress/kendo-angular-grid';
import {process, State, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {PreferenceService} from '../../../../shared/services/preference.service';

declare var jQuery: any;

@Component({
	selector: 'tds-notice-list',
	templateUrl: 'notice-list.component.html'
})

export class NoticeListComponent implements OnInit {

	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}],
		filter: {
			filters: [],
			logic: 'and'
		}
	};
	protected skip = 0;
	protected pageSize = GRID_DEFAULT_PAGE_SIZE;
	protected maxOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	protected noticeColumnModel = null;
	protected COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	protected noticeTypes = [];
	protected yesNoList = [...YesNoList];
	protected defaultNoticeType = {typeId: '', name: 'Please Select'};
	protected defaultYesNoList = {value: null, name: 'Please Select'};

	protected PRE_LOGIN = NOTICE_TYPE_PRE_LOGIN;
	protected actionType = ActionType;
	private gridData: GridDataResult;
	protected resultSet: any[];
	protected dateFormat = '';
	protected notices = [];
	protected noticesTypeDescriptions = {};

	/**
	 * @constructor
	 * @param {NoticeService} noticeService
	 */
	constructor(
		private notifier: NotifierService,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private noticeService: NoticeService,
		private prompt: UIPromptService,
		private route: ActivatedRoute,
		private windowService: WindowService) {
		this.resultSet = this.route.snapshot.data['notices'];
		this.updateGrid();
		this.noticeTypes = [...NoticeTypes];
	}

	/**
	 * Get the notices list, the date format and based on it creates the column model
	 */
	ngOnInit() {
		this.notices = this.noticeTypes
			.map((notice) => notice.name);

		// Get the description text of the notices types
		NoticeTypes.forEach((notice: any) => {
			this.noticesTypeDescriptions[notice.typeId] = notice.name;
		});

		// Get the preferences to format dates
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
		.subscribe((dateFormat) => {
			this.dateFormat = dateFormat;
			this.noticeColumnModel = new NoticeColumnModel(`{0:${dateFormat}}`);
		});
	}

	/**
	 * Refresh the data grid info and update the grid heigth
	 */
	private updateGrid(): void {
		this.gridData = process(this.resultSet, this.state);
		this.fixGridHeight();
	}

	/**
	 * Notify the event to update the grid height
	 */
	private fixGridHeight(): void {
		this.notifier.broadcast({
			name: 'grid.header.position.change'
		});
		// when dealing with locked columns Kendo grid fails to update the height, leaving a lot of empty space
		jQuery('.k-grid-content-locked').addClass('element-height-100-per-i');
	}

	/**
	 *  On filter change grab the current filter value and process the notices list
	 */
	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.updateGrid();
	}

	/**
	 *  On sort change grab the current sort value and process the notices list
	 */
	protected sortChange(sort): void {
		this.state.sort = sort;
		this.updateGrid();
	}

	/**
	 * Get the reference to the column that was filtered and throws the filter event
	*/
	protected onFilter(column: any): void {
		const root = GridColumnModel.filterColumn(column, this.state);
		this.filterChange(root);
	}

	/**
	 * Clear the column filter value currently selected
	 * @param {Any} column Column to clear out filter
	*/
	protected clearText(column: any): void {
		this.noticeService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	/**
	 * Reset the filter type to the original value
	 * @param {Any} column Column to reset the filter
	*/
	protected resetTypeFilter(column: any): void {
		this.clearText(column);
		column.filter = null;
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0 && this.isEditAvailable()) {
			this.openNotice(event['dataItem'], ActionType.View);
		}
	}

	/**
	 * Make the entire header clickable on Grid
	 * @param event: any
	 */
	public onClickTemplate(event: any): void {
		if (event.target && event.target.parentNode) {
			event.target.parentNode.click();
		}
	}

	/**
	 * Delete the selected License
	 * @param dataItem
	 */
	protected onDelete(dataItem: any): void {
		this.prompt.open('Confirmation Required', 'You are about to delete the selected notice. Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.noticeService.deleteNotice(dataItem.id).subscribe(
						(result) => {
							this.reloadData();
						},
						(err) => console.log(err));
				}
			});
	}

	/**
	 * Reload the list with the latest created/edited license
	 */
	protected reloadData(): void {
		this.noticeService.getNoticesList()
			.subscribe(
			(result: any) => {
				this.resultSet = result;
				this.updateGrid();
			},
			(err) => console.log(err));
	}

	/**
	 * Manage Pagination
	 * @param {PageChangeEvent} event
	 */
	public pageChange(event: any): void {
		this.skip = event.skip;
		this.state.skip = this.skip;
		this.state.take = event.take || this.state.take;
		this.pageSize = this.state.take;
		this.updateGrid();
	}

	/**
	 * Create a new Notice
	 * @listens onCreateNotice
	 */
	public onCreateNotice(): void {
		this.dialogService.open(NoticeViewEditComponent, [
			{provide: NoticeModel, useValue: new NoticeModel()},
			{provide: Number, useValue: ActionType.Create}
		]).then(result => {
			this.reloadData();
		}, error => {
			console.log(error);
		});
	}

	/**
	 * Open a notice view
	 * @param {NoticeModel} dataItem Contain the notice object
	 * @param {ActionType} action Mode in which open the view (Create, Edit, etc...)
	 */
	public openNotice(dataItem: NoticeModel, action: ActionType): void {
		this.noticeService.getNotice(dataItem.id)
			.subscribe((notice: NoticeModel) => {
				this.dialogService.open(NoticeViewEditComponent, [
					{provide: NoticeModel, useValue: notice as NoticeModel},
					{provide: Number, useValue: action}
				]).then(result => {
					this.reloadData();
				}, error => {
					console.error(error);
				});
			})
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeEdit);
	}

	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeCreate);
	}
}