// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ArchitectureGraphComponent} from './components/architecture-graph/architecture-graph.component';

/**
 * Asset Manager Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Manager Module
 */
export class AssetExplorerStates {
	public static readonly ARCHITECTURE_GRAPH = {
		url: 'architecture-graph'
	};
	public static readonly DEPENDENCY_ANALYZER = {
		url: 'dependency-analyzer'
	};
}

/**
 * Top menu parent section class for all Assets Manager module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export const AssetExplorerRoute: Routes = [
	{
		path: AssetExplorerStates.ARCHITECTURE_GRAPH.url,
		data: {
			page: {
				title: 'ARCHITECTURE.ARCHITECTURE_GRAPH', instruction: '',
				menu: ['ASSETS.ASSETS', 'ARCHITECTURE.GOJSARCHITECTUREGRAPH'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-architecture-graph-gojs', subMenu: false }
			}
		},
		component: ArchitectureGraphComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	},
	{
		path: AssetExplorerStates.DEPENDENCY_ANALYZER.url,
		data: {
			page: {
				title: 'DEPENDENCY_ANALYZER.DEPENDENCY_ANALYZER', instruction: '',
				menu: ['ASSETS.ASSETS', 'DEPENDENCY_ANALYZER.DEPENDENCY_ANALYZER'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-architecture-graph-gojs', subMenu: false }
			}
		},
		component: ArchitectureGraphComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetExplorerRoute)]
})

export class AssetExplorerrRouteModule {
}
