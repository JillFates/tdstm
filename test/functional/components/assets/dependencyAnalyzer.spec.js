'use strict';
var Menu = require('../menu/menu.po.js');
var DependencyAnalizer = require('./dependencyAnalyzer.po.js');

describe('Dependency Analyzer page', function(){

  it('should go to Dependency AnalyzerPage after select Assets - Dependency Analyzer',function(){
    var menu = new Menu();
    menu.goToAssets('dependencyAnalizer');
    expect(menu.getCurrentUrl('/tdstm/moveBundle/dependencyConsole')).toEqual(process.env.BASE_URL+'/tdstm/moveBundle/dependencyConsole');
  });

  var depAnalizerPage = new DependencyAnalizer();
  
  it('should have "Dependency Analyzer" Title', function(){
    expect(depAnalizerPage.titleh.getText()).toEqual('Dependency Analyzer');
  });

  describe('Compact Control', function() {

    xit('should have Compact Control label',function () {
      
    });

    xit('should have Compact Control checkbox disabled as default', function() {

      expect(depAnalizerPage.controlCompactCheckbx.getAttribute('checked')).toBe(null);

    });

    xit('should have Control Compacted if you enable Compact Control checkbox', function() {
      
      depAnalizerPage.controlCompactCheckbx.click();
      expect(depAnalizerPage.controlCompactCheckbx.getAttribute('checked')).toBe('true');
      depAnalizerPage.compactClassElements.then(function (list) {
        list.forEach(function (element) {
          expect(element.getAttribute('style')).toEqual('display: none;');
        });
      });

    });

    xit('should have Control Expanded if you disable Compact control checkbox', function() {
      depAnalizerPage.controlCompactCheckbx.click();
      expect(depAnalizerPage.controlCompactCheckbx.getAttribute('checked')).toBe(null);
      depAnalizerPage.compactClassElements.then(function (list) {
        list.forEach(function (element) {
          expect(element.getAttribute('style')).not.toContain('none');
        });
      });
    });

  }); //Compact Control

  describe('Regenerate button', function() {
    
    it('should have "Regenerate..." as label', function() {
    
      expect(depAnalizerPage.regenerateBtn.getAttribute('value')).toEqual('Regenerate...');
    
    });

    it('should display "Dependency Grouping Control" modal after the button is clicked',function () {
      depAnalizerPage.regenerateBtn.click();
      expect(depAnalizerPage.depGroupControlModal.getAttribute('style')).toContain('display: block;');
    });

      describe('Dependency Grouping Control modal', function() {
        
        it('should have "Dependency Grouping Control" as title', function() {
          
          expect(depAnalizerPage.modalTitle.getText()).toEqual('Dependency Grouping Control');
        
        });

        xit('should have Connection Type section', function() {
          
        });

        xit('should have Connection Status section', function() {
          
        });

        xit('should have "Save as defaults" checkbox', function() {
          
        });

        it('should have Generate as generate button label', function() {

          expect(depAnalizerPage.generateBtn.getAttribute('value')).toEqual('Generate');
          
        });

        describe('Progress Bar Modal', function() {
          
          it('should display progress modal after click on generate button', function() {

            depAnalizerPage.generateBtn.click();
            expect(depAnalizerPage.isProgressBarOpened()).toEqual(true);
            // expect(depAnalizerPage.progressBarModal.getAttribute('style')).toContain('display: block;')
            
          });

          it('should have "Generating Dependency Groups" as title', function() {
            
            expect(depAnalizerPage.progressBarTitle.getText()).toEqual('Generating Dependency Groups');
          
          });

          it('should displayed "Finished" as message when group generation ends', function () {
            
            expect(depAnalizerPage.isGenerationEnded()).toEqual(true);

          });

          it('should display Close Button', function() {
            expect(depAnalizerPage.isCloseButtonDisplayed()).toEqual(true);
          });

          it('should have Close as value', function () {
            
            expect(depAnalizerPage.progressBarCloseBtn.getText()).toEqual('Close');
          
          });

          it('should close Progress Bar modal after click on close btn',function () {
            
            depAnalizerPage.progressBarCloseBtn.click();
            // expect(depAnalizerPage.progressBarModal.getAttribute('style')).toContain('display: none;');
            expect(depAnalizerPage.isProgressBarClosed()).toEqual(true);

          });

        }); // Progress Bar Modal



      });
  });
});//Dependency Analyzer page