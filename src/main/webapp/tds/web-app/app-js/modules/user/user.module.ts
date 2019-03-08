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
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserDashboardComponent} from './components/dashboard/user-dashboard.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {UserResolveService} from './resolve/user-resolve.service';
// Services
import {UserService} from './service/user.service';

@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		FormsModule,
		AssetExplorerModule,
		// Kendo
		DropDownsModule,
		GridModule,
		// Route
		UserRouteModule
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
		UserDashboardComponent
	],
	exports: [
		UserListComponent
	],
	entryComponents: [
	]
})

export class UserModule {

}