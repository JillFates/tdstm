/**
 * Created by daviD on 04/06/2017.
 */

import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {Observable} from 'rxjs';
import {HttpModule, Http} from '@angular/http';

import {PopupModule} from '@progress/kendo-angular-popup';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {DialogModule} from '@progress/kendo-angular-dialog';

import {SelectListConfigurationPopupComponent} from '../components/select-list/selectlist-configuration-popup.component';
import {SharedModule} from '../../../shared/shared.module';
import {FieldSettingsModel} from '../model/field-settings.model';
import {CustomDomainService} from '../service/custom-domain.service';

describe('SelectListConfigurationPopupComponent:', () => {
	let fixture: ComponentFixture<SelectListConfigurationPopupComponent>;
	let comp: SelectListConfigurationPopupComponent;
	let mockField: FieldSettingsModel = {
		field: 'string',
		label: 'string',
		tip: 'string',
		udf: true,
		shared: true,
		imp: 'C',
		constraints: {
			required: true,
			values: []
		},
		show: true,
		control: 'List'
	};

	let spy: jasmine.Spy;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				BrowserAnimationsModule,
				HttpModule,
				SharedModule,
				FormsModule,
				PopupModule,
				SortableModule,
				DialogModule
			],
			declarations: [SelectListConfigurationPopupComponent],
			providers: [CustomDomainService]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(SelectListConfigurationPopupComponent);
		comp = fixture.componentInstance;
		comp.field = mockField;
		let customService = fixture.debugElement.injector.get(CustomDomainService);
		spy = spyOn(customService, 'getDistinctValues').and.callFake(() => {
			return Observable.from([]).bufferCount(1);
		});
	});

	it('should create the component', () => {
		expect(comp).toBeDefined();
	});

	it('should not show the popup on load', () => {
		expect(comp.show).toBeFalsy();
	});

	it('should add new item to list', () => {
		fixture.detectChanges();
		let addButton: DebugElement;
		addButton = fixture.debugElement.query(By.css('button[name=addButton]'));
		comp.newItem = 'Foo';
		expect(addButton === null).toBeFalsy();
		addButton.triggerEventHandler('click', null);
		expect(comp.items.length).toEqual(1);
		expect(comp.items[0].value).toEqual('Foo');
	});

	it('should add & remove existing item from list', () => {
		fixture.detectChanges();
		let addButton: DebugElement;
		addButton = fixture.debugElement.query(By.css('button[name=addButton]'));
		comp.newItem = 'Foo';
		addButton.triggerEventHandler('click', null);
		expect(comp.items.length).toEqual(1);

		fixture.detectChanges();
		let removeButton: DebugElement;
		removeButton = fixture.debugElement.query(By.css('i.fa.fa-fw.fa-trash'));
		removeButton.triggerEventHandler('click', null);
		expect(comp.items.length).toEqual(0);
	});

	it('should save list', () => {
		fixture.detectChanges();
		let addButton: DebugElement;
		addButton = fixture.debugElement.query(By.css('button[name=addButton]'));
		comp.newItem = 'Foo';
		addButton.triggerEventHandler('click', null);
		expect(comp.items.length).toEqual(1);

		fixture.detectChanges();
		let saveButton: DebugElement;
		saveButton = fixture.debugElement.query(By.css('button[name=saveButton]'));
		saveButton.triggerEventHandler('click', null);
		expect(comp.field.constraints.values.length).toEqual(1);
		expect(comp.field.constraints.values[0]).toEqual('Foo');
	});

	it('should order the items', () => {
		fixture.detectChanges();
		comp.items = [
			{deletable: true, value: 'Z'},
			{deletable: true, value: 'P'},
			{deletable: true, value: 'E'},
			{deletable: true, value: 'A'},
			{deletable: true, value: 'C'}
		];

		let sortButton: DebugElement;
		sortButton = fixture.debugElement.query(By.css('button[name=sortButton]'));
		sortButton.triggerEventHandler('click', null);

		expect(comp.items[0].value).toEqual('A');
		expect(comp.items[1].value).toEqual('C');
		expect(comp.items[2].value).toEqual('E');
		expect(comp.items[3].value).toEqual('P');
		expect(comp.items[4].value).toEqual('Z');
	});
});