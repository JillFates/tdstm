// Angular
import {NgModule} from '@angular/core';
// Route Module
import {SharedModule} from '../../shared/shared.module';
import {SecurityRouteModule} from './security-route.module';
// Components
import {ErrorPageComponent} from './error-page/error-page.component';
import {UnauthorizedPageComponent} from './unauthorized-page/unauthorized-page.component';
import {NotFoundPageComponent} from './not-found-page/not-found-page.component';

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