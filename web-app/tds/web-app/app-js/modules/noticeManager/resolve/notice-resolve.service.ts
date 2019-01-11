// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {NoticeService} from '../service/notice.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class NoticeResolveService implements Resolve<any> {
	constructor(private noticeService: NoticeService, private router: Router) {
	}

	/**
	 * Get the List of Notices
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.noticeService.getNoticesList().map((result: any) => {
			return result.notices;
		}).catch((err: any) => {
			console.error('NoticeResolveService:', 'An Error Occurred trying to fetch Notice List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}