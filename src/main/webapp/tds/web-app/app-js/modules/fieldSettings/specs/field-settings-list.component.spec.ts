/**
 * Created by aaferreira on 13/02/2017.
 */
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { HttpModule, Http } from '@angular/http';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
// Kendo Components
import { GridModule } from '@progress/kendo-angular-grid';
import { PopupModule } from '@progress/kendo-angular-popup';
import { SortableModule } from '@progress/kendo-angular-sortable';
import { DialogModule } from '@progress/kendo-angular-dialog';
import { InputsModule } from '@progress/kendo-angular-inputs';

import { SharedModule } from '../../../shared/shared.module';
import { HttpServiceProvider } from '../../../shared/providers/http-interceptor.provider';
import { NotifierService } from '../../../shared/services/notifier.service';

import { FieldSettingsListComponent } from '../components/list/field-settings-list.component';
import { FieldSettingsGridComponent } from '../components/grid/field-settings-grid.component';
import { SelectListConfigurationPopupComponent } from '../components/select-list/selectlist-configuration-popup.component';
import { MinMaxConfigurationPopupComponent } from '../components/min-max/min-max-configuration-popup.component';
import { FieldSettingsImportanceComponent } from '../components/imp/field-settings-imp.component';
import { FieldSettingsService } from '../service/field-settings.service';
import { DomainModel } from '../model/domain.model';

describe('FieldSettingsListComponent:', () => {
	let fixture: ComponentFixture<FieldSettingsListComponent>;
	let comp: FieldSettingsListComponent;
	let de: DebugElement;

	let mockData: DomainModel[] = [
		{
			domain: 'APPLICATION',
			fields: [{
				field: 'string',
				label: 'string',
				tip: 'string',
				udf: true,
				shared: true,
				imp: 'C',
				constraints: {
					required: true
				},
				show: true
			}]
		},
		{
			domain: 'DEVICE',
			fields: [{
				field: 'string',
				label: 'string',
				tip: 'string',
				udf: true,
				shared: true,
				imp: 'C',
				constraints: {
					required: true
				},
				show: true
			}]
		}
	];

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [HttpModule,
				FormsModule,
				SharedModule,
				GridModule,
				PopupModule,
				SortableModule,
				DialogModule,
				InputsModule
			],
			declarations: [
				FieldSettingsListComponent,
				FieldSettingsGridComponent,
				FieldSettingsImportanceComponent,
				SelectListConfigurationPopupComponent,
				MinMaxConfigurationPopupComponent
			],
			providers: [FieldSettingsService, HttpServiceProvider,
				NotifierService,
				{ provide: 'fields', useValue: Observable.from(mockData).bufferCount(mockData.length) }
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(FieldSettingsListComponent);
		comp = fixture.componentInstance;
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should create tabs based on domain model length', () => {
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('.nav-tabs'));
		expect(de.children.length).toBe(mockData.length);
	});
});