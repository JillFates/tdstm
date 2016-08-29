'use strict';
var  Menu = require('./menu.po.js');
describe('Admin Menu', function() {
  var menu = new Menu();

  it('should have \'Admin\' as the parent label', function() {
    expect(menu.admin.getParent().getText()).toEqual('Admin');
  });

	it('should expand the Admin Menu', function() {
		menu.admin.toggle();
		expect(menu.admin.isExpanded()).toBe(true);
	});

  describe('Child Items',function() {

		it('should have the text \'Admin Portal\' as its link text', function() {
			expect(menu.admin.getAdminPortal().findElement(by.tagName('a')).getText())
				.toMatch(/Admin Portal/);
		});

		it('should have the target URL \'/tdstm/admin/home\'', function() {
			expect(menu.admin.getAdminPortal().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/admin/home');
		});

		it('should have the text \'Role Permissions\' as its link text', function() {
			expect(menu.admin.getRolePermissions().findElement(by.tagName('a')).getText())
				.toMatch(/Role Permissions/);
		});

		it('should have the target URL \'/tdstm/permissions/show\'', function() {
			expect(menu.admin.getRolePermissions().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/permissions/show');
		});

		it('should have the text \'Asset Options\' as its link text', function() {
			expect(menu.admin.getAssetOptions().findElement(by.tagName('a')).getText())
				.toMatch(/Asset Options/);
		});

		it('should have the target URL \'/tdstm/assetEntity/assetOptions\'', function() {
			expect(menu.admin.getAssetOptions().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/assetOptions');
		});

		it('should have the text \'List Companies\' as its link text', function() {
			expect(menu.admin.getListCompanies().findElement(by.tagName('a')).getText())
				.toMatch(/List Companies/);
		});

		it('should have the target URL \'/tdstm/partyGroup/list?active=active&tag_s_2_name=asc\'', function() {
			expect(menu.admin.getListCompanies().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/partyGroup/list');
		});

		it('should have the text \'List Staff\' as its link text', function() {
			expect(menu.admin.getListStaff().findElement(by.tagName('a')).getText())
				.toMatch(/List Staff/);
		});

		it('should have the target URL \'/tdstm/person/index\'', function() {
			expect(menu.admin.getListStaff().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/person/index');
		});

		it('should have the text \'List Users\' as its link text', function() {
			expect(menu.admin.getListUsers().findElement(by.tagName('a')).getText())
				.toMatch(/List Users/);
		});

		it('should have the target URL \'/tdstm/userLogin/index\'', function() {
			expect(menu.admin.getListUsers().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/userLogin/index');
		});

		it('should have the text \'Import Accounts\' as its link text', function() {
			expect(menu.admin.getImportAccounts().findElement(by.tagName('a')).getText())
				.toMatch(/Import Accounts/);
		});

		it('should have the target URL \'/tdstm/admin/importAccounts\'', function() {
			expect(menu.admin.getImportAccounts().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/admin/importAccounts');
		});

		it('should have the text \'Export Accounts\' as its link text', function() {
			expect(menu.admin.getExportAccounts().findElement(by.tagName('a')).getText())
				.toMatch(/Export Accounts/);
		});

		it('should have the target URL \'/tdstm/admin/exportAccounts\'', function() {
			expect(menu.admin.getExportAccounts().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/admin/exportAccounts');
		});

		it('should have the text \'List Workflows\' as its link text', function() {
			expect(menu.admin.getListWorkflows().findElement(by.tagName('a')).getText())
				.toMatch(/List Workflows/);
		});

		it('should have the target URL \'/tdstm/workflow/home\'', function() {
			expect(menu.admin.getListWorkflows().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/workflow/home');
		});

		it('should have the text \'List Manufacturers\' as its link text', function() {
			expect(menu.admin.getListManufacturers().findElement(by.tagName('a')).getText())
				.toMatch(/List Manufacturers/);
		});

		it('should have the target URL \'/tdstm/manufacturer/index\'', function() {
			expect(menu.admin.getListManufacturers().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/manufacturer/index');
		});

		it('should have the text \'List Models\' as its link text', function() {
			expect(menu.admin.getListModels().findElement(by.tagName('a')).getText())
				.toMatch(/List Models/);
		});

		it('should have the target URL \'/tdstm/model/index\'', function() {
			expect(menu.admin.getListModels().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/model/index');
		});

		it('should have the text \'Sync Libraries\' as its link text', function() {
			expect(menu.admin.getSyncLibraries().findElement(by.tagName('a')).getText())
				.toMatch(/Sync Libraries/);
		});

		it('should have the target URL \'/tdstm/model/importExport\'', function() {
			expect(menu.admin.getSyncLibraries().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/model/importExport');
		});

  }); // end Child Items

	it('should close the Admin Menu', function() {
		menu.admin.toggle();
		expect(menu.admin.isExpanded()).toBe(false);
	});
	
}); // end Admin Menu
