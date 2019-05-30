// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {SecurityRouteModule} from './security-route.module';
// Components
import {ErrorPageComponent} from './components/error-page/error-page.component';
import {UnauthorizedPageComponent} from './components/unauthorized-page/unauthorized-page.component';
import {NotFoundPageComponent} from './components/not-found-page/not-found-page.component';

// Services
// import {UserPostNoticesManagerService} from '../security/services/user-post-notices-manager.service';
// import {UserPostNoticesService} from '../security/services/user-post-notices.service';
// import {NoticesValidatorService} from '../security/services/notices-validator.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Route
		SecurityRouteModule
	],
	declarations: [
		ErrorPageComponent,
		UnauthorizedPageComponent,
		NotFoundPageComponent
	],
	providers: [
		// UserPostNoticesManagerService,
		// UserPostNoticesService,
		// NoticesValidatorService
	],
	exports: [
		ErrorPageComponent,
		UnauthorizedPageComponent,
		NotFoundPageComponent
	],
})
export class SecurityModule {
}