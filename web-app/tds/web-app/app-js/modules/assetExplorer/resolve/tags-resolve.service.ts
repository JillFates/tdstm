// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {TagService} from '../../assetTags/service/tag.service';
// Models
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class TagsResolveService implements Resolve<any> {
	constructor(private tagService: TagService, private router: Router) {
	}

	/**
	 * Get the List of Tags used in Several Views
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.tagService.getTags().map(result => {
			return result.status === ApiResponseModel.API_SUCCESS && result.data ? result.data : [];
		}).catch((err) => {
			console.error('TagsResolveService:', 'An Error Occurred trying to fetch all Tags');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}