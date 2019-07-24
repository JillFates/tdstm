// Angular
import {Component, OnInit} from '@angular/core';
// Components
import {NoticeViewEditComponent} from '../view-edit/notice-view-edit.component';
// Service
import {PermissionService} from '../../../../shared/services/permission.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {NoticeService} from '../../service/notice.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Model
import {Permission} from '../../../../shared/model/permission.model';
import {NoticeColumnModel, NoticeModel, NoticeTypes, NOTICE_TYPE_PRE_LOGIN } from '../../model/notice.model';
import {ActionType} from '../../../../shared/model/action-type.enum';
import {YesNoList} from '../../../../shared/model/constants';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
// Kendo
import {CellClickEvent} from '@progress/kendo-angular-grid';

@Component({
	selector: 'tds-notice-list',
	templateUrl: 'notice-list.component.html'
})

export class NoticeListComponent implements OnInit {
	protected gridSettings: DataGridOperationsHelper;
	protected noticeColumnModel = null;
	protected COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	protected noticeTypes = [];
	protected yesNoList = [...YesNoList];
	protected defaultNoticeType = {typeId: '', name: 'Please Select'};
	protected defaultYesNoList = {value: null, name: 'Please Select'};

	protected PRE_LOGIN = NOTICE_TYPE_PRE_LOGIN;
	protected actionType = ActionType;
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
		private prompt: UIPromptService) {
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
			this.onLoad();
		});
	}

	/**
	 * Get the notices list and defines the gridSettings helper
	 */
	private onLoad(): void {
		this.noticeService.getNoticesList()
			.subscribe(
			(result: any) => {
				this.gridSettings = new DataGridOperationsHelper(result,
					[{ dir: 'asc', field: 'name'}], // initial sort config.
					{ mode: 'single', checkboxOnly: false}, // selectable config.
					{ useColumn: 'id' } ); // checkbox config.
			},
			(err) => err && console.error(err));
	}

	/**
	 * Reloads the current notices list from grid.
	 */
	private reloadNotices(): void {
		this.noticeService.getNoticesList()
			.subscribe(
			(result: any) => this.gridSettings.reloadData(result),
			(err) => err && console.error(err));
	}

	/**
	 * Reset the filter boolean type to the original value
	 * @param {Any} column Column to reset the filter
	*/
	protected resetBooleanFilter(column: any): void {
		this.gridSettings.clearValue(column);
		column.filter = null;
	}

	/**
	 * On cell click open the notice dialog
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
			.then((confirmation: boolean) => {
				if (confirmation) {
					this.noticeService.deleteNotice(dataItem.id)
					.subscribe(
						(result) => this.reloadNotices(),
						(err) => err && console.error(err)
					);
				}
			});
	}

	/**
	 * Open the dialog to create a new Notice
	 * @listens onCreateNotice
	 */
	public onCreateNotice(): void {
		this.dialogService.open(NoticeViewEditComponent, [
			{provide: NoticeModel, useValue: new NoticeModel()},
			{provide: Number, useValue: ActionType.Create}
		]).then(result => this.reloadNotices(),
			err =>  err && console.error(err)
		);
	}

	/**
	 * Open the dialog with the notice details
	 * @param {NoticeModel} dataItem Contain the notice object
	 * @param {ActionType} action Mode in which open the view (Create, Edit, etc...)
	 */
	public openNotice(dataItem: NoticeModel, action: ActionType): void {
		this.noticeService.getNotice(dataItem.id)
			.subscribe((notice: NoticeModel) => {
				this.dialogService.open(NoticeViewEditComponent, [
					{provide: NoticeModel, useValue: notice as NoticeModel},
					{provide: Number, useValue: action}
				]).then(result => this.reloadNotices(),
					err => err && console.error(err)
				);
			});
	}

	/**
	 * Determine if the user has the permission to edit notices
	*/
	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeEdit);
	}

	/**
	 * Determine if the user has the permission to create notices
	*/
	protected isCreateAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.NoticeCreate);
	}
}
