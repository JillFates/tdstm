/**
 * User Context is an early service injected at the beginning of the App that initialize the App Object that contains
 * multiple information necessary to run the application, like permissions, user info, license management, etc.
 */
// Angular
import {Injectable} from '@angular/core';
// NGXS
import {Store} from '@ngxs/store';
// Model
import {UserContextModel} from '../model/user-context.model';
// Others
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export class UserContextService {

	private userContextSubject = new BehaviorSubject(new UserContextModel());
	private userContext = this.userContextSubject.asObservable();

	constructor(
		private store: Store) {
		// Add the user context
		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			if (userContext && userContext.user) {
				this.userContextSubject.next(userContext);
			}
		});
	}

	/**
	 * Get the User Context
	 */
	public getUserContext(): Observable<UserContextModel> {
		return Observable.from(this.userContext);
	}
}
