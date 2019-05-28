/**
 * Created by Jorge Morayta on 3/15/2017.
 */

// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {UserRouteModule} from './user-routing.states';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserPreferencesComponent} from './components/preferences/user-preferences.component';
import {UserEditPersonComponent} from './components/edit-person/user-edit-person.component';
import {UserDateTimezoneComponent} from './components/date-timezone/user-date-timezone.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {UserResolveService} from './resolve/user-resolve.service';
// Services
import {UserService} from './service/user.service';
import {UserManageStaffComponent} from './components/manage-staff/user-manage-staff.component';
import {UserPostNoticesManagerService} from './service/user-post-notices-manager.service';
import {UserPostNoticesService} from './service/user-post-notices.service';
import {NoticesValidatorService} from './service/notices-validator.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		// Route
		UserRouteModule,
		// Kendo
		DropDownsModule
	],
	providers: [
		// Resolve
		ModuleResolveService,
		UserResolveService,
		// Service
		UserService,
		UserPostNoticesManagerService,
		UserPostNoticesService,
		NoticesValidatorService
	],
	declarations: [
		UserDateTimezoneComponent,
		UserListComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserManageStaffComponent
	],
	exports: [
		UserDateTimezoneComponent,
		UserListComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserManageStaffComponent
	],
	entryComponents: [
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserDateTimezoneComponent,
		UserManageStaffComponent
	]
})

export class UserModule {

}