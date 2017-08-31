import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {TDSAppComponent} from '../config/tds-app.component';
import {UserService} from '../shared/services/user.service';
import {SharedModule} from '../shared/shared.module';

describe('TDSAppComponent:', () => {
	let fixture: ComponentFixture<TDSAppComponent>;
	let comp: TDSAppComponent;
	let de: DebugElement;
	let userStub: UserService = {
		userName: 'Bruce Wayne'
	};

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [SharedModule],
			declarations: [TDSAppComponent],
			providers: [{provide: UserService, useValue: userStub}]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(TDSAppComponent);
		comp = fixture.componentInstance;
		de = fixture.debugElement.query(By.css('h1'));
	});

	it('should create component', () => expect(comp).toBeDefined());

});