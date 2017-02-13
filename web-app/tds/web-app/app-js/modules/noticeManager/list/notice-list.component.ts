import { Component, OnInit } from '@angular/core';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    template: '<a uiSref="tds" uiSrefActive="active">Return</a> <br /><h1>{{moduleName}}</h1>'
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