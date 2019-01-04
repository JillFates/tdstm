/**
 * Created by aaferreira on 07/04/2017.
 */
import {ComponentFixture, TestBed, async, fakeAsync, tick} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {HttpModule, Http} from '@angular/http';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {DropDownListModule} from '@progress/kendo-angular-dropdowns';
// import { TranslateModule, TranslateLoader, TranslateStaticLoader, TranslateService } from 'ng2-translate';

import {SharedModule} from '../../../shared/shared.module';
import {HttpServiceProvider} from '../../../shared/providers/http-interceptor.provider';
import {NotifierService} from '../../../shared/services/notifier.service';
import {ActionType} from '../../../shared/model/action-type.enum';
import {UIActiveDialogService} from '../../../shared/services/ui-dialog.service';

// import {NoticeFormComponent} from '../components/form/notice-form.component';
// import {NoticeService} from '../service/notice.service';
// import {NoticeModel} from '../model/notice.model';
//
// describe('NoticeFormComponent:', () => {
// 	let fixture: ComponentFixture<NoticeFormComponent>;
// 	let comp: NoticeFormComponent;
//
// 	let noticeService: NoticeService;
// 	let notice: NoticeModel = {
// 		id: 1,
// 		title: 'NOTICE_1',
// 		active: true,
// 		acknowledgeable: true,
// 		createdBy: 'TEST',
// 		dateCreated: new Date(Date.now()).toDateString(),
// 		htmlText: '<p>this is a paragraph</p>',
// 		rawText: '<p>this is a paragraph</p>',
// 		lastModified: new Date(Date.now()).toDateString(),
// 		typeId: 1
// 	};
//
// 	beforeEach(async(() => {
// 		TestBed.configureTestingModule({
// 			imports: [HttpModule, FormsModule, SharedModule, DropDownListModule,
// 				// TranslateModule.forRoot({
// 				//     provide: TranslateLoader,
// 				//     useFactory: (http: Http) => new TranslateStaticLoader(http, '../../tds/web-app/i18n', '.json'),
// 				//     deps: [Http]
// 				// })
// 			],
// 			declarations: [NoticeFormComponent],
// 			providers: [NoticeService, HttpServiceProvider, NotifierService,
// 				// TranslateService,
// 				UIActiveDialogService,
// 				{provide: NoticeModel, useValue: notice},
// 				{provide: Number, useValue: ActionType.Edit}]
// 		}).compileComponents();
// 	}));
//
// 	beforeEach(() => {
// 		fixture = TestBed.createComponent(NoticeFormComponent);
// 		comp = fixture.componentInstance;
// 		noticeService = fixture.debugElement.injector.get(NoticeService);
// 	});
//
// 	it('should create component', () => expect(comp).toBeDefined());
//
// 	it('should display valid form', () => {
// 		fixture.detectChanges();
// 		expect(comp.noticeForm.valid).toBeTruthy();
// 	});
//
// });