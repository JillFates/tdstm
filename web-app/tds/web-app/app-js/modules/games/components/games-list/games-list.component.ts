import { Component, OnInit } from '@angular/core';
import { GameService } from '../../service/game.service'
import { Game } from '../../model/game.model'

@Component({
    moduleId: module.id,
    selector: 'games-list',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games-list/games-list.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games-list/games-list.component.css'],
    providers: [GameService, { provide: Game, useValue: {} }]
})

export class GameListComponent implements OnInit {
    games: Array<Game>;
    newgame: boolean;
    title = "Games you should play";
    model = new Game("");
    submitted = false;

    constructor(private gameService: GameService, private game: Game) {
        this.model = game;
    }

    onRefresh(): void {
        this.gameService.query().then(result => {
            this.games = result;
        });
    };

    onSubmit(form): void {
        var ctrl = this;
        this.submitted = true;
        this.gameService.save(this.model).then(function (r) {
            return ctrl.gameService.query();
        }).then(function (data) {
            ctrl.games = data;
            ctrl.model = new Game("");
            form.reset();
            ctrl.submitted = false;
        });
    };

    newGame() { this.model = new Game(""); };

    ngOnInit(): void {
        this.gameService.query().then(result => {
            this.games = result;
        });
        this.newgame = this.game.Name == 'Default';
    };
}