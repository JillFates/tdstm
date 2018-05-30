/**
 * Compile the Application Dynamically Just-in-Time (JIT)
 * Application is using the JSDoc 3
 */

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {TDSAppModule} from './config/tds-app.module';

enableProdMode();

// Compile and launch the module
platformBrowserDynamic().bootstrapModule(TDSAppModule);