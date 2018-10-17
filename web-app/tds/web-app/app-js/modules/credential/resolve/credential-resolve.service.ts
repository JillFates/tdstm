// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {CredentialService} from '../service/credential.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class CredentialResolveService implements Resolve<any> {
	constructor(private credentialService: CredentialService, private router: Router) {
	}

	/**
	 * Get the List of Providers
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.credentialService.getCredentials().map(credentials => {
			return credentials;
		}).catch((err) => {
			console.error('CredentialResolveService:', 'An Error Occurred trying to fetch the Credential List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}