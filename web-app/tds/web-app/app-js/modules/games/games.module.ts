import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { GameListComponent }  from './games-list/games-list.component'

@NgModule({
  imports:      [ BrowserModule ],
  declarations: [ GameListComponent ],
  bootstrap:    [ GameListComponent]
})
export class GamesModule { }