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

    constructor(private noticeService: NoticeService) {
        this.moduleName = 'Notice Grid';
    }

    private onLoadNoticeList(noticeList: any): void {
        this.noticeList = noticeList.notices as NoticeModel[];

    }

    public add(): void {
        let notice = new NoticeModel();
        notice.title = 'Teste';
        notice.active = false;
        this.noticeList.push(notice);
    }

    ngOnInit(): void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => this.onLoadNoticeList({ notices: [] }));
    }

}