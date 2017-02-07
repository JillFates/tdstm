import { NgModule }      from '@angular/core';
import { CommonModule }       from '@angular/common';

import { GameListComponent }  from './games-list/games-list.component'

@NgModule({
  imports:      [ CommonModule ],
  declarations: [ GameListComponent ],
  exports:    [ GameListComponent]
})
export class GamesModule { }