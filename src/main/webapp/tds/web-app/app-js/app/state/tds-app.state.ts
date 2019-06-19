// Store
import {Selector, State} from '@ngxs/store';
import {UserContextState} from '../../modules/auth/state/user-context.state';

export interface TDSAppStateModel {
	/**
	 * Contains the basic structure of the logged user
	 */
	userContext: {},
}

@State<TDSAppStateModel>({
	name: 'TDSApp',
	children: [UserContextState]
})
export class TDSAppState {

	@Selector()
	static TDSAppState(state: TDSAppStateModel) {
		return state;
	}

}
