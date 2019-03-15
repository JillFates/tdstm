// Angular
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
// Components
import {NoticeViewEditComponent} from '../view-edit/notice-view-edit.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {NoticeService} from '../../service/notice.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
// Model
import {Permission} from '../../../../shared/model/permission.model';
import {
	NoticeColumnModel, NoticeModel, NoticeTypes, PostNoticeModel,
	PostNoticeResponse, StandardNotices
} from '../../model/notice.model';
import {ActionType} from '../../../../shared/model/action-type.enum';
import {GRID_DEFAULT_PAGE_SIZE, GRID_DEFAULT_PAGINATION_OPTIONS} from '../../../../shared/model/constants';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
// Kendo
import {GridDataResult, CellClickEvent} from '@progress/kendo-angular-grid';
import {process, State, CompositeFilterDescriptor} from '@progress/kendo-data-query';
import {APIActionColumnModel} from '../../../apiAction/model/api-action.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {EULAComponent} from '../eula/eula.component';
import {StandardNoticesComponent} from '../standard-notices/standard-notices.component';

@Component({
	selector: 'tds-notice-list',
	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html'
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
	protected defaultPageOptions = GRID_DEFAULT_PAGINATION_OPTIONS;
	protected noticeColumnModel = null;
	protected COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	protected noticeTypes = [{typeId: null, name: ''}].concat(NoticeTypes);
	protected actionType = ActionType;
	private gridData: GridDataResult;
	protected resultSet: any[];
	protected dateFormat: string;
	protected postNotices: PostNoticeModel[] = [];
	protected notices = [];

	/**
	 * @constructor
	 * @param {NoticeService} noticeService
	 */
	constructor(
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private preferenceService: PreferenceService,
		private noticeService: NoticeService,
		private prompt: UIPromptService,
		private route: ActivatedRoute) {
		this.resultSet = this.route.snapshot.data['notices'];
		this.gridData = process(this.resultSet, this.state);
	}

	ngOnInit() {
		this.notices = this.noticeTypes
			.map((notice) => notice.name);
		console.log('The notices are:');
		console.log(this.notices);

		this.preferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				this.dateFormat = dateFormat;
				this.noticeColumnModel = new NoticeColumnModel(dateFormat);
			});

		this.noticeService.getPostNotices()
			.subscribe((response: PostNoticeResponse) => {
				console.log('The post notice are:');
				console.log(response);
				this.postNotices = response.notices;
			});
	}

	protected filterChange(filter: CompositeFilterDescriptor): void {
		this.state.filter = filter;
		this.gridData = process(this.resultSet, this.state);
	}

	protected sortChange(sort): void {
		this.state.sort = sort;
		this.gridData = process(this.resultSet, this.state);
	}

	protected onFilter(column: any): void {
		const root = this.noticeService.filterColumn(column, this.state);
		this.filterChange(root);
	}

	protected clearValue(column: any): void {
		this.noticeService.clearFilter(column, this.state);
		this.filterChange(this.state.filter);
	}

	protected resetTypeFilter(column: any): void {
		this.clearValue(column);
		column.filter = null;
	}

	/**
	 * Catch the Selected Row
	 * @param {SelectionEvent} event
	 */
	protected cellClick(event: CellClickEvent): void {
		if (event.columnIndex > 0) {
			this.openNoticeViewEdit(event['dataItem']);
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
		this.noticeService.getNoticesList().subscribe(
			(result: any) => {
				this.resultSet = result;
				this.gridData = process(this.resultSet, this.state);
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
		this.gridData = process(this.resultSet, this.state);
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
		console.log('Clicked on create notice');
	}

	onShowEULA(): void {
		this.showMandatoryMessages()
			.then((result) => {
				if (result) {
					setTimeout(() => this.showDefaultMessages(), 500);
				}
			});
	}

	async showMandatoryMessages(): Promise<boolean> {
		const noticesMandatory = this.postNotices.filter((notice: PostNoticeModel) =>  notice.needAcknowledgement);
		let keepGoing = true;

		while (keepGoing && noticesMandatory.length) {
			try {
				await this.openDialogWithDelay(noticesMandatory.shift());
			} catch (error) {
				console.log('Error:', error.message || error);
				keepGoing = false;
			}
		}

		return Promise.resolve(keepGoing);
	}

	showDefaultMessages() {
		const notices = this.postNotices.filter((notice: PostNoticeModel) =>  !notice.needAcknowledgement);

		this.dialogService.open(StandardNoticesComponent, [ {provide: StandardNotices, useValue: {notices: notices}}])
			.then((response: any) => {
				console.log('The response is:');
				console.log(response);
			})
			.catch((error) => {
				console.log('The error calling the standard notice is:');
				console.log(error);
			});
	}

	openDialogWithDelay(notice: PostNoticeModel) {
		return new Promise((resolve, reject) => {
			setTimeout(async() => {
				try {
					await this.dialogService.open(EULAComponent, [ {provide: PostNoticeModel, useValue: notice}]);
					return resolve(true);
				} catch (error) {
					return reject(error || 'User cancelled');
				}
			}, 500);
		});
	}

	/**
	 * Edit a Notice
	 * @param {NoticeModel} dataItem
	 */
	public openNoticeViewEdit(dataItem: NoticeModel): void {
		this.dialogService.open(NoticeViewEditComponent, [
			{provide: NoticeModel, useValue: dataItem as NoticeModel},
			{provide: Number, useValue: ActionType.Edit}
		]).then(result => {
			this.reloadData();
		}, error => {
			console.log(error);
		});
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeEdit);
	}

	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeCreate);
	}

}