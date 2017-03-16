import { Component, OnInit } from '@angular/core';
import { GameService } from '../../service/game.service';
import { Game } from '../../model/game.model';

@Component({
    moduleId: module.id,
    selector: 'games-list',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games-list/games-list.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games-list/games-list.component.css'],
    providers: []
})
export class GameListComponent implements OnInit {
    games: Array<Game>;

    constructor(private gameService: GameService) {
    }

    onRefresh(): void {
        this.gameService.query().then(result => {
            this.games = result;
        });
    };

    ngOnInit(): void {
        this.gameService.query().then(result => {
            this.games = result;
        });
    };
}