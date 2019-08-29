// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
// Route Module
import {AuthRouteModule} from './auth-route.module';
// Services
import {CookieService} from 'ngx-cookie-service';
import {AuthService} from './service/auth.service';
import {UserContextService} from './service/user-context.service';
import {UserService} from './service/user.service';
import {AuthGuardService} from './service/auth.guard.service';
import {LoginService} from './service/login.service';
import {PermissionService} from '../../shared/services/permission.service';
// Components
import {LoginComponent} from './components/login/login.component';
import {ForgotPasswordComponent} from './components/forgot-password/forgot-password.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		// Route
		AuthRouteModule
	],
	providers: [
		CookieService,
		AuthService,
		UserService,
		PermissionService,
		UserContextService,
		AuthGuardService,
		LoginService
	],
	declarations: [
		LoginComponent,
		ForgotPasswordComponent,
		ChangePasswordComponent
	]
})
export class AuthModule {
}