// Angular
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
// Shared
import {SharedModule} from '../../shared/shared.module';
import {TranslatePipe} from '../../shared/pipes/translate.pipe';
// Assets Module
import {AssetExplorerModule} from '../assetExplorer/asset-explorer.module';
// Route Module
import {DependenciesRouteModule} from './dependencies-routing.states';
// Kendo
import {DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {GridModule} from '@progress/kendo-angular-grid';
import {PopupModule} from '@progress/kendo-angular-popup';
import {InputsModule} from '@progress/kendo-angular-inputs';
import { SortableModule } from '@progress/kendo-angular-sortable';
import { IntlModule } from '@progress/kendo-angular-intl';
import {DateInputsModule} from '@progress/kendo-angular-dateinputs';
// Components
import {DependenciesViewGridComponent} from './components/view-grid/dependencies-view-grid.component';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {TagsResolveService} from '../assetExplorer/resolve/tags-resolve.service';
// Services
import {DependenciesService} from './service/dependencies.service';
import {OpenAssetDependenciesService} from './service/open-asset-dependencies.service';
import { TagService } from '../assetTags/service/tag.service';

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
		// Route
		DependenciesRouteModule
	],
	declarations: [
		DependenciesViewGridComponent
	],
	providers: [
		// Resolve
		TagsResolveService,
		ModuleResolveService,
		DependenciesService,
		OpenAssetDependenciesService,
		// Services
		TagService,
		TranslatePipe
	],
	exports: [DependenciesViewGridComponent],
	entryComponents: []
})

export class DependenciesModule {
}