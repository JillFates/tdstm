/**
 * Compile the Application Dynamically Just-in-Time (JIT)
 */

// The browser platform with a compiler
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { TDSAppModule } from './config/tds-app.module';

// Compile and launch the module
platformBrowserDynamic().bootstrapModule(TDSAppModule);