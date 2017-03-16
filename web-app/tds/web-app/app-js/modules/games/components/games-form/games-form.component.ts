import { Component } from '@angular/core';
import { GameService } from '../../service/game.service';
import { Game } from '../../model/game.model';

@Component({
    moduleId: module.id,
    selector: 'games-form',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games-form/games-form.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games-form/games-form.component.css'],
    providers: []
})

export class GameFormComponent {
    model: Game;
    submitted = false;

    constructor(private gameService: GameService, private game: Game) {
        this.model = game || new Game('');
    }

    onSubmit(form): void {
        let ctrl = this;
        this.submitted = true;
        this.gameService.save(this.model).then(function (r) {
            ctrl.model = new Game('');
            form.reset();
            ctrl.submitted = false;
        });
    };

    newGame() { this.model = new Game(''); };

}