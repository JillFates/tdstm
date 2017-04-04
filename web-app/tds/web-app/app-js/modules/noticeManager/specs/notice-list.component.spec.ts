/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async, fakeAsync, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { HttpModule } from '@angular/http';
import { FormsModule } from '@angular/forms';
import { Observable, Scheduler, TestScheduler } from 'rxjs/Rx';
import { GridModule } from '@progress/kendo-angular-grid';
import { DropDownListModule } from '@progress/kendo-angular-dropdowns';

import { SharedModule } from '../../../shared/shared.module';
import { HttpServiceProvider } from '../../../shared/providers/http-interceptor.provider';
import { NotifierService } from '../../../shared/services/notifier.service';

import { NoticeListComponent } from '../components/list/notice-list.component';
import { NoticeService } from '../service/notice.service';

describe('NoticeListComponent:', () => {
    let fixture: ComponentFixture<NoticeListComponent>;
    let comp: NoticeListComponent;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [HttpModule, FormsModule, SharedModule, DropDownListModule],
            declarations: [NoticeListComponent],
            providers: [NoticeService, HttpServiceProvider, NotifierService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NoticeListComponent);
        comp = fixture.componentInstance;
    });

    it('should create component', () => expect(comp).toBeDefined());

});