/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TDSAppComponent }  from './tds-app.component';
import { HighlightDirective } from '../shared/directives/highlight.directive'
import { GamesModule } from '../modules/games/games.module'
import { UserService } from '../shared/services/user.service'

// Decorator that tells to Angular is a module.
@NgModule({
  imports:      [ BrowserModule, GamesModule ], // Means browser runnable, only add Angular Module here
  declarations: [ TDSAppComponent, HighlightDirective], // components, directives and pipes ONLY and only ONCE
  bootstrap:    [ TDSAppComponent ], // Contains the root component that is being injected on the index.html
  providers: [UserService]
})

export class TDSAppModule { }
