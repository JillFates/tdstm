/**
 * UserPostNoticesContextService is an early service injected at the beginning of the App that initialize the App Object that contains
 * information to handle state of the user the post notices info
 */
// Angular
import {Injectable} from '@angular/core';
// Model
// Services
import {UserPostNoticesManagerService} from './user-post-notices-manager.service';
// Others
import {BehaviorSubject, Observable} from 'rxjs';
import {UserPostNoticesContextModel} from '../model/user-post-notices-context.model';

@Injectable()
export class UserPostNoticesContextService {

	private userContextSubject = new BehaviorSubject(new UserPostNoticesContextModel());
	private userContext = this.userContextSubject.asObservable();

	constructor(private userPostNoticesManagerService: UserPostNoticesManagerService) {
	}

	/**
	 * Get the User Context
	 */
	public getUserPostNoticesContext(): Observable<UserPostNoticesContextModel> {
		return Observable.from(this.userContext);
	}

	/**
	 * Being call in the Bootstrap of the Application
	 */
	public initializeUserPostNoticesContext() {
		let contextPromises = [];
		contextPromises.push(Observable.of(this.userPostNoticesManagerService));

		return new Promise((resolve) => {
			Observable.forkJoin(contextPromises)
				.subscribe((contextResponse: any) => {
					const [postNoticesManager] = contextResponse;
					this.userContextSubject.next({postNoticesManager});
					resolve(true);
				});
		})
	}
}