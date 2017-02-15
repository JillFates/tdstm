/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing'
import { By } from '@angular/platform-browser'
import { DebugElement } from '@angular/core'

import { NoticeListComponent } from '../components/list/notice-list.component'

describe('NoticeListComponent:', () => {
    let fixture: ComponentFixture<NoticeListComponent>;
    let comp: NoticeListComponent;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [NoticeListComponent],
            providers: [{ provide: String,multi:false, useValue: '' }]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NoticeListComponent);
        comp = fixture.componentInstance;
    });

    it('should create component', () => expect(comp).toBeDefined());

});