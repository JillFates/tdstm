import { Component, OnInit } from '@angular/core';
import { GameService } from '../shared/game.service'
import { Game } from '../shared/game.model'

@Component({
    moduleId: module.id,
    selector: 'games-list',
    templateUrl: './games-list.component.html',
    styleUrls: ['./games-list.component.css'],
    providers: [GameService, { provide: Game, useValue: {} }]
})

export class GameListComponent implements OnInit {
    games: Array<Game>;
    newgame: boolean;
    title = "Games you should play";
    constructor(private gameService: GameService, private game: Game) { }

    ngOnInit(): void {
        this.gameService.query().then(result => {
            this.games = result;
        });
        this.newgame = this.game.Name == 'Default';
    }
}