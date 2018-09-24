// Angular
import {NgModule} from '@angular/core';
// Route Module
import {SharedModule} from '../../shared/shared.module';
import {SecurityRouteModule} from './security-route.module';
// Components
import {ErrorPageComponent} from './components/error-page/error-page.component';
import {UnauthorizedPageComponent} from './components/unauthorized-page/unauthorized-page.component';
import {NotFoundPageComponent} from './components/not-found-page/not-found-page.component';
import {AuthGuardService} from './services/auth.guard.service';

@NgModule({
	imports: [
		SharedModule,
		SecurityRouteModule
	],
	providers: [
		AuthGuardService
	],
	declarations: [
		ErrorPageComponent,
		UnauthorizedPageComponent,
		NotFoundPageComponent
	]
})
export class SecurityModule {
}