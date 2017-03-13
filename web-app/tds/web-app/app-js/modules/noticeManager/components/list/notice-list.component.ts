import { Component, ViewChild, OnInit, AfterViewInit, ViewEncapsulation } from '@angular/core';
import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';

import { ActionType } from '../../../../shared/model/action-type.enum';

import { GridComponent } from '@progress/kendo-angular-grid';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    encapsulation: ViewEncapsulation.None,
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html',
    providers: [NoticeService, { provide: NoticeModel, useValue: {} }]
})

export class NoticeListComponent implements OnInit {

    private moduleName = '';
    private title = '';
    noticeList: NoticeModel[] = [];

    ActionType: typeof ActionType = ActionType;

    @ViewChild(GridComponent) private grid: GridComponent;

    /**
     * @constructor
     * @param {NoticeService} noticeService
     */
    constructor(private noticeService: NoticeService) {
        this.moduleName = 'Notice List';
    }

    /**
     * Get the Initial Notice List
     * @param noticeList
     */
    private onLoadNoticeList(noticeList): void {
        this.noticeList = noticeList.notices;
    }

    private getNoticeList(): void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => this.onLoadNoticeList([]));
    }

    public reloadNoticeList(): void {
        console.log(this.grid);
        this.getNoticeList();
    }

    public onEditCreateNotice(actionType: ActionType, dataItem: NoticeModel): void {
        console.log('Clicked on ', dataItem);
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        this.getNoticeList();
    }

}