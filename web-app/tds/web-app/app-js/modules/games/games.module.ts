import { NgModule }      from '@angular/core';
import { CommonModule }       from '@angular/common';

import { GameListComponent }  from './games-list/games-list.component'
import { GamesRoutingModule } from './games-routing.module';

@NgModule({
  imports:      [ CommonModule, GamesRoutingModule ],
  declarations: [ GameListComponent ],
  exports:    [ GameListComponent]
})
export class GamesModule { }