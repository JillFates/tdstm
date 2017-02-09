/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TDSAppComponent }  from './tds-app.component';
// Shared Services
import { UserService } from '../shared/services/user.service'
// Shared Directives
import { HighlightDirective } from '../shared/directives/highlight.directive'
// Feature modules
import { GamesModule } from '../modules/games/games.module'
import { NoticesManagerModule } from '../modules/noticeManager/notices-manager.module'
// Router Logic
import { TDSRoutingModule } from './tds-routing.module';


// Decorator that tells to Angular is a module.
@NgModule({
    imports: [
        // Angular Modules
        BrowserModule,
        // Featured Modules
        GamesModule, NoticesManagerModule,
        // Routing Modules
        TDSRoutingModule
    ],
    declarations: [TDSAppComponent, HighlightDirective], // components, directives and pipes ONLY and only ONCE
    bootstrap: [TDSAppComponent], // Contains the root component that is being injected on the index.html
    providers: [UserService]
})

export class TDSAppModule {
}
