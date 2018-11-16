// /**
//  * Created by aaferreira on 13/02/2017.
//  */
// import {ComponentFixture, TestBed, async, fakeAsync, tick} from '@angular/core/testing';
// import {By} from '@angular/platform-browser';
// import {DebugElement} from '@angular/core';
// import {HttpModule, Http} from '@angular/http';
// import {FormsModule} from '@angular/forms';
// import {Observable} from 'rxjs/Observable';
// import {GridModule} from '@progress/kendo-angular-grid';
// // import { TranslateModule, TranslateLoader, TranslateStaticLoader, TranslateService } from 'ng2-translate';
//
// import {SharedModule} from '../../../shared/shared.module';
// import {HttpServiceProvider} from '../../../shared/providers/http-interceptor.provider';
// import {NotifierService} from '../../../shared/services/notifier.service';
//
// import {NoticeListComponent} from '../components/list/notice-list.component';
// import {NoticeService} from '../service/notice.service';
// import {NoticeModel} from '../model/notice.model';
// import {DropDownListModule} from '@progress/kendo-angular-dropdowns';
//
// describe('NoticeListComponent:', () => {
// 	let fixture: ComponentFixture<NoticeListComponent>;
// 	let comp: NoticeListComponent;
//
// 	let noticeService: NoticeService;
// 	let mockData: any = [{
// 		notices: [
// 			{
// 				id: 1,
// 				title: 'NOTICE_1',
// 				active: true,
// 				acknowledgeable: true,
// 				createdBy: 'TEST',
// 				dateCreated: new Date(Date.now()).toDateString(),
// 				htmlText: '<p>this is a paragraph</p>',
// 				rawText: '<p>this is a paragraph</p>',
// 				lastModified: new Date(Date.now()).toDateString(),
// 				typeId: 1
// 			}, {
// 				id: 2,
// 				title: 'NOTICE_2',
// 				active: true,
// 				acknowledgeable: true,
// 				createdBy: 'TEST',
// 				dateCreated: new Date(Date.now()).toDateString(),
// 				htmlText: '<p>this is a paragraph</p>',
// 				rawText: '<p>this is a paragraph</p>',
// 				lastModified: new Date(Date.now()).toDateString(),
// 				typeId: 1
// 			}
// 		]
// 	}];
// 	let spyGet: jasmine.Spy;
//
// 	beforeEach(async(() => {
// 		TestBed.configureTestingModule({
// 			imports: [HttpModule, FormsModule, SharedModule, DropDownListModule,
// 				GridModule
// 				// TranslateModule.forRoot({
// 				//     provide: TranslateLoader,
// 				//     useFactory: (http: Http) => new TranslateStaticLoader(http, '../../tds/web-app/i18n', '.json'),
// 				//     deps: [Http]
// 				// })
// 			],
// 			declarations: [NoticeListComponent],
// 			providers: [NoticeService, HttpServiceProvider,
// 				NotifierService,
// 				{provide: 'notices', useValue: Observable.from(mockData)}
// 				// TranslateService
// 			]
// 		}).compileComponents();
// 	}));
//
// 	beforeEach(() => {
// 		fixture = TestBed.createComponent(NoticeListComponent);
// 		comp = fixture.componentInstance;
// 		noticeService = fixture.debugElement.injector.get(NoticeService);
// 		spyGet = spyOn(noticeService, 'getNoticesList')
// 			.and.returnValue(Observable.from(mockData));
// 	});
//
// 	it('should create component', () => expect(comp).toBeDefined());
//
// 	it('should not call getNoticesList at start', done => {
// 		fixture.whenStable().then(() => {
// 			fixture.detectChanges();
// 			expect(spyGet.calls.any()).toBe(false);
// 			done();
// 		});
// 	});
//
// 	it('should call getNoticesList when grid is refreshed', done => {
// 		comp.reloadNoticeList();
// 		fixture.whenStable().then(() => {
// 			fixture.detectChanges();
// 			expect(spyGet.calls.any()).toBe(true);
// 			spyGet.calls.mostRecent().returnValue.subscribe(
// 				(noticeList) => {
// 					expect(noticeList.notices.length).toBe(2);
// 				},
// 				(err) => {
// 					console.log('error');
// 				},
// 				() => {
// 					done();
// 				});
// 		});
// 	});
//
// });