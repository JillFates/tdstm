import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LazyTestRouteModule} from './lazy-test-route.module';

// containers
import {LazyTestComponent} from './lazy-test.component';

@NgModule({
	imports: [
		CommonModule,
		LazyTestRouteModule
	],
	declarations: [
		LazyTestComponent
	]
})
export class LazyTestModule {
}