/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {UserPostNoticesService} from './user-post-notices.service';

import {switchMap} from 'rxjs/operators';

@Injectable()
export class UserPostNoticesManagerService {
	private postNotices: any;

	constructor(private userPostNoticesService: UserPostNoticesService) {
		this.postNotices = null;
	}

	getNotices(): Observable<any> {
		// if notices already exists, return the collection
		if (this.postNotices !== null) {
			return Observable.of(this.postNotices);
		}

		// otherwise call the endpoint
		return this.userPostNoticesService
			.getUserPostNotices()
			.pipe(
				switchMap((postNotices) => {
					this.postNotices = postNotices;
					return Observable.of(postNotices);
				})
			);
	}

	hasNoticesPending(): Observable<any> {
		return this.getNotices()
			.pipe(
				switchMap((postNotices) => {
					const itemsCounter =  (postNotices && postNotices.notices || []).length > 0;

					return Observable.of(itemsCounter);
				})
			);
	}

	/**
	 * Set the Acknowledge state for a notice
	 * @param {number} id:  Id of the notice
	 * @returns NoticeModel
	 */
	setAcknowledge(id: number) {
		return this.userPostNoticesService.setAcknowledge(id)
		.pipe(
			switchMap((result) => {
				this.removeNoticeFromCollection(id);
				return Observable.of(result);
			})
		);
	}

	private removeNoticeFromCollection(id: number) {
		this.postNotices.notices = this.postNotices.notices.filter((notice) => notice.id !== id);
	}

	/**
    * Call the endpoint to se the flag that filter the notices pending
    * @returns any
    */
	notifyContinue()  {
		return this.userPostNoticesService.notifyContinue()
			.pipe(
				switchMap((result) => {
					console.log(this.postNotices);
					return Observable.of(result);
				})
			);
	}

}