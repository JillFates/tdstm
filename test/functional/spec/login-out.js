'use strict';
var MenuHelper = require('../support/helpers/menu-helper.js').MenuHelper;
// var baseUrl =  process.env.BASE_URL;
describe('test - login/logout', function(){
  var menu = new MenuHelper();

  describe('signOut - Menu', function(){

    it('signOut', function(){
      //since onPrepare function the user is logging I testing here the logout
      //open menu
      menu.selectopt('user','signOut');
      var loginPageh1 = browser.driver.findElement(by.css('h1'));
      expect(loginPageh1.getText()).toEqual('TransitionManager™');
    });
  }); // logout

  describe('login Page', function(){

    var signInbtn,userNameInput,passwordInput;
    beforeEach(function(){
      // browser.driver.get(baseUrl+'/tdstm/auth/login');
      browser.navigate().refresh();
      userNameInput = browser.driver.findElement(by.id('usernameId'));
      passwordInput = browser.driver.findElement(by.name('password'));
      signInbtn =  browser.driver.findElement(by.css(menu.signInbtnCss));
    });
    it('validate username field label', function(){
      var username = browser.driver.findElement(by.css('td tr td'));
      expect(username.getText()).toEqual('Username:');
    });

    it('validate password field label', function(){
      browser.driver.findElements(by.css('td tr td')).then(function(pass){
        expect(pass[2].getText()).toEqual('Password:');
      });
    });

    it('click on login button without setting the username and password', function(){
      signInbtn.click();
      var message = browser.driver.findElement(by.css(menu.errorMessageCss));
      expect(message.getText()).toEqual('Invalid username and/or password');
    });

    it('click on login button after only adding the username', function(){
      userNameInput.sendKeys('jcalabrese');
      signInbtn.click();
      var message = browser.driver.findElement(by.css(menu.errorMessageCss));
      expect(message.getText()).toEqual('Invalid username and/or password');
    });

    it('click on login button after only adding the password', function(){
      userNameInput.clear();
      passwordInput.sendKeys('hola');
      signInbtn.click();
      var message = browser.driver.findElement(by.css(menu.errorMessageCss));
      expect(message.getText()).toEqual('Invalid username and/or password');

    });

    it('login with invalid userName', function(){
      userNameInput.sendKeys('invalid');
      passwordInput.clear();
      signInbtn.click();
      var message = browser.driver.findElement(by.css(menu.errorMessageCss));
      expect(message.getText()).toEqual('Invalid username and/or password');      

    });

    it('should show error with login using valid userName and invalid password', function(){
      userNameInput.sendKeys('jcalabrese');
      passwordInput.sendKeys('invalid');
      signInbtn.click();
      var message = browser.driver.findElement(by.css(menu.errorMessageCss));
      expect(message.getText()).toEqual('Invalid username and/or password');      
    });

    it('login to tdstm', function(){
      userNameInput.sendKeys(process.env.USERNAME);
      passwordInput.sendKeys(process.env.PASSWORD);
      signInbtn.click();
      // expect(menu.getTitle().getText()).toEqual(' TransitionManager™ - testJ');
      expect(menu.getTitle().getText()).toMatch(/TransitionManager™ - .*/);
    });
    
  }); // loginPage
  
});