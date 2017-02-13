import { Component, OnInit } from '@angular/core';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/list/notice-list.component.html',
})

export class NoticeListComponent implements OnInit {

    private moduleName: string = '';

    constructor(moduleName: string) {
        this.moduleName = 'Notice List';
    }

    ngOnInit(): void {
        console.log('Notice List Loaded');
    }
}