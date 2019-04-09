// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AuthRouteModule} from './auth-route.module';
// Components
import {LoginComponent} from './components/login/login.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Route
		AuthRouteModule
	],
	declarations: [
		LoginComponent
	]
})
export class AuthModule {
}