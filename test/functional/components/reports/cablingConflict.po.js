'use strict';
var CablingConflict = function(){
	this.getPageTitle = function(){
		return browser.driver.findElement(by.css('h1')).getText();
	};
  this.generateBtn = browser.driver.findElement(by.css('[onclick="return submit_CablingQAReport(this)"]'));
};
module.exports = CablingConflict;