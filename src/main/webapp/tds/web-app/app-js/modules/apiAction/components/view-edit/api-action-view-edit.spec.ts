import {TestBed, async, fakeAsync, tick, ComponentFixture} from '@angular/core/testing';
import {DropDownListComponent, DropDownsModule} from '@progress/kendo-angular-dropdowns';
import {By} from '@angular/platform-browser';
import {CommonModule} from '@angular/common';
import {ElementRef, EventEmitter, NO_ERRORS_SCHEMA, Renderer2} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {GridModule} from '@progress/kendo-angular-grid';
import {HttpClientModule} from '@angular/common/http';
import {NgxsModule} from '@ngxs/store';
import {of} from 'rxjs';

import {APIActionViewEditComponent} from './api-action-view-edit.component';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {APIActionModel} from '../../model/api-action.model';
import {APIActionService} from '../../service/api-action.service';
import {CustomDomainService} from '../../../fieldSettings/service/custom-domain.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PermissionService} from '../../../../shared/services/permission.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {en_DICTIONARY} from '../../../../shared/i18n/en.dictionary';
import {ProviderModel} from '../../../provider/model/provider.model';
import {DictionaryModel} from '../../model/agent.model';
import {NumericTextBoxModule} from '@progress/kendo-angular-inputs';
import {DomainModel} from '../../../fieldSettings/model/domain.model';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {TDSModalPageWrapperComponent} from '../../../../shared/components/modal-page-wrapper/modal-page-wrapper.component';

describe('APIActionViewEditComponent', () => {

	const dictionaryModel: DictionaryModel[] = [
		{
			id: 1,
			name: 'cloud'
		},
		{
			id: 2,
			name: 'cloudx'
		}
	];
	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [
				CommonModule,
				FormsModule,
				GridModule,
				DropDownsModule,
				NumericTextBoxModule,
				HttpClientModule,
				NgxsModule.forRoot(),
				BrowserAnimationsModule
			],
			declarations: [
				APIActionViewEditComponent,
				TranslatePipe
			],
			providers: [
				APIActionModel,
				APIActionService,
				CustomDomainService,
				UIPromptService,
				PermissionService,
				UIActiveDialogService,
				UIPromptService,
				NotifierService,
				TranslatePipe,
				{
					provide: 'localizedDictionary',
					useValue: en_DICTIONARY
				}
			],
			schemas: [NO_ERRORS_SCHEMA]
		}).compileComponents();
	}));

	function setup() {
		const fixture = TestBed.createComponent(APIActionViewEditComponent);
		const comp = fixture.debugElement.componentInstance;
		const apiActionService = fixture.debugElement.injector.get(APIActionService);
		const customDomainService = fixture.debugElement.injector.get(CustomDomainService);
		const promptService = fixture.debugElement.injector.get(UIPromptService);

		spyOn(promptService, 'open').and.returnValue(Promise.resolve('Prompt called'));
		spyOn(comp, 'getProviders').and.callFake(() => 'fake Call');
		spyOn(comp, 'getAgents').and.callFake(() => 'fake Call');
		spyOn(comp, 'getCommonFieldSpecs').and.callFake(() => 'fake Call');
		spyOn(comp, 'loadDictionaryModel').and.callFake(() => 'fake Call');
		spyOn(comp, 'getCredentials').and.callFake(() => 'fake Call');
		spyOn(comp, 'getDataScripts').and.callFake(() => 'fake Call');
		spyOn(apiActionService, 'getProviders').and.callFake(() => 'fake Call');
		spyOn(apiActionService, 'getAPIActionEnums').and.callFake(() => 'fake Call');
		spyOn(apiActionService, 'getCredentials').and.callFake(() => 'fake Call');
		spyOn(apiActionService, 'getDataScripts').and.callFake(() => 'fake Call');
		spyOn(customDomainService, 'getCommonFieldSpecs').and.callFake(() => 'fake Call');
		spyOn(customDomainService, 'getFieldSpecsPerDomain').and.callFake(() => 'fake Call');
		spyOn(customDomainService, 'getCommonFieldSpecsWithShared').and.callFake(() => 'fake Call');

		return { fixture, comp, apiActionService, customDomainService, promptService };
	}

	describe('OnDictionaryValueChange behaviour', () => {
		it('should prompt the confirmation dialog', fakeAsync(() => {

			const { fixture, comp, promptService } = setup();

			tick(500);
			fixture.detectChanges();
			tick(500);

			comp.modalType = 2;
			comp.lastSelectedDictionaryModel = dictionaryModel[0];

			comp.onDictionaryValueChange(dictionaryModel[0]);

			expect(promptService.open).toHaveBeenCalled();
		}));

		it('should not prompt confirmation dialog', fakeAsync(() => {

			const { fixture, comp, promptService } = setup();

			tick(500);
			fixture.detectChanges();
			tick(500);

			comp.modalType = 0;
			comp.lastSelectedDictionaryModel = dictionaryModel[0];

			comp.onDictionaryValueChange(dictionaryModel[0]);

			expect(promptService.open).not.toHaveBeenCalled();
		}));
	});

	describe('OnMethodValueChange behaviour', () => {
		it('should prompt the confirmation dialog', fakeAsync(() => {

			const { fixture, comp, promptService } = setup();

			tick(500);
			fixture.detectChanges();
			tick(500);

			comp.modalType = 2;
			comp.lastSelectedAgentMethodModel = dictionaryModel[0];

			comp.onMethodValueChange(dictionaryModel[0]);

			expect(promptService.open).toHaveBeenCalled();
		}));

		it('should not prompt confirmation dialog', fakeAsync(() => {

			const { fixture, comp, promptService } = setup();

			tick(500);
			fixture.detectChanges();
			tick(500);

			comp.modalType = 0;
			comp.lastSelectedAgentMethodModel = dictionaryModel[0];

			comp.onMethodValueChange(dictionaryModel[0]);

			expect(promptService.open).not.toHaveBeenCalled();
		}));
	});
});
