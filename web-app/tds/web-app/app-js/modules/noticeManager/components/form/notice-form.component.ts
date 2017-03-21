import { Component } from '@angular/core';

import { NoticeModel } from '../../model/notice.model';
import { NoticeService } from '../../service/notice.service';

import { ActionType } from '../../../../shared/model/action-type.enum';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
    moduleId: module.id,
    selector: 'notice-form',
    templateUrl: '../../tds/web-app/app-js/modules/noticeManager/components/form/notice-form.component.html'
})
export class NoticeFormComponent {
    defaultItem: any = {
        typeId: null, name: 'Select a Type'
    };
    typeDataSource: Array<any> = [
        { typeId: 1, name: 'Prelogin' },
        { typeId: 2, name: 'Postlogin' }
    ];

    constructor(public model: NoticeModel, public action: Number, public activeDialog: UIActiveDialogService) {
        console.log(model, action as ActionType);
    }

    cancelCloseDialog(): void {
        this.activeDialog.dismiss();
    }

    deleteNotice(): void {
        this.activeDialog.close();
    }

    saveNotice(): void {
        this.activeDialog.close();
    }
}
