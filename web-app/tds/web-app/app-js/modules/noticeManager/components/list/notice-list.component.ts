import { Component, OnInit } from '@angular/core';
import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html',
    providers: [NoticeService, {provide: NoticeModel, useValue: {}}]
})

export class NoticeListComponent implements OnInit {

    private moduleName:string = '';
    private noticeList:NoticeModel[];

    constructor(moduleName:string, private noticeService:NoticeService) {
        this.moduleName = 'Notice List';
    }

    /**
     * Get the Initial Notice List
     * @param noticeList
     */
    private onLoadNoticeList(noticeList) {
        this.noticeList = noticeList;
        console.log(this.noticeList);
    }

    ngOnInit():void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => {
                console.log(err);
            });

        console.log('Notice List Loaded');
    }
}