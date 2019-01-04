// Angular
import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// Services
import {PreferenceService} from '../services/preference.service';
// Models
import {PREFERENCES_LIST} from '../services/preference.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class PreferencesResolveService implements CanActivate {

	constructor(private preferenceService: PreferenceService, private router: Router) {
	}

	/**
	 * Guard Code to Pre Fill all Preferences with a few one as default
	 * @param route
	 * @param state
	 */
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
		let requiresPermission: string[] = route.data['requiresPermissions'];
		// Get the Preferences that by Default are available in most parts of the Application, like time zone and date format
		return this.preferenceService.getPreferences(
			PREFERENCES_LIST.CURR_TZ,
			PREFERENCES_LIST.CURRENT_DATE_FORMAT
		).map(() => {
			return true;
		}).catch((err) => {
			console.error('PreferencesResolveService:', err);
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}