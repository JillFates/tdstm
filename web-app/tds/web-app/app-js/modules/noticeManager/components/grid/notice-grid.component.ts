import { Component, OnInit } from '@angular/core';

import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';

@Component({
    moduleId: module.id,
    selector: 'notice-grid',
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/components/grid/notice-grid.component.html',
    providers: [NoticeService]
})

export class NoticeGridComponent implements OnInit {
    private moduleName = '';
    noticeList: NoticeModel[];
    onEditTemplate = '<button class="btn btn-default" (click)="onEditNotice(this)"><span class="glyphicon glyphicon-pencil"></span></button>';

    constructor(private noticeService: NoticeService) {
        this.moduleName = 'Notice Grid';
    }

    private onLoadNoticeList(noticeList: any): void {
        this.noticeList = noticeList.notices as NoticeModel[];
    }

    public onEditNotice(notice): void {
        console.log(notice);
    }

    ngOnInit(): void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => this.onLoadNoticeList({ notices: [] }));
    }

}