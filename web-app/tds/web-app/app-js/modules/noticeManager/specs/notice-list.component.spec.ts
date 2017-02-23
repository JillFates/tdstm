/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async, fakeAsync, tick } from '@angular/core/testing'
import { By } from '@angular/platform-browser'
import { DebugElement } from '@angular/core'
import { HttpModule } from '@angular/http';

import { NoticeListComponent } from '../components/list/notice-list.component'
import { NoticeService } from '../service/notice.service';
import { NoticeModel } from '../model/notice.model';
import { HttpServiceProvider } from '../../../shared/providers/http-interceptor.provider';
import { NotifierService } from '../../../shared/services/notifier.service';
import { Observable, Scheduler, TestScheduler } from 'rxjs/Rx';


describe('NoticeListComponent:', () => {
    let fixture: ComponentFixture<NoticeListComponent>;
    let comp: NoticeListComponent;

    let noticeService: NoticeService;
    let spyGet: jasmine.Spy;

    let mockData: Array<NoticeModel> = [
        new NoticeModel(new Date(Date.now()), "Mario", "Description", 1),
        new NoticeModel(new Date(Date.now()), "Peach", "Description", 2),
        new NoticeModel(new Date(Date.now()), "Luigi", "Description", 3),
        new NoticeModel(new Date(Date.now()), "Kappa", "Description", 4)];

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [HttpModule],
            declarations: [NoticeListComponent],
            providers: [NoticeService, { provide: String, multi: false, useValue: '' }, { provide: NoticeModel, useValue: {} },
                HttpServiceProvider, NotifierService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NoticeListComponent);
        comp = fixture.componentInstance;
        noticeService = fixture.debugElement.injector.get(NoticeService);
        spyGet = spyOn(noticeService, 'getNoticesList')
            .and.returnValue(Observable.from(mockData).bufferCount(mockData.length));
    });

    it('should create component', () => expect(comp).toBeDefined());

    it('should call getNoticesList and retrive notice list', done => {
        expect(spyGet.calls.any()).toBe(false);

        fixture.detectChanges();
        expect(spyGet.calls.count()).toBe(1);
        spyGet.calls.mostRecent().returnValue.subscribe(
            (noticeList) => {
                expect(noticeList.length).toBe(4)
            },
            (err) => {
                    console.log('error')
            },
            () => {//completed callback
                expect(comp.noticeList.length).toBe(4);
                done();
            }
        )
    });

});