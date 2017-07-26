/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SharedModule } from '../../../shared/shared.module';

import { FieldSettingsImportanceComponent } from '../components/imp/field-settings-imp.component';

describe('FieldSettingsImportanceComponent:', () => {
	let fixture: ComponentFixture<FieldSettingsImportanceComponent>;
	let comp: FieldSettingsImportanceComponent;
	let de: DebugElement;

	let editMode = true;
	let model = 'I';
	let modelChange = (newValue) => {
		comp.model = newValue;
		model = comp.model;
	};
	let changeSpy: jasmine.Spy;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [FormsModule],
			declarations: [FieldSettingsImportanceComponent],
			providers: []
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(FieldSettingsImportanceComponent);
		comp = fixture.componentInstance;
		comp.model = model;
		comp.editMode = editMode;
		spyOn(comp, 'onModelChange').and.callFake(modelChange);
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should change model value on click', () => {
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('div span:first-child'));
		de.triggerEventHandler('click', null);
		fixture.detectChanges();
		expect(comp.model).toBe('C');
		expect(model).toBe(comp.model);
	});

	it('should change div class on click', () => {
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('div span:last-child'));
		de.triggerEventHandler('click', null);
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('div'));
		expect(de.classes['U']).toBeTruthy();
	});

});