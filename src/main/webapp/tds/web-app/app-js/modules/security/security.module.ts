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
import {ErrorLicensePageComponent} from './components/error-license-page/error-license-page.component';
import {TokenInvalidPageComponent} from './components/token-invalid/token-invalid-page.component';

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
		NotFoundPageComponent,
		ErrorLicensePageComponent,
		TokenInvalidPageComponent
	],
	providers: [],
	exports: [
		ErrorPageComponent,
		UnauthorizedPageComponent,
		NotFoundPageComponent,
		ErrorLicensePageComponent,
		TokenInvalidPageComponent
	],
})
export class SecurityModule {
}
