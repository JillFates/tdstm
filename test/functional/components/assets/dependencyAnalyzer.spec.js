'use strict';
var Menu = require('../menu/menu.po.js');
var DependencyAnalizer = require('./dependencyAnalyzer.po.js');
describe('Dependency Analyzer page', function(){
  it('should go to Dependency AnalyzerPage after select Assets - Dependency Analyzer',function(){
    var menu = new Menu();
    menu.goToAssets('dependencyAnalizer');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveBundle/dependencyConsole');
  });

  it('should have "Dependency Analyzer" Title', function(){
    var depAnalizerPage = new DependencyAnalizer();
    expect(depAnalizerPage.titleh.getText()).toEqual('Dependency Analyzer');
  });
});//Dependency Analyzer page