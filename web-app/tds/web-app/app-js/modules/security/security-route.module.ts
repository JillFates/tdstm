import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
// Components
import {LazyTestComponent} from './lazy-test.component';

// routes
export const LazyTestRoute: Routes = [
	{
		path: '',
		data: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER'],
			topMenu: { parent: 'menu-parent-assets', child: 'menu-parent-assets-asset-manager', subMenu: true }
		},
		component: LazyTestComponent
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LazyTestRoute)]
})

export class SecurityRouteModule {}