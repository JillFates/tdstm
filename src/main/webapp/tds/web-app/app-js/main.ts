/**
 * Compile the Application Dynamically Just-in-Time (JIT)
 * Application is using the JSDoc 3
 */

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { TDSAppModule } from './app/tds-app.module';

declare var NODE_ENV: any;

if (NODE_ENV === 'production') {
	enableProdMode();
}

platformBrowserDynamic()
	.bootstrapModule(TDSAppModule)
	.then(ref => {
		if (window['ngRef']) {
			window['ngRef'].destroy();
		}
		window['ngRef'] = ref;
	})
	.catch(err => console.error(err));
