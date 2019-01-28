// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {AssetCommentService} from '../service/asset-comment.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class AssetCommentResolveService implements Resolve<any> {
	constructor(private assetCommentService: AssetCommentService, private router: Router) {
	}

	/**
	 * Get the List of Asset Comments
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.assetCommentService.getAssetComments().map(assetComments => {
			return assetComments;
		}).catch((err) => {
			console.error('AssetCommentResolveService:', 'An Error Occurred trying to fetch Asset Comment List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}