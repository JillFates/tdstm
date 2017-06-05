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

describe('SelectListConfigurationPopupComponent:', () => {
	let fixture: ComponentFixture<SelectListConfigurationPopupComponent>;
	let comp: SelectListConfigurationPopupComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
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

	it('should create the component', () => expect(comp).toBeDefined());

	it('should show the Select List configuration popup on load', () => {
		console.log(comp);
		expect(comp.show).toBeTruthy();
	});
});