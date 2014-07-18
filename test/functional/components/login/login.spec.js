'use strict';
var Login = require('./login.po.js');
describe('Login Test', function(){
  var loginPage = new Login();

  it('should have "Username:" as label for username', function(){
    loginPage.getUserNameLabel().then(function(label){
      expect(label.getText()).toEqual('Username:');
    });
  });

  it('should have "Password:" as label for password', function(){
    loginPage.getPasswordLabel().then(function(label){
      expect(label.getText()).toEqual('Password:');
    });
  });  

  describe('"Invalid userName and/or password" validation message', function(){

    beforeEach(function(){
      loginPage.get();
      // browser.navigate().refresh();
    });

    it('should be displayed after clicking on login button without setting user/pass', function(){
      loginPage.clickSingInBtn();
      expect(loginPage.getValidationMessage().getText()).toEqual('Invalid username and/or password');  
    });

    it('should be displayed when click on login button after only adding the username', function(){
      loginPage.setUsername('jcalabrese');
      loginPage.clickSingInBtn();
      expect(loginPage.getValidationMessage().getText()).toEqual('Invalid username and/or password');  
    });

    it('should be displayed when click on login button after only adding the password', function(){
      loginPage.setPassword('hola');
      loginPage.clickSingInBtn();
      expect(loginPage.getValidationMessage().getText()).toEqual('Invalid username and/or password');  
    });

    it('should be displayed when try to login with invalid userName/password', function(){
      loginPage.setUsername('jcalabrese');
      loginPage.setPassword('invalid');
      loginPage.clickSingInBtn();
      expect(loginPage.getValidationMessage().getText()).toEqual('Invalid username and/or password');  
    });

  }); //"Invalid userName and/or password" validation message
  
  it('should login to tdstm with valid userName/Password', function(){
    loginPage.clearUsername();
    loginPage.setUsername(process.env.USERNAME);
    loginPage.setPassword(process.env.PASSWORD);
    loginPage.clickSingInBtn();
    expect(loginPage.getAfterLoginTitle().getText()).toMatch(/TransitionManager™.*/);
    });

});