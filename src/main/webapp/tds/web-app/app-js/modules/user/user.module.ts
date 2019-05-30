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
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
import {UserRouteModule} from './user-routing.states';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs'
import {ContextMenuModule} from '@progress/kendo-angular-menu';
// Components
import {UserListComponent} from './components/list/user-list.component';
import {UserDashboardComponent} from './components/dashboard/user-dashboard.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {UserResolveService} from './resolve/user-resolve.service';
// Services
import {UserService} from './service/user.service';
import {UserPostNoticesManagerService} from './service/user-post-notices-manager.service';
import {UserPostNoticesService} from './service/user-post-notices.service';
import {NoticesValidatorService} from './service/notices-validator.service';


@NgModule({
	imports: [
		// Angular
		CommonModule,
		SharedModule,
		AssetExplorerModule,
		FormsModule,
		// Kendo
		DropDownsModule,
		GridModule,
		PopupModule,
		SortableModule,
		IntlModule,
		InputsModule,
		DateInputsModule,
		ContextMenuModule,
		// Route
		UserRouteModule
	],
	declarations: [
		UserListComponent,
		UserDashboardComponent
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
	exports: [
		UserListComponent
	],
	entryComponents: [
	]
})

export class UserModule {

}