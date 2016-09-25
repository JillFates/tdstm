/**
 * Created by Jorge Morayta on 11/23/2015.
 */

describe("TaskManagerController", function () {

    var taskManager, // Controller itself
        taskManagerService,
        restService;

    // Load the general module first
    beforeEach(angular.mock.module('TDSTM'));

    // Inject each one of the dependencies
    beforeEach(inject(function (_$controller_, _taskManagerService_) {
        taskManager = _$controller_('TaskManagerController');
        taskManagerService = _taskManagerService_;
    }));

    // Test internal var properties
    it("Controller Name Must be 'Task Manager'", function () {
        expect(taskManager.module).toBe('TaskManager');
    });

});