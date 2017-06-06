/**
 * Created by daviD on 04/06/2017.
 */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SelectListConfigurationPopupComponent} from '../components/popup/selectlist-configuration-popup.component';
import {DebugElement} from '@angular/core';
import {PopupModule} from '@progress/kendo-angular-popup';
import {SortableModule} from '@progress/kendo-angular-sortable';
import {FormsModule} from '@angular/forms';
import {SharedModule} from '../../../shared/shared.module';
import {By} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FieldSettingsModel} from '../model/field-settings.model';

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
		required: true,
		show: true,
		control: 'Select',
		option: []
	};

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				BrowserAnimationsModule,
				SharedModule,
				FormsModule,
				PopupModule,
				SortableModule],
			declarations: [SelectListConfigurationPopupComponent],
			providers: []
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(SelectListConfigurationPopupComponent);
		comp = fixture.componentInstance;
	});

	it('should create the component', () => {
		expect(comp).toBeDefined();
		expect(comp === null).toBeFalsy();
	});

	it('should show the popup on load', () => {
		expect(comp.show).toBeTruthy();
	});

	it('should add new item to list', () => {
		fixture.detectChanges();
		let addButton: DebugElement;
		addButton = fixture.debugElement.query(By.css('button[name=addButton]'));
		comp.newItem = 'Foo';
		expect(addButton === null).toBeFalsy();
		addButton.triggerEventHandler('click', null);
		expect(comp.items.length).toEqual(1);
		expect(comp.items[0]).toEqual('Foo');
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

		comp.field = mockField;

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
		expect(comp.field.option.length).toEqual(1);
		expect(comp.field.option[0]).toEqual('Foo');

	});
});