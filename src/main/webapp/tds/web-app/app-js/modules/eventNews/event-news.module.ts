// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
// Route Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
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

// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {EventNewsListComponent} from './components/list/event-news-list.component';
import {EventsResolveService} from '../event/resolve/events-resolve.service';
import {EventNewsRouteModule} from './event-news-routing.states';
import {EventNewsService} from './service/event-news.service';
import {EventsService} from '../event/service/events.service';
import {EventNewsViewEditComponent} from './components/view-edit/event-news-view-edit.component';
// Services

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
		EventNewsRouteModule
	],
	declarations: [
		EventNewsListComponent,
		EventNewsViewEditComponent,
	],
	providers: [
		// Resolve
		ModuleResolveService,
		EventsResolveService,
		// Service
		EventsService,
		EventNewsService
	],
	entryComponents: [
		EventNewsViewEditComponent,
	],
	exports: [
		EventNewsListComponent,
		EventNewsViewEditComponent,
	]
})

export class EventNewsModule {
}
