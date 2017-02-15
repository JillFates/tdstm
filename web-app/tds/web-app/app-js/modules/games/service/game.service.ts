import { Injectable } from '@angular/core';
import { Game } from '../model/game.model'

@Injectable()
export class GameService {
    private games: Array<Game> = [];

    constructor() {
        this.games.push(new Game("Last of us"));
        this.games.push(new Game("Uncharted"));
        this.games.push(new Game("Dark Souls 3"));
        this.games.push(new Game("Red dead redemption"));
    };

    query(): Promise<Array<Game>> {
        return new Promise((resolve, reject) => {
            setInterval(() => { resolve(this.games) }, 2000);
        });
    };

    findById(id: number):Promise<Game> {
        return new Promise((resolve, reject) => {
            setInterval(() => {
                if (id < this.games.length)
                    resolve(this.games[id]);
                else
                    reject("Game not found.");
            }, 2000)
        });
    };

    save(game: Game):Promise<number> {
        return new Promise((resolve, reject) => {
            setInterval(() => {
                this.games.push(game);
                resolve(this.games.length);
            }, 2000);
        });
    };

    delete(id: number):Promise<string> {
        return new Promise((resolve, reject) => {
            setInterval(() => {
                this.games.splice(id, 1);
                resolve("Ok");
            }, 2000);
        });
    }
}