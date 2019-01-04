// import { Component, ViewChild, Inject, AfterViewInit, ViewEncapsulation } from '@angular/core';
// import { NoticeService } from '../../service/notice.service';
// import { NoticeModel } from '../../model/notice.model';
// import { NoticeFormComponent } from '../form/notice-form.component';
//
// import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
// import { ActionType } from '../../../../shared/model/action-type.enum';
// import { PermissionService } from '../../../../shared/services/permission.service';
// import { Permission } from '../../../../shared/model/permission.model';
//
// import { GridDataResult, DataStateChangeEvent } from '@progress/kendo-angular-grid';
// import { process, State, FilterDescriptor } from '@progress/kendo-data-query';
//
// @Component({
// 	selector: 'notice-list',
// 	encapsulation: ViewEncapsulation.None,
// 	templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html'
// })
//
// export class NoticeListComponent {
//
// 	private moduleName = '';
// 	private title = '';
// 	private noticeList: NoticeModel[];
// 	private gridData: GridDataResult;
// 	private state: State = {
// 		skip: 0,
// 		take: 10,
// 		sort: [{
// 			dir: 'asc',
// 			field: 'title'
// 		}],
// 		filter: {
// 			filters: [
// 				{ field: 'active', operator: 'eq', value: false }
// 			],
// 			logic: 'and'
// 		}
// 	};
// 	private defaultItem: any = {
// 		typeId: null, name: 'Select a Type'
// 	};
// 	private typeDataSource: Array<any> = [
// 		{ typeId: 1, name: 'Prelogin' },
// 		{ typeId: 2, name: 'Postlogin' }
// 	];
//
// 	/**
// 	 * @constructor
// 	 * @param {NoticeService} noticeService
// 	 */
// 	constructor(
// 		@Inject('notices') notices,
// 		private noticeService: NoticeService,
// 		private dialogService: UIDialogService,
// 		private permissionService: PermissionService) {
//
// 		this.moduleName = 'Notice List';
// 		notices.subscribe(
// 			(noticeList) => this.onLoadNoticeList(noticeList),
// 			(err) => this.onLoadNoticeList([]));
// 	}
//
// 	/**
// 	 * Get the Initial Notice List
// 	 * @param noticeList
// 	 */
// 	private onLoadNoticeList(noticeList): void {
// 		this.noticeList = noticeList.notices as NoticeModel[];
// 		this.gridData = process(this.noticeList, this.state);
// 	}
//
// 	private getNoticeList(): void {
// 		this.noticeService.getNoticesList().subscribe(
// 			(noticeList) => this.onLoadNoticeList(noticeList),
// 			(err) => this.onLoadNoticeList([]));
// 	}
//
// 	public reloadNoticeList(): void {
// 		this.getNoticeList();
// 	}
//
// 	/**
// 	 * Create a new Notice
// 	 * @listens onCreateNotice
// 	 */
// 	public onCreateNotice(): void {
// 		this.dialogService.open(NoticeFormComponent, [
// 			{ provide: NoticeModel, useValue: new NoticeModel() },
// 			{ provide: Number, useValue: ActionType.Create }
// 		]).then(result => {
// 			this.getNoticeList();
// 		}, error => {
// 			console.log(error);
// 		});
// 		console.log('Clicked on create notice');
// 	}
//
// 	/**
// 	 * Edit a Task
// 	 * @listens onEditCreateNotice
// 	 * @param {NoticeModel} dataItem
// 	 */
// 	public onEditNotice(dataItem: NoticeModel): void {
// 		this.dialogService.open(NoticeFormComponent, [
// 			{ provide: NoticeModel, useValue: dataItem as NoticeModel },
// 			{ provide: Number, useValue: ActionType.Edit }
// 		]).then(result => {
// 			this.getNoticeList();
// 		}, error => {
// 			console.log(error);
// 		});
// 	}
//
// 	protected dataStateChange(state: DataStateChangeEvent): void {
// 		this.state = state;
// 		this.gridData = process(this.noticeList, this.state);
// 	}
//
// 	protected applyCustomFilter(value: number): void {
// 		let index = this.state.filter.filters.findIndex(filter => {
// 			let x = filter as FilterDescriptor;
// 			return x.field === 'typeId';
// 		});
// 		if (index !== -1) {
// 			this.state.filter.filters.splice(index, 1);
// 		}
// 		if (value) {
// 			this.state.filter.filters.push({
// 				field: 'typeId',
// 				operator: 'eq',
// 				value: value
// 			});
// 		}
// 		this.gridData = process(this.noticeList, this.state);
// 	}
//
// 	protected isEditAvailable(): boolean {
// 		return this.permissionService.hasPermission(Permission.NoticeEdit);
// 	}
//
// 	protected isCreateAvailable(): boolean {
// 		return this.permissionService.hasPermission(Permission.NoticeCreate);
// 	}
//
// }