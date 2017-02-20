/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing'
import { By } from '@angular/platform-browser'
import { DebugElement } from '@angular/core'
import { HttpModule } from '@angular/http';

import { NoticeListComponent } from '../components/list/notice-list.component'
import { NoticeService } from '../service/notice.service';
import { NoticeModel } from '../model/notice.model';
import { HttpServiceProvider } from '../../../shared/providers/http-interceptor.provider';
import { NotifierService } from '../../../shared/services/notifier.service';

describe('NoticeListComponent:', () => {
    let fixture: ComponentFixture<NoticeListComponent>;
    let comp: NoticeListComponent;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [HttpModule],
            declarations: [NoticeListComponent],
            providers: [{ provide: String, multi: false, useValue: '' }, { provide: NoticeModel, useValue: {} },
                HttpServiceProvider, NotifierService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NoticeListComponent);
        comp = fixture.componentInstance;
    });

    it('should create component', () => expect(comp).toBeDefined());

});