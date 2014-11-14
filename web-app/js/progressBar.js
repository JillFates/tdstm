function TaskProgressBar(taskId, pingTime, onSuccess, onFailure, progressTitle) {
	this.taskId = taskId;
	this.pingTime = pingTime;
	this.onSuccess = onSuccess;
	this.onFailure = onFailure;
	this.lastUpdated = 0;
	this.noProgressCount = 0; 	// Used to keep track of # of times called service and got same lastUpdated 
	if (progressTitle == undefined) {
		progressTitle = "";
	}
	this.progressTitle = progressTitle;
	var self = this;
	
	setTimeout(function() {
		self.initUI();
	}, 10);
	
	setTimeout(function() {
		self.updateProgress();
	}, self.pingTime);
}

TaskProgressBar.prototype.initUI = function() {
	var initialized = $('#progressBar').length != 0;
	var self = this;
	if (!initialized) {
		var newcontent = document.createElement('div');
	    newcontent.innerHTML = '<div class="modal fade" id="globalProgressBar" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">  <div class="modal-dialog">    <div class="modal-content">      <div class="modal-header">        <h4 id="progressTitle" class="modal-title" id="myModalLabel">Modal title</h4>      </div>      <div class="modal-body">	<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>        <div id="innerGlobalProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%; font-size: 11px; font-family: verdana;">0%</div>	<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>      </div>      <div class="modal-footer">	<button id="progressClose" type="button" class="btn btn-default" data-dismiss="modal" style="display:none;">Close</button>        <button id="progressCancel" type="button" class="btn btn-primary" style="display:none;">Cancel</button>      </div>    </div>  </div></div>';
	    document.body.appendChild(newcontent.firstChild);

        $('#progressTitle').html(self.progressTitle);
		var bar = $('#innerGlobalProgressBar');
		var status = $('#progressStatus');
		var value = 0;
		bar.attr('aria-valuenow', value);
		bar.css('width', value + '%');
		bar.html(value + '%');
		self.showProgressBar();
		status.html("Initializing...");
	}
}

TaskProgressBar.prototype.finishProgressBar = function() {
	setTimeout(function() {
		$('#globalProgressBar').modal('hide');
	}, 1000);

}

TaskProgressBar.prototype.showProgressBar = function() {
	$('#globalProgressBar').modal('show');
}

TaskProgressBar.prototype.updateProgress = function() {
	var self = this;
	
	var jqxhr = $.ajax( 
		{ 	type:'GET',
			cache: false,
			//dataType: 'json',
			// data: 
			//url: tdsCommon.createAppURL('/ws/progress/' + self.taskId + '?rand=' + tdsCommon.randomString(16)), 
			url: tdsCommon.createAppURL('/ws/progress/' + self.taskId), 
			success: function(data) {
				if (data) {
					data = data.data;

					if (data.lastUpdated == self.lastUpdated) {
						self.noProgressCount++;
						if (self.noProgressCount > 2) {
							if (! confirm('It appears that the process has stopped work. Press Okay to continue waiting otherwise press Cancel.')) {
								self.onFailure();						
								self.finishProgressBar();
								return;
							}
							self.noProgressCount = 0;
						}
					} else {
						self.lastUpdated = data.lastUpdated;
						self.noProgressCount = 1;
					}

			        $('#progressTitle').html(self.progressTitle);

					var bar = $('#innerGlobalProgressBar');
					var status = $('#progressStatus');
					var closeButton = $('#progressClose');

					if (data.status == "In progress") {						
						var value = data.percentComp;
						bar.show();
						bar.attr('aria-valuenow', value);
						bar.css('width', value + '%');
						bar.html(value + '%');
						self.showProgressBar();
						status.html(data.status);
											
						setTimeout(function() {
							self.updateProgress();
						}, self.pingTime);

					} else if (data.status == "Completed")  {
						var value = 100;
						bar.show();
						bar.attr('aria-valuenow', value);
						bar.css('width', value + '%');
						bar.html(value + '%');
						status.html(data.status);

						if (self.onSuccess != undefined) {
							self.onSuccess();
						}
						self.finishProgressBar();
					} else {
						status.html(data.status + (data.detail ? ": " + data.detail : ""));
						bar.hide();						
						closeButton.show();
						closeButton.click(function() {
							if (self.onFailure != undefined) {
								self.onFailure();
							}
							closeButton.hide();
							self.finishProgressBar();
						});

					}
				}
			},
			error: function() {
				if (self.onFailure != undefined) {
					self.onFailure();
				}
				self.finishProgressBar();
			}
		});
}
