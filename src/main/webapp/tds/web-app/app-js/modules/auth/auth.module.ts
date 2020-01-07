// Angular
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
// Modules
import { SharedModule } from '../../shared/shared.module';
import { AuthRouteModule } from './auth-route.module';
// Services
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from './service/auth.service';
import { UserContextService } from './service/user-context.service';
import { UserService } from './service/user.service';
import { AuthGuardService } from './service/auth.guard.service';
import { LoginService } from './service/login.service';
import { PermissionService } from '../../shared/services/permission.service';
import {PageService} from './service/page.service';
// Components
import { LoginComponent } from './components/login/login.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { StandardNoticesComponent } from '../noticeManager/components/standard-notices/standard-notices.component';
import { MandatoryNoticesComponent } from '../noticeManager/components/mandatory-notices/mandatory-notices.component';
import {SelectProjectModalComponent} from '../project/components/select-project-modal/select-project-modal.component';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		ReactiveFormsModule,
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
		LoginService,
		PageService
	],
	declarations: [
		LoginComponent,
		ForgotPasswordComponent,
		ChangePasswordComponent,
		StandardNoticesComponent,
		MandatoryNoticesComponent,
		SelectProjectModalComponent
	],
	entryComponents: [
		StandardNoticesComponent,
		MandatoryNoticesComponent,
		SelectProjectModalComponent
	]
})
export class AuthModule {
}
