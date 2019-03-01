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
// Asset Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserPreferencesComponent} from './components/preferences/user-preferences.component';
import {UserEditPersonComponent} from './components/edit-person/user-edit-person.component';
import {UserDateTimezoneComponent} from './components/date-timezone/user-date-timezone.component';
import {UserDashboardComponent} from './components/dashboard/user-dashboard.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {UserResolveService} from './resolve/user-resolve.service';
// Services
import {UserService} from './service/user.service';
import {UserManageStaffComponent} from './components/manage-staff/user-manage-staff.component';
import {GridModule} from '@progress/kendo-angular-grid';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		AssetExplorerModule,
		// Route
		UserRouteModule,
		// Kendo
		DropDownsModule,
		GridModule
	],
	providers: [
		// Resolve
		ModuleResolveService,
		UserResolveService,
		// Service
		UserService
	],
	declarations: [
		UserDateTimezoneComponent,
		UserListComponent,
		UserPreferencesComponent,
		UserEditPersonComponent,
		UserManageStaffComponent,
		UserDashboardComponent
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