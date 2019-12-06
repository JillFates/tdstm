// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// import {ArchitectureGraphRouteModule} from './architecture-graph-routing.states';
// Kendo Module
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {IntlModule} from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs'
import {ContextMenuModule} from '@progress/kendo-angular-menu';
import {LayoutModule} from '@progress/kendo-angular-layout';
// Components

// import {EventViewEditComponent} from './components/view-edit/event-view-edit.component';

// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
// import {EventsService} from './service/events.service';
// import {EventsResolveService} from './resolve/events-resolve.service';

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
		InputsModule,
		DateInputsModule,
		SortableModule,
		IntlModule,
		InputsModule,
		DateInputsModule,
		ContextMenuModule,
		LayoutModule,
		// Route
		// EventRouteModule
	],
	declarations: [
		// EventListComponent,
	],
	providers: [
		// Resolve
		ModuleResolveService,
		// EventsResolveService,
		// // Service
		// EventsService
	],
	entryComponents: [
		// EventCreateComponent,
	],
	exports: [
		// EventListComponent,
	]
})

export class ArchitectureGraphModule {
}
