// Angular
import {ComponentFactoryResolver, Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
// Services
import {ComponentCreatorService} from '../../../shared/services/component-creator.service';
// Others
import {Observable} from 'rxjs';

@Injectable()
export class ModuleResolverService implements CanActivate {

	constructor(private moduleResolver: ComponentFactoryResolver, private componentCreatorService: ComponentCreatorService) {
	}

	/**
	 * Guard Code to Inject the current Module Resolver, so we can use dialogs across the app
	 * @param route
	 * @param state
	 */
	canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
		this.componentCreatorService.setFactoryResolver(this.moduleResolver);
		return Observable.of(true);
	}
}