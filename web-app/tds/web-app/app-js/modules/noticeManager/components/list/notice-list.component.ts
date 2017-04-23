import { Component, ViewChild, OnInit, AfterViewInit, ViewEncapsulation } from '@angular/core';
import { NoticeService } from '../../service/notice.service';
import { NoticeModel } from '../../model/notice.model';
import { NoticeFormComponent } from '../form/notice-form.component';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { ActionType } from '../../../../shared/model/action-type.enum';

import { GridComponent } from '@progress/kendo-angular-grid';
import { SortDescriptor, orderBy } from '@progress/kendo-data-query';

@Component({
    moduleId: module.id,
    selector: 'notice-list',
    encapsulation: ViewEncapsulation.None,
    templateUrl: '../tds/web-app/app-js/modules/noticeManager/components/list/notice-list.component.html',
    providers: [NoticeService]
})

export class NoticeListComponent implements OnInit {

    private moduleName = '';
    private title = '';
    noticeList: NoticeModel[] = [];
    sort: SortDescriptor[] = [{
        dir: 'asc',
        field: 'title'
    }];

    ActionType: typeof ActionType = ActionType;

    @ViewChild(GridComponent) private grid: GridComponent;

    /**
     * @constructor
     * @param {NoticeService} noticeService
     */
    constructor(private noticeService: NoticeService, private dialogService: UIDialogService) {
        this.moduleName = 'Notice List';
    }

    /**
     * Get the Initial Notice List
     * @param noticeList
     */
    private onLoadNoticeList(noticeList): void {
        this.noticeList = orderBy(noticeList.notices as NoticeModel[], this.sort);
    }

    private getNoticeList(): void {
        this.noticeService.getNoticesList().subscribe(
            (noticeList) => this.onLoadNoticeList(noticeList),
            (err) => this.onLoadNoticeList([]));
    }

    public reloadNoticeList(): void {
        this.getNoticeList();
    }

    /**
     * Create a new Notice
     * @listens onCreateNotice
     */
    public onCreateNotice(): void {
        this.dialogService.open(NoticeFormComponent, [
            { provide: NoticeModel, useValue: new NoticeModel() },
            { provide: Number, useValue: ActionType.Create }
        ]).then(result => {
            this.getNoticeList();
        }, error => {
            console.log(error);
        });
        console.log('Clicked on create notice');
    }

    /**
     * Edit a Task
     * @listens onEditCreateNotice
     * @param {NoticeModel} dataItem
     */
    public onEditNotice(dataItem: NoticeModel): void {
        this.dialogService.open(NoticeFormComponent, [
            { provide: NoticeModel, useValue: dataItem as NoticeModel },
            { provide: Number, useValue: ActionType.Edit }
        ]).then(result => {
            this.getNoticeList();
        }, error => {
            console.log(error);
        });
        console.log('Clicked on ', dataItem);
    }

    /**
     * Initiates the Notice Module
     */
    ngOnInit(): void {
        this.getNoticeList();
    }

}