var TimerBar = function (defaultValue, preferenceName, refreshCallback) {
	
	// public functions
	var public = {};
	
	// called on the initial definition of a TimerBar object to set up variables
	public.init = function () {
		public.oop = new zxcAnimate('width', document.getElementById('issueTimebarId'), 0);
		public.max = $('#issueTimebar').width();
		public.to = null;
		public.timerValue = null;
		public.modeModal = null;
		
		if (refreshCallback)
			public.refreshFunction = refreshCallback;
		else
			public.refreshFunction = function () {
				if(public.modeModal && public.modeModal === 'viewMode'){
					public.modeModal = null; // restart the status
				} else {
				window.location.reload();
				}
			};
			
		public.bindListeners();
		public.getUserPreference();
	}
	
	// returns true the timerbar is set to manual refresh mode
	public.isManual = function () {
		return (public.timerValue == 0)
	}
	
	// starts the timer and animation at the value specified by @param sec
	public.Start = function (sec) {
		clearTimeout(public.to);
		public.oop.animate(0,$('#issueTimebar').width(),sec*1000);
		public.srt = new Date();
		public.sec = sec;
		public.Time();
	}
	
	// handles each tick of the timer
	public.Time = function (sec) {
		var oop = public;
		var sec2 = public.sec-Math.floor((new Date()-public.srt)/1000);
		$('#timeBarValueId').val(sec2)
		if (sec2 > 0) {
			public.to = setTimeout(function(){ oop.Time(); },1000);
		} else if (isNaN(sec2)) {
			var timePref = $("#selectTimedBarId").val()
			if (timePref != 0) {
				public.Start(timePref);
			} else {
				public.Pause(0);
			}
		} else {
			public.refreshFunction();
		}
	}
	
	// pauses the timer at the specified value
	public.Pause = function (sec) {
		clearTimeout(public.to);
		if (sec == 0) {
			public.oop.animate(sec,'',sec*1000);
		} else {
			public.oop.animate($('#issueTimebarId').width(),$('#issueTimebarId').width(),sec*1000);
		}
	}
	
	// resumes the timer
	public.Resume = function () {
		clearTimeout(public.to);
		if (public.sec != 0) {
			var remainingTime = $('#timeBarValueId').val();
			var currentTime = public.sec - remainingTime;
			public.srt = new Date() - currentTime * 1000;
			public.oop.animate($('#issueTimebarId').width(), $('#issueTimebar').width(), remainingTime*1000);
			if (currentTime > 0 && currentTime < public.sec + 1)
				public.Time();
			else
				public.Restart();
		}
	}
	
	// resumes the timer if no modals are open
	public.attemptResume = function () {
		if ( ! public.areActionBarsOpen() )
			public.Resume();
	}
	
	// restarts the timer
	public.Restart = function (sec) {
		clearTimeout(public.to);
		var second = public.timerValue;
		$('#timeBarValueId').val(second);
		public.oop.animate(0, $('#issueTimebar').width(),second*1000);
		public.srt = new Date();
		public.sec = second;
		public.Time();
	}
	
	// resets the value on the timerbar
	public.resetTimer = function () {
		if (! public.isManual())
			public.Restart(public.timerValue);
	}
	
	// binds event listeners to DOM elements that affect the timerbar
	public.bindListeners = function () {
		$('#selectTimedBarId').on('change', function (e) {
			var element = e.srcElement;
			if (!element)
				element = e.target;
			public.setTimerValue(element.value);
			public.setUserPreference(element.value);
		});
		
		$(window).resize(function() {
			public.resetTimer();
		});
		
		$('div[role=dialog]').on('dialogclose', function () {
			public.modeModal = null; // restart the status
			var viewElementModal = $(this).find('.viewMode');
			if(viewElementModal && viewElementModal.length > 0) {
				public.modeModal = 'viewMode';
			}
			public.attemptResume();
		});
	}
	
	// reinitializes the timerbar using the response data from getUserPreference
	public.setUpTimerBar = function (response) {
		var responseObject = $.parseJSON(response.responseText);
		var time = parseInt(responseObject.data.preferences[preferenceName]);
		time = isNaN(time) ? defaultValue : time;
		$("#selectTimedBarId").val(time);
		
		public.setTimerValue(time);
	}
	
	// sets the value on the timer to the specified value
	public.setTimerValue = function (newTime) {
		var timebar = $('#issueTimebar');
		public.timerValue = newTime;
		
		if (newTime != 0) {
			public.Start(newTime);
			timebar.removeClass('hide');
		} else {
			public.Pause(0);
			timebar.addClass('hide');
		}
	}
	
	// gets the user's timer preference for this page
	public.getUserPreference = function () {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preferences/' + preferenceName),
			data: {},
			type:'GET',
			complete: public.setUpTimerBar
		});
	}
	
	// sets the user's timer preference for this page to the specified value
	public.setUserPreference = function (newValue) {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preference'),
			data: {'code':preferenceName,'value':newValue},
			type:'POST'
		});
	}
	
	public.areActionBarsOpen = function () {
		var taskActionBars = $('#actionBarId').size();
		if (taskActionBars > 0)
			return true;
		var taskRows = $('tr.taskDetailsRow');
		for (var i = 0; i < taskRows.size(); i++)
			if ($(taskRows[i]).css('display') != 'none')
				return true;
		return false;
	}
	
	/*	This ugly zxcAnimate code should be replaced when we implement a new animation library.
		For now it's a black box that handles the animation of the timer bar. */
	function zxcAnimate(mde,obj,srt) {
		this.to = null;
		this.obj = typeof(obj) == 'object' ? obj : document.getElementById(obj);
		this.mde = mde.replace(/\W/g,'');
		this.data = [srt || 0];
		return this;
	}

	zxcAnimate.prototype.animate=function(srt,fin,ms,scale,c) {
		clearTimeout(this.to);
		this.time = ms || this.time || 0;
		this.neg = srt < 0 || fin < 0;
		this.data = [srt,srt,fin];
		this.mS = this.time*( !scale ? 1 : Math.abs((fin-srt) / (scale[1]-scale[0])) );
		this.c = typeof(c)=='string'?c.charAt(0).toLowerCase():this.c?this.c:'';
		this.inc = Math.PI/(2*this.mS);
		this.srttime = new Date().getTime();
		this.cng();
	}

	zxcAnimate.prototype.cng=function() {
		var oop = this
		var ms = new Date().getTime() - this.srttime;
		this.data[0] = (this.c=='s') ? (this.data[2]-this.data[1])*Math.sin(this.inc*ms)+this.data[1] : (this.c=='c')?this.data[2]-(this.data[2]-this.data[1])*Math.cos(this.inc*ms):(this.data[2]-this.data[1])/this.mS*ms+this.data[1];
		this.apply();
		if (ms < this.mS)
			this.to = setTimeout(function(){oop.cng()},10);
		else {
			this.data[0] = this.data[2];
			this.apply();
			if (this.Complete)
				this.Complete(this);
		}
	}

	zxcAnimate.prototype.apply=function() {
		if (isFinite(this.data[0])) {
			if (this.data[0]<0&&!this.neg)
				this.data[0] = 0;
			if (this.mde!='opacity')
				this.obj.style[this.mde] = Math.floor(this.data[0])+'px';
			else
				zxcOpacity(this.obj,this.data[0]);
		}
	}

	function zxcOpacity(obj,opc) {
		if (opc < 0 || opc > 100)
			return;
		obj.style.filter = 'alpha(opacity='+opc+')';
		obj.style.opacity = obj.style.MozOpacity = obj.style.WebkitOpacity = obj.style.KhtmlOpacity = opc / 100 - .001;
	}
	
	// call the initialization function
	public.init();
	
	// return the public object to make the public functions accessable
	return public;
	
}
