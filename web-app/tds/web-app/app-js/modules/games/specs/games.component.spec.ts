import {
    ComponentFixture,// Core class of a component: has control over methods, digest cycle, elements, injection and so on
    TestBed, //Acts like a NgModule to wrap the component as an application
    async, //Any component who has external resources(eg. html,css) or any asynchronous call should import this module
    ComponentFixtureAutoDetect//Not needed, only used to remove the need of fixture.detectChanges at start of the test
} from '@angular/core/testing'

import {
    By // Used to query html elements
} from '@angular/platform-browser'
import { FormsModule } from '@angular/forms';

import {
    DebugElement // Hold the instance to an HTML element you want to test
} from '@angular/core'

//after all these imports you should import what actually is gonna be tested
import { GameComponent } from '../components/games/games.component'
import { GameFormComponent } from '../components/games-form/games-form.component'
import { GameListComponent } from '../components/games-list/games-list.component'

import { Game } from '../model/game.model'

describe('GameComponent - Learning: Instance of the component', () => {
    let fixture: ComponentFixture<GameComponent>;
    let comp: GameComponent;

    //we need to use async here because this component requires external html and css
    beforeEach(async(() => {
        //similar to what you do when creating a NgModule
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameComponent, GameFormComponent, GameListComponent],
            providers: [{ provide: Game, useValue: {} }]//ignore this for now
        }).compileComponents();//this methods returns a promise
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameComponent);
        comp = fixture.componentInstance;
    });

    //there is a way to handle this async behavior with only one beforeEach call, but this seems a litte more tricky
    // beforeEach(async(() => {
    //     TestBed.configureTestingModule({
    //         declarations: [GameListComponent],
    //            providers: [{provide:Game,useValue:{}}]//ignore this for now
    //     }).compileComponents().then(result => { //Its a Promise right??
    //         fixture = TestBed.createComponent(GameListComponent);
    //         comp = fixture.componentInstance;
    //     })
    // }));


    it('should create component', () => expect(comp).toBeDefined());

});

describe('GameComponent - Learning: Instance of the component (one beforeEach)', () => {
    let fixture: ComponentFixture<GameComponent>;
    let comp: GameComponent;

    //there is a way to handle this async behavior with only one beforeEach call, but this seems a litte more tricky
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameComponent, GameFormComponent, GameListComponent],
            providers: [{ provide: Game, useValue: {} }]//ignore this for now
        }).compileComponents().then(result => { //Its a Promise right??
            fixture = TestBed.createComponent(GameComponent);
            comp = fixture.componentInstance;
        });
    }));

    it('should create component', () => expect(comp).toBeDefined());

});

describe("GameComponent - Learning: DOM Access", () => {
    let fixture: ComponentFixture<GameComponent>;
    let comp: GameComponent;
    let de: DebugElement; // this will hold our html element


    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameComponent, GameFormComponent, GameListComponent],
            providers: [{ provide: Game, useValue: {} }],//ignore this for now

        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should have an empty title at start', () => {
        const h1 = de.nativeElement;
        expect(h1.innerText).toBe('');
    });
    it('should have an an title after detecting the changes', () => {
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/games/i, 'Should say sometingh about "games"');
    });

    it('should reflect the changes made by the component', () => {
        comp.title = "Food you must eat";
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/food/i, 'Should say sometingh about "food"');
    });
});

describe('GameComponent - Learning: Fixture.detectChanges', () => {
    let fixture: ComponentFixture<GameComponent>;
    let comp: GameComponent;
    let de: DebugElement; // this will hold our html element

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameComponent, GameFormComponent, GameListComponent],
            providers: [{ provide: Game, useValue: {} },//ignore this for now
            { provide: ComponentFixtureAutoDetect, useValue: true }]//this enable automatic detectChanges at test startup//ignore this for now

        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should already has a title defined at startup', () => {
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/games/i, 'Should say sometingh about "games"');
        //remember: if you do any changes to propertys inside the component you still need to call fixture.detectChanges
    })
});

