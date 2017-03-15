import { Component } from '@angular/core';
import {StateService} from 'ui-router-ng2';

@Component({
    moduleId: module.id,
    selector: 'header',
    templateUrl: '../../tds/web-app/app-js/shared/modules/header/header.component.html',
})

export class HeaderComponent {

    private state: StateService;
    private pageMetaData: {
        title: string,
        instruction: string,
        menu: Array<string>
    };

    constructor(state: StateService) {
        this.state = state;

        if (this.state && this.state.$current && this.state.$current.data) {
            this.pageMetaData = this.state.$current.data.page;
            document.title = this.pageMetaData.title;
        }
    }

}