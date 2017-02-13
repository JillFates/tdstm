/**
 * Compile the Application Dynamically Just-in-Time (JIT)
 */

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { environment } from './environment/environment';
import { TDSAppModule } from './config/tds-app.module';

if (environment.production) {
    enableProdMode();
}

// Compile and launch the module
platformBrowserDynamic().bootstrapModule(TDSAppModule);