// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AuthRouteModule} from './auth-route.module';
// Services
import {CookieService} from 'ngx-cookie-service';
import {AuthService} from './service/auth.service';
import {UserContextService} from './service/user-context.service';
import {UserService} from './service/user.service';
import {AuthGuardService} from './service/auth.guard.service';
import {LoginService} from './service/login.service';
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
	providers: [
		CookieService,
		AuthService,
		UserService,
		UserContextService,
		AuthGuardService,
		LoginService
	],
	declarations: [
		LoginComponent
	]
})
export class AuthModule {
}