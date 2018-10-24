/**
 * Created by aaferreira on 15/02/2017.
 */
import { PermissionService } from '../shared/services/permission.service';
import { EmptyComponent } from '../testing/empty.component';
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { HttpModule } from '@angular/http';
import { SharedModule } from '../shared/shared.module';
import { Observable } from 'rxjs';

// describe('PermissionService:', () => {
// 	let fixture: ComponentFixture<EmptyComponent>;
// 	let comp: EmptyComponent;
// 	let permissionService: PermissionService;
// 	let spyGet: jasmine.Spy;
//
// 	beforeEach(async(() => {
// 		TestBed.configureTestingModule({
// 			imports: [HttpModule, SharedModule],
// 			declarations: [EmptyComponent],
// 			providers: []
// 		}).compileComponents();
// 	}));
//
// 	beforeEach(() => {
// 		fixture = TestBed.createComponent(EmptyComponent);
// 		comp = fixture.componentInstance;
// 		permissionService = fixture.debugElement.injector.get(PermissionService);
// 		spyGet = spyOn(permissionService, 'getPermissions')
// 			.and.callFake(() => {
// 				let observable = Observable.from([{
// 					ProjectFieldSettingsEdit: 'ProjectFieldSettingsEdit',
// 					ProjectFieldSettingsView: 'ProjectFieldSettingsView'
// 				}]);
// 				observable.subscribe((res) => {
// 					permissionService.permissions = res;
// 				});
// 				return observable;
// 			});
// 	});
//
// 	it('should not have permissions at start and return false on permission method', () => {
// 		expect(permissionService.permissions).toBeUndefined();
// 		expect(spyGet.calls.any()).toBeFalsy();
// 		expect(permissionService.hasPermission('ProjectFieldSettingsView')).toBeFalsy();
// 	});
//
// 	it('should define permissions and return true on permission method', done => {
// 		permissionService.getPermissions();
// 		spyGet.calls.mostRecent().returnValue.subscribe(
// 			(res) => {
// 				expect(permissionService.permissions).toBeDefined();
// 				expect(permissionService.hasPermission('ProjectFieldSettingsView')).toBeTruthy();
// 				done();
// 			}
// 		);
// 	});
//
// });