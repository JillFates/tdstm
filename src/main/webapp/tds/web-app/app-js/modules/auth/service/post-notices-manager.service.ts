/**
 * User Service is a top level service to retrieve information of the current user logged
 * For more specific endpoint, changes should be done on its specific module instead
 */
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PostNoticesService} from './post-notices.service';

import {switchMap} from 'rxjs/operators';
import {Store} from '@ngxs/store';
import {PostNoticeRemove} from '../action/notice.actions';

@Injectable()
export class PostNoticesManagerService {

	constructor(
		private postNoticesService: PostNoticesService,
		private store: Store) {
	}

	/**
	 * Get The List of Post Notices
	 */
	public getPostNotices(): Observable<any> {
		return this.postNoticesService
			.getPostNotices()
			.pipe(
				switchMap((postNotices) => {
					return Observable.of(postNotices);
				})
			);
	}

	/**
	 * Does the Notices is a Pending State?
	 */
	public hasNoticesPending(): Observable<any> {
		return this.getPostNotices()
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
	public setAcknowledge(id: number) {
		return this.postNoticesService.setAcknowledge(id)
		.pipe(
			switchMap((result) => {
				this.removeNoticeFromCollection(id);
				return Observable.of(result);
			})
		);
	}

	/**
	 * Remove the Notice from the Storage instead of the local so we have just single source of truth
	 * @param id
	 */
	private removeNoticeFromCollection(id: number) {
		this.store.dispatch(new PostNoticeRemove({id: id}));
	}

	/**
    * Call the endpoint to se the flag that filter the notices pending
    * @returns any
    */
	notifyContinue()  {
		return this.postNoticesService.notifyContinue()
			.pipe(
				switchMap((result) => {
					return Observable.of(result);
				})
			);
	}

}
