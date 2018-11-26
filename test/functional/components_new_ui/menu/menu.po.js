'use strict';
var Menu = function() {

  this.getCurrentUrl = function(expUrl) {

    if (expUrl) {
			return browser.driver.wait(function() {
      	return browser.driver.getCurrentUrl().then(function(url) {
        	return url.indexOf(expUrl) > -1;
      	});
    	}).then(function() {
        return browser.driver.getCurrentUrl();
      });
    }
		else
      return browser.driver.getCurrentUrl();
  };

  this.waitForURL = function(expUrl) {
    return browser.driver.wait(function() {
      return browser.driver.getCurrentUrl().then(function(url) {
        return process.env.BASE_URL+expUrl === url;
      });
    }).then(function() {
      return true;
    });
  };

	// constructor for menu-parent objects
	this.parentObj = function(parentClass) {
		this.loc = by.className(parentClass); // assign ID

		this.getParent = function() {
			return browser.driver.findElement(this.loc);
		};
		this.toggle = function() {
			this.getParent().click();
		};
		this.isExpanded = function() {
			var that = this;
			return browser.driver.wait(function() {
				return that.getParent().findElement(by.className('menu-item-expand')).isDisplayed();
			}).then(function() {
				return true;
			}, function() {
				return false;
			});
		};
		this.getItem = function(itemClass) {
			return this.getParent().findElement(by.className(itemClass)); // assign IDs when available
		};
	};

	var that = this;

	this.admin = new function() {
		that.parentObj.call(this, 'menu-parent-admin');
		this.getAdminPortal = function() {return this.getItem('menu-admin-portal');};
		this.getRolePermissions = function() {return this.getItem('menu-admin-role');};
		this.getAssetOptions = function() {return this.getItem('menu-admin-asset-options');};
		this.getListCompanies = function() {return this.getItem('menu-list-companies');};
		this.getListStaff = function() {return this.getItem('menu-list-staff');};
		this.getListUsers = function() {return this.getItem('menu-list-users');};
		this.getImportAccounts = function() {return this.getItem('menu-client-import-accounts');};
		this.getExportAccounts = function() {return this.getItem('menu-client-export-accounts');};
		this.getListWorkflows = function() {return this.getItem('menu-list-workflows');};
		this.getListManufacturers = function() {return this.getItem('menu-list-manufacturers');};
		this.getListModels = function() {return this.getItem('menu-list-models');};
		this.getSyncLibraries = function() {return this.getItem('menu-sync-libraries');};
	};

	this.projects = new function() {
		that.parentObj.call(this, 'menu-parent-projects');
		this.getActiveProjects = function() {return this.getItem('menu-projects-active-projects');};
		this.getProjectDetails = function() {return this.getItem('menu-projects-current-project');};
		this.getProjectStaff = function() {return this.getItem('menu-projects-project-staff');};
		this.getUserActivationEmails = function() {return this.getItem('menu-projects-user-activation');};
		this.getFieldSettings = function() {return this.getItem('menu-projects-field-settings');};
	};

	this.dataCenters = new function() {
		that.parentObj.call(this, 'menu-parent-data-centers');
		this.getListRooms = function() {return this.getItem('menu-parent-data-centers-list-rooms');};
		this.getRackElevations = function() {return this.getItem('menu-parent-data-centers-rack-elevation');};
	};

	this.assets = new function() {
		that.parentObj.call(this, 'menu-parent-assets');
		this.getSummaryTable = function() {return this.getItem('menu-parent-assets-summary-table');};
		this.getApplications = function() {return this.getItem('menu-parent-assets-application-list');};
		this.getServers = function() {return this.getItem('menu-parent-assets-server-list');};
		this.getAllDevices = function() {return this.getItem('menu-parent-assets-all-list');};
		this.getDatabases = function() {return this.getItem('menu-parent-assets-database-list');};
		this.getStorageDevices = function() {return this.getItem('menu-parent-assets-storage-list');};
		this.getStorageLogical = function() {return this.getItem('menu-parent-assets-storage-logical-list');};
		this.getComments = function() {return this.getItem('menu-parent-assets-comments-list');};
		this.getDependencies = function() {return this.getItem('menu-parent-assets-dependencies-list');};
		this.getDependencyAnalyzer = function() {return this.getItem('menu-parent-assets-dependency-analyzer');};
		this.getArchitectureGraph = function() {return this.getItem('menu-parent-assets-architecture-graph');};
		this.getImportAssets = function() {return this.getItem('menu-parent-assets-import-assets');};
		this.getManageBatches = function() {return this.getItem('menu-parent-assets-manage-batches');};
		this.getExportAssets = function() {return this.getItem('menu-parent-assets-export-assets');};
	};

	this.planning = new function() {
		that.parentObj.call(this, 'menu-parent-planning');
		this.getListEvents = function() {return this.getItem('menu-parent-planning-event-list');};
		this.getEventDetails = function() {return this.getItem('menu-parent-planning-event-detail-list');};
		this.getListEventNews = function() {return this.getItem('menu-parent-planning-event-news');};
		this.getPreEventChecklist = function() {return this.getParent().findElement(by.partialLinkText('Pre-event Checklist')).findElement(by.xpath('..'));}; // assign class?
		this.getExportRunbook = function() {return this.getItem('menu-parent-planning-export-runbook');};
		this.getListBundles = function() {return this.getItem('menu-parent-planning-list-bundles');};
		this.getBundleDetails = function() {return this.getItem('menu-parent-planning-selected-bundle');};
	};

	this.tasks = new function() {
		that.parentObj.call(this, 'menu-parent-tasks');
		this.getMyTasks = function() {return this.getItem('menu-parent-tasks-my-tasks');};
		this.getTaskManager = function() {return this.getItem('menu-parent-tasks-task-manager');};
		this.getTaskGraph = function() {return this.getItem('menu-parent-tasks-task-graph');};
		this.getTaskTimeline = function() {return this.getItem('menu-parent-tasks-task-timeline');};
		this.getCookbook = function() {return this.getItem('menu-parent-tasks-cookbook');};
		this.getGenerationHistory = function() {return this.getItem('menu-parent-tasks-generation-history');};
	};

	this.dashboards = new function() {
		that.parentObj.call(this, 'menu-parent-dashboard');
		this.getUserDashboard = function() {return this.getItem('menu-parent-dashboard-user-dashboard active');};
		this.getPlanningDashboard = function() {return this.getItem('menu-parent-dashboard-planning-dashboard');};
		this.getEventDashboard = function() {return this.getItem('menu-parent-dashboard-event-dashboard');};
	};

	this.reports = new function() {
		that.parentObj.call(this, 'menu-parent-reports');
		this.getApplicationProfiles = function() {return this.getItem('menu-reports-application-profiles');};
		this.getApplicationConflicts = function() {return this.getItem('menu-reports-application-conflicts');};
		this.getServerConflicts = function() {return this.getItem('menu-reports-server-conflicts');};
		this.getDatabaseConflicts = function() {return this.getItem('menu-reports-database-conflicts');};
		this.getTaskReport = function() {return this.getItem('menu-reports-task-report');};
		this.getReportSummary = function() {return this.getItem('menu-reports-report-summary');};
		this.getActivityMetrics = function() {return this.getItem('menu-reports-activity-metrics');};
		this.getPreeventChecklist = function() {return this.getItem('menu-reports-pre-checklist');};
		this.getLoginBadges = function() {return this.getItem('menu-reports-login-badges');};
		this.getApplicationMigrationResults = function() {return this.getItem('menu-reports-application-migration');};
	};

};

module.exports = Menu;
