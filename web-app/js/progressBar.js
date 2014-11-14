/**
  * Creates a progress bar popup to show task batch progress
  */
tds.ui.progressBar = function(taskId, pingTime, onSuccess, onFailure, progressTitleValue, uiContainerId) {

	var containerTemplate = 
	'<div class="modal fade" id="globalProgressBar" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">' +
		'<div class="modal-dialog">' +
			'<div class="modal-content">' +
				'<div class="modal-header">' +
					'<h4 id="progressTitle" class="modal-title" id="myModalLabel">Modal title</h4>' +
				'</div>' +
				'<div class="modal-body">' +
					'<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>' +
					'<div id="innerGlobalProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%; font-size: 11px; font-family: verdana;">0%</div>' +
					'<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>' +
				'</div>' +
				'<div class="modal-footer">' +
					'<button id="progressClose" type="button" class="btn btn-default" data-dismiss="modal" style="display:none;">Close</button>' +
					'<button id="progressCancel" type="button" class="btn btn-primary" style="display:none;">Cancel</button>' +
				'</div>' +
			'</div>' + 
		'</div>' +
	'</div>';

	var lastUpdated = 0;
	var noProgressCount = 0; 	// Used to keep track of # of times called service and got same lastUpdated 

	if (progressTitleValue == undefined) {
		progressTitleValue = "";
	}
	var progressTitle = progressTitleValue;

	if (!uiContainerId) {
		uiContainerId = "progressBar"
	}
	var refUiContainerId = '#' + uiContainerId;

	var destroy = function() {
	};

	var initContainer = function() {
		var initialized = $(refUiContainerId).length != 0;
		if (!initialized) {
			$(document.body).prepend('<div id="' + uiContainerId + '"></div>');
		}
	}

	var initUI = function() {
		initContainer();
		var container = $(refUiContainerId);
		container.html(containerTemplate)

		$('#progressTitle').html(progressTitle);
		var bar = $('#innerGlobalProgressBar');
		var status = $('#progressStatus');
		var value = 0;
		bar.attr('aria-valuenow', value);
		bar.css('width', value + '%');
		bar.html(value + '%');
		showProgressBar();
		status.html("Initializing...");
	}

	var finishProgressBar = function(onFinishCallback) {
		setTimeout(function() {
			$('#globalProgressBar').modal('hide');
			$(refUiContainerId).html('');
			if (onFinishCallback) {
				onFinishCallback();
			}
		}, 1000);

	}

	var showProgressBar = function() {
		$('#globalProgressBar').modal('show');
	}

	var updateProgress = function() {
		var jqxhr = $.ajax( 
			{ 	type:'GET',
				cache: false,
				//dataType: 'json',
				// data: 
				//url: tdsCommon.createAppURL('/ws/progress/' + taskId + '?rand=' + tdsCommon.randomString(16)), 
				url: tdsCommon.createAppURL('/ws/progress/' + taskId), 
				success: function(data) {
					if (data) {
						data = data.data;

						if (data.lastUpdated == lastUpdated) {
							noProgressCount++;
							if (noProgressCount > 2) {
								if (! confirm('It appears that the process has stopped work. Press Okay to continue waiting otherwise press Cancel.')) {
									finishProgressBar(onFailure);
									return;
								}
								noProgressCount = 0;
							}
						} else {
							lastUpdated = data.lastUpdated;
							noProgressCount = 1;
						}

				        $('#progressTitle').html(progressTitle);

						var bar = $('#innerGlobalProgressBar');
						var status = $('#progressStatus');
						var closeButton = $('#progressClose');

						if (data.status == "In progress") {						
							var value = data.percentComp;
							bar.show();
							bar.attr('aria-valuenow', value);
							bar.css('width', value + '%');
							bar.html(value + '%');
							showProgressBar();
							status.html(data.status);
												
							setTimeout(function() {
								updateProgress();
							}, pingTime);

						} else if (data.status == "Completed")  {
							var value = 100;
							bar.show();
							bar.attr('aria-valuenow', value);
							bar.css('width', value + '%');
							bar.html(value + '%');
							status.html(data.status);

							finishProgressBar(onSuccess);
						} else {
							status.html(data.status + (data.detail ? ": " + data.detail : ""));
							bar.hide();						
							closeButton.show();
							closeButton.click(function() {
								closeButton.hide();
								finishProgressBar(onFailure);
							});

						}
					}
				},
				error: function() {
					finishProgressBar(onFailure);
				}
			});
	}
	
	setTimeout(function() {
		initUI();
	}, 10);
	
	setTimeout(function() {
		updateProgress();
	}, pingTime);

	return {
		destroy: destroy
	};
};
