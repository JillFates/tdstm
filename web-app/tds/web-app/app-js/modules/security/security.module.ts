import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
// Route Module
import {SecurityRouteModule} from './security-route.module';
// Components
import {ErrorPageComponent} from './errorPage/error-page.component';
import {UnauthorizedPageComponent} from './unauthorizedPage/unauthorized-page.component';
import {NotFoundPageComponent} from './notFoundPage/not-found-page.component';

@NgModule({
	imports: [
		CommonModule,
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