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
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserPreferencesComponent} from './components/preferences/user-preferences.component';
import {UserEditPersonComponent} from './components/edit-person/user-edit-person.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {UserResolveService} from './resolve/user-resolve.service';
// Services
import {UserService} from './service/user.service';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';

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
		UserService
	],
	declarations: [
		UserListComponent,
		UserPreferencesComponent,
		UserEditPersonComponent
	],
	exports: [
		UserListComponent,
		UserPreferencesComponent,
		UserEditPersonComponent
	],
	entryComponents: [
		UserPreferencesComponent,
		UserEditPersonComponent
	]
})

export class UserModule {

}