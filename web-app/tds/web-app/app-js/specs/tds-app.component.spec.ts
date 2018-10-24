import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {TDSAppComponent} from '../app/tds-app.component';
import {SharedModule} from '../shared/shared.module';

describe('TDSAppComponent:', () => {
	let fixture: ComponentFixture<TDSAppComponent>;
	let comp: TDSAppComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [SharedModule],
			declarations: [TDSAppComponent],
			providers: []
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(TDSAppComponent);
		comp = fixture.componentInstance;
		de = fixture.debugElement.query(By.css('h1'));
	});

	it('should create component', () => expect(comp).toBeDefined());

});