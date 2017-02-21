import {
    ComponentFixture,// Core class of a component: has control over methods, digest cycle, elements, injection and so on
    TestBed, //Acts like a NgModule to wrap the component as an application
    async, //Any component who has external resources(eg. html,css) or any asynchronous call should import this module
    fakeAsync, //Not needed, another way to handle async, but in the same thread
    tick //Not needed, waits to all async call get resolved or simullates passage of time
} from '@angular/core/testing'

import {
    By // Used to query html elements
} from '@angular/platform-browser'
import { FormsModule } from '@angular/forms';

import {
    DebugElement // Hold the instance to an HTML element you want to test
} from '@angular/core'

//after all these imports you should import what actually is gonna be tested
import { GameFormComponent } from '../components/games-form/games-form.component'
import { GameService } from '../service/game.service'
import { Game } from '../model/game.model'

//if your component is receiving some object at its contructor you can use your own objects to test
describe('GameFormComponent - Learning: Stub values', () => {
    let fixture: ComponentFixture<GameFormComponent>;
    let comp: GameFormComponent;
    let de: DebugElement;
    let gameStub: Game = new Game("Zelda")

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameFormComponent],
            providers: [{ provide: Game, useValue: gameStub }, GameService]//passing your value
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameFormComponent);
        comp = fixture.componentInstance;
    });

    it('should already have the input value with stub variable', () => {
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            de = fixture.debugElement.query(By.css('input#Name'));
            expect(de.nativeElement.value).toBe('Zelda');
        }, () => { });
    });

});

describe('GameFormComponent - Learning: Form Validation', () => {
    let fixture: ComponentFixture<GameFormComponent>;
    let comp: GameFormComponent;
    let de: DebugElement;

    let gameService: GameService;
    let spySave: jasmine.Spy;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GameFormComponent],
            providers: [{ provide: Game, useValue: new Game("") }, GameService]//passing your value
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameFormComponent);
        comp = fixture.componentInstance;
        gameService = fixture.debugElement.injector.get(GameService);

        //save on the mockdata 
        spySave = spyOn(gameService, 'save').and.callFake((game: Game) => {
            return new Promise((resolve, reject) => {
                resolve(1);
            });
        });
    });

    it('should already have empty input at start', () => {
        fixture.detectChanges();
        de = fixture.debugElement.query(By.css('input#Name'));
        expect(de.nativeElement.value).toBe('');
    });

    it('should clear input when cancel is clicked', () => {
        fixture.detectChanges();

        comp.model.Name = "Zelda"
        fixture.detectChanges();

        fixture.whenStable().then(() => {

            fixture.detectChanges();
            de = fixture.debugElement.query(By.css('input#Name'));
            expect(de.nativeElement.value).toBe('Zelda');

            de = fixture.debugElement.query(By.css('#cancel'));
            de.triggerEventHandler('click', null);

            fixture.detectChanges();

            fixture.whenStable().then(() => {
                fixture.detectChanges();

                de = fixture.debugElement.query(By.css('input#Name'));
                expect(de.nativeElement.value).toBe('');
                expect(comp.model.Name).toBe('');
            }, () => { });

        }, () => { });

    });

    it('should not call save if have an empty input', () => {
        fixture.detectChanges();
        de = fixture.debugElement.query(By.css('input#Name'));
        expect(de.nativeElement.value).toBe('');

        de = fixture.debugElement.query(By.css('#submit'));
        de.triggerEventHandler('click', null);
        fixture.detectChanges();

        expect(spySave.calls.any()).toBe(false);
    });

    it('should call save if form is valid and reset afterwards', () => {
        fixture.detectChanges();

        comp.model.Name = "Zelda"
        fixture.detectChanges();

        fixture.whenStable().then(() => {

            fixture.detectChanges();
            de = fixture.debugElement.query(By.css('#submit'));
            de.triggerEventHandler('click', null);
            fixture.detectChanges();

            expect(spySave.calls.any()).toBe(true);
            fixture.whenStable().then(() => {
                 fixture.detectChanges();
                expect(comp.model.Name).toBe('');
            })
        }, () => { });

    });

});