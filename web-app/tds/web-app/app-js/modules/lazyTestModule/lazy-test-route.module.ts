import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
// Components
import {LazyTestComponent} from './lazy-test.component';

// routes
export const LazyTestRoute: Routes = [
	{path: '', component: LazyTestComponent}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LazyTestRoute)]
})

export class LazyTestRouteModule {}