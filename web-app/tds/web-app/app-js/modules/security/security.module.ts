import {NgModule} from '@angular/core';
// Route Module
import {SecurityRouteModule} from './security-route.module';
import {SharedModule} from '../../shared/shared.module';
// Components
import {ErrorPageComponent} from './errorPage/error-page.component';
import {UnauthorizedPageComponent} from './unauthorizedPage/unauthorized-page.component';
import {NotFoundPageComponent} from './notFoundPage/not-found-page.component';

@NgModule({
	imports: [
		SharedModule,
		SecurityRouteModule
	],
	declarations: [
		ErrorPageComponent,
		UnauthorizedPageComponent,
		NotFoundPageComponent
	]
})
export class SecurityModule {
}