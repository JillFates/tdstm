// import {Injectable} from '@angular/core';
// import {Response, Headers, RequestOptions} from '@angular/http';
// import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
// import {NotifierService} from '../../../shared/services/notifier.service';
// import {NoticeModel} from '../model/notice.model';
// import {Observable} from 'rxjs/Observable';
//
// import 'rxjs/add/operator/map';
// import 'rxjs/add/operator/catch';
//
// /**
//  * @name NoticeService
//  */
// @Injectable()
// export class NoticeService {
//
// 	// private instance variable to hold base url
// 	private noticeListUrl = '../ws/notices';
//
// 	// Resolve HTTP using the constructor
// 	constructor(private http: HttpInterceptor) {
// 	}
//
// 	/**
// 	 * Get the Notice List
// 	 * @returns {Observable<R>}
// 	 */
// 	getNoticesList(): Observable<NoticeModel[]> {
// 		return this.http.get(this.noticeListUrl)
// 			.map((res: Response) => res.json())
// 			.catch((error: any) => error.json());
// 	}
//
// 	createNotice(notice: NoticeModel): Observable<NoticeModel[]> {
// 		return this.http.post(this.noticeListUrl, JSON.stringify(notice))
// 			.map((res: Response) => res.json())
// 			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
// 	}
//
// 	editNotice(notice: NoticeModel): Observable<NoticeModel[]> {
// 		return this.http.put(`${this.noticeListUrl}/${notice.id}`, JSON.stringify(notice))
// 			.map((res: Response) => res.json())
// 			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
// 	}
//
// 	deleteNotice(notice: NoticeModel): Observable<NoticeModel[]> {
// 		return this.http.delete(`${this.noticeListUrl}/${notice.id}`)
// 			.map((res: Response) => res.json())
// 			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
// 	}
// }