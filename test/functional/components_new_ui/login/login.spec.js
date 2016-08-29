'use strict';
var Login = require('./login.po.js');

describe('Login Test', function() {

  var loginPage = new Login();

	it('should display a message when attempting to access a different page when not logged in', function() {
		// have the user sign out directly through the URL
		loginPage.forceSignOut();
		loginPage.getDashboard();
		expect(loginPage.getMessage()).toBe('Your login session has expired. Please login again.');
	});

	it('should have a link for forgotten passwords with the text "Forgot your password?"', function() {
		expect(loginPage.getForgotPasswordText()).toBe('Forgot your password?');
	});

	it('should be able to retrieve the build info', function() {
		expect(loginPage.getBuildInfo()).toMatch(/Version \d+(?:\.\d+)*/);
	});

  describe('"Invalid username and/or password" Validation Message Test', function() {

		beforeEach(function() {
			loginPage.clearUsername();
			loginPage.clearPassword();
		});

    it('should display a message when attempting to login without setting a username/password', function() {
			loginPage.clickSignInBtn();
      expect(loginPage.getMessage()).toBe('Invalid username and/or password');
    });

    it('should display a message when attempting to login with only a username', function() {
      loginPage.setUsername(process.env.USER_NAME);
      loginPage.clickSignInBtn();
      expect(loginPage.getMessage()).toBe('Invalid username and/or password');
    });

    it('should display a message when attempting to login with only a password', function() {
	    loginPage.setPassword(process.env.PASSWORD);
      loginPage.clickSignInBtn();
      expect(loginPage.getMessage()).toBe('Invalid username and/or password');
    });

    it('should display a message when attempting to login with an invalid username/password', function() {
      loginPage.setUsername('invalidUsername');
      loginPage.setPassword('invalidPassword');
      loginPage.clickSignInBtn();
      expect(loginPage.getMessage()).toBe('Invalid username and/or password');
    });

  }); // end "Invalid username and/or password" Validation Message Test

  it('should successfully log back in with a valid username/password', function() {
		loginPage.clearUsername();
		loginPage.clearPassword();
    loginPage.setUsername(process.env.USER_NAME);
    loginPage.setPassword(process.env.PASSWORD);
    loginPage.clickSignInBtn();
    expect(browser.driver.getTitle()).not.toBe('Login');
  });

}); // end Login Test
