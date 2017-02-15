import { Component, OnInit } from '@angular/core';
import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/list/notice-list.component.html',
    providers: [NoticeService, {provide: NoticeModel, useValue: {}}]
})

export class NoticeListComponent implements OnInit {

    private moduleName:string = '';

    constructor(moduleName:string, private noticeService:NoticeService, private noticeModel:NoticeModel) {
        this.moduleName = 'Notice List';
    }

    ngOnInit():void {
        console.log('Notice List Loaded');
    }
}