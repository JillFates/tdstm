import {NgModule} from '@angular/core';
import {LazyTestRouteModule} from './lazy-test-route.module';

// containers
import {LazyTestComponent} from './lazy-test.component';

@NgModule({
	imports: [
		LazyTestRouteModule
	],
	declarations: [
		LazyTestComponent
	]
})
export class LazyTestModule {
}