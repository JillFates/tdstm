import { Component } from '@angular/core';
import { GameService } from '../../service/game.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { GameDetailComponent } from '../games-detail/games-detail.component';
import { Game } from '../../model/game.model';

@Component({
    moduleId: module.id,
    selector: 'games',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games/games.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games/games.component.css'],
    providers: [GameService]
})

export class GameComponent {
    title = 'Games you should play';
    message = '';
    constructor(private dialogService: UIDialogService) {

    }

    open(): void {
        this.dialogService.open(GameDetailComponent,
            [{ provide: Game, useValue: new Game('Dark Souls') }])
            .then(value => {
                this.message = 'Success:' + value;
            }, error => {
                this.message = 'Error:' + error;
            });
    }

}