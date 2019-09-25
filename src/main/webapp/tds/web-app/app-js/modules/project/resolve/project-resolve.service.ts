// Angular
import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
// Services
import {ProjectService} from '../service/project.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class ProjectResolveService implements Resolve<any> {
	constructor(private projectService: ProjectService, private router: Router) {
	}

	/**
	 * Get the List of Projects
	 * @param route
	 */
	resolve(route: ActivatedRouteSnapshot): Observable<any> | boolean {
		return this.projectService.getProjects().map(projects => {
			return projects;
		}).catch((err) => {
			console.error('ProjectService:', 'An Error Occurred trying to fetch Project List');
			this.router.navigate(['/security/error']);
			return Observable.of(false);
		});
	}
}