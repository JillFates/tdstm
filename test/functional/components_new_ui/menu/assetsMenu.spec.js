'use strict';
var  Menu = require('./menu.po.js');
describe('Assets Menu', function(){

  var menu = new Menu();

  it('should have \'Assets\' as the parent label', function() {
    expect(menu.assets.getParent().getText()).toEqual('Assets');
  });

	it('should expand the Assets Menu',function(){
		menu.assets.toggle();
		expect(menu.assets.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'Summary Table\' as its link text', function() {
			expect(menu.assets.getSummaryTable().findElement(by.tagName('a')).getText())
				.toMatch(/Summary Table/);
		});

		it('should have the target URL \'/tdstm/assetEntity/assetSummary\'', function() {
			expect(menu.assets.getSummaryTable().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/assetSummary');
		});

		it('should have the text \'Applications\' as its link text', function() {
			expect(menu.assets.getApplications().findElement(by.tagName('a')).getText())
				.toMatch(/Applications/);
		});

		it('should have the target URL \'/tdstm/application/list\'', function() {
			expect(menu.assets.getApplications().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/application/list');
		});

		it('should have the text \'Servers\' as its link text', function() {
			expect(menu.assets.getServers().findElement(by.tagName('a')).getText())
				.toMatch(/Servers/);
		});

		it('should have the target URL \'/tdstm/assetEntity/list?filter=server\'', function() {
			expect(menu.assets.getServers().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/list?filter=server');
		});

		it('should have the text \'All Devices\' as its link text', function() {
			expect(menu.assets.getAllDevices().findElement(by.tagName('a')).getText())
				.toMatch(/All Devices/);
		});

		it('should have the target URL \'/tdstm/assetEntity/list?filter=all\'', function() {
			expect(menu.assets.getAllDevices().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/list?filter=all');
		});

		it('should have the text \'Databases\' as its link text', function() {
			expect(menu.assets.getDatabases().findElement(by.tagName('a')).getText())
				.toMatch(/Databases/);
		});

		it('should have the target URL \'/tdstm/database/list\'', function() {
			expect(menu.assets.getDatabases().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/database/list');
		});

		it('should have the text \'Storage-Devices\' as its link text', function() {
			expect(menu.assets.getStorageDevices().findElement(by.tagName('a')).getText())
				.toMatch(/Storage-Devices/);
		});

		it('should have the target URL \'/tdstm/assetEntity/list?filter=storage\'', function() {
			expect(menu.assets.getStorageDevices().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/list?filter=storage');
		});

		it('should have the text \'Storage-Logical\' as its link text', function() {
			expect(menu.assets.getStorageLogical().findElement(by.tagName('a')).getText())
				.toMatch(/Storage-Logical/);
		});

		it('should have the target URL \'/tdstm/files/list\'', function() {
			expect(menu.assets.getStorageLogical().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/files/list');
		});

		it('should have the text \'Comments\' as its link text', function() {
			expect(menu.assets.getComments().findElement(by.tagName('a')).getText())
				.toMatch(/Comments/);
		});

		it('should have the target URL \'/tdstm/assetEntity/listComment\'', function() {
			expect(menu.assets.getComments().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/listComment');
		});

		it('should have the text \'Dependencies\' as its link text', function() {
			expect(menu.assets.getDependencies().findElement(by.tagName('a')).getText())
				.toMatch(/Dependencies/);
		});

		it('should have the target URL \'/tdstm/assetEntity/listDependencies\'', function() {
			expect(menu.assets.getDependencies().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/listDependencies');
		});

		it('should have the text \'Dependency Analyzer\' as its link text', function() {
			expect(menu.assets.getDependencyAnalyzer().findElement(by.tagName('a')).getText())
				.toMatch(/Dependency Analyzer/);
		});

		it('should have the target URL \'/tdstm/moveBundle/dependencyConsole\'', function() {
			expect(menu.assets.getDependencyAnalyzer().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveBundle/dependencyConsole');
		});

		it('should have the text \'Architecture Graph\' as its link text', function() {
			expect(menu.assets.getArchitectureGraph().findElement(by.tagName('a')).getText())
				.toMatch(/Architecture Graph/);
		});

		it('should have the target URL \'/tdstm/assetEntity/architectureViewer\'', function() {
			expect(menu.assets.getArchitectureGraph().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/architectureViewer');
		});

		it('should have the text \'Import Assets\' as its link text', function() {
			expect(menu.assets.getImportAssets().findElement(by.tagName('a')).getText())
				.toMatch(/Import Assets/);
		});

		it('should have the target URL \'/tdstm/assetEntity/assetImport\'', function() {
			expect(menu.assets.getImportAssets().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/assetImport');
		});

		it('should have the text \'Manage Batches\' as its link text', function() {
			expect(menu.assets.getManageBatches().findElement(by.tagName('a')).getText())
				.toMatch(/Manage Batches/);
		});

		it('should have the target URL \'/tdstm/dataTransferBatch/index\'', function() {
			expect(menu.assets.getManageBatches().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/dataTransferBatch/index');
		});

		it('should have the text \'Export Assets\' as its link text', function() {
			expect(menu.assets.getExportAssets().findElement(by.tagName('a')).getText())
				.toMatch(/Export Assets/);
		});

		it('should have the target URL \'/tdstm/assetEntity/exportAssets\'', function() {
			expect(menu.assets.getExportAssets().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/exportAssets');
		});

  }); // end Child Items

	it('should close the Assets Menu',function(){
		menu.assets.toggle();
		expect(menu.assets.isExpanded()).toBe(false);
	});

}); // end Assets Menu
