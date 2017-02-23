import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    encapsulation: ViewEncapsulation.None,
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html',
    providers: [NoticeService, { provide: NoticeModel, useValue: {} }]
})

export class NoticeListComponent implements OnInit {

    private moduleName: string = '';
    noticeList: NoticeModel[] = [];

    constructor(moduleName: string, private noticeService: NoticeService) {
        this.moduleName = 'Notice List';
    }

    /**
     * Get the Initial Notice List
     * @param noticeList
     */
    private onLoadNoticeList(noticeList): void {
        this.noticeList = noticeList;
    }

    public getNoticeList(): void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => this.onLoadNoticeList([]));
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        this.getNoticeList();
    }
}