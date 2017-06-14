var ProgressTimer = function (defaultValue, preferenceName, refreshCallback) {
	
	// public functions
	var public = {};
	
	// called on the initial definition of a TimerBar object to set up variables
	public.init = function () {
		public.container = '.progress-bar-svg';
		public.timerValue = null;
		public.pausedValue = 0;
        public.updateTarget = true;
		public.bindListeners();
		public.getUserPreference();

		if (refreshCallback)
			public.refreshFunction = refreshCallback;
		else
			public.refreshFunction = function () {
				window.location.reload();
			};
	}

	public.isManual = function () {
		return (public.timerValue == 0)
	};

	public.setUpTimerBar = function (response) {
		var responseObject = $.parseJSON(response.responseText);
		var time = parseInt(responseObject.data.preferences[preferenceName]);
		time = isNaN(time) ? defaultValue : time;
		$("#selectTimedBarId").val(time);

		public.setTimerValue(time);
	};

	// pauses the timer at the specified value
	public.Pause = function () {
		if(!public.isManual()) {
			public.pausedValue = public.progressTimer.value();
			public.progressTimer.stop();
		}
	}

	public.getUserPreference = function () {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preferences/' + preferenceName),
			data: {},
			type:'GET',
			complete: public.setUpTimerBar
		});
	};

	public.showState = function(state, bar, attachment) {
		// When is completed 1 = 100%
		if(bar.value() == 1){
            public.refreshFunction(public.updateTarget);
            public.updateTarget = true;
		}
	}

	public.Start = function (sec) {

		if(public.progressTimer) {
			public.progressTimer.destroy();
		}

		public.progressTimer = new ProgressBar.Circle(public.container, {
			strokeWidth: 50,
			duration: sec * 1000,
			color: '#3c8dbc',
			trailColor: '#eee',
			trailWidth: 0,
			svgStyle: null,
			step: public.showState
		});

		public.progressTimer.animate(1.0)
	};

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

	// resumes the timer if no modals are open
	public.attemptResume = function () {
		if ( ! public.areActionBarsOpen() )
			public.Resume();
	}

	public.Resume = function() {
		public.Restart();
	};

	public.Restart = function () {
		// if a temp values was assigned, then restart the graph and clean the pasued value
		if(public.pausedValue != 0) {
			public.progressTimer.set(public.pausedValue);
			public.progressTimer.animate(1.0)
			public.pausedValue = 0;
		} else {
			var second = public.timerValue;
			$('#timeBarValueId').val(second);
			public.Start(second);
		}
	}

	public.bindListeners = function () {
		$('#selectTimedBarId').on('change', function (e) {
			var element = e.srcElement;
			if (!element)
				element = e.target;
			public.setTimerValue(element.value);
			public.setUserPreference(element.value);
		});

		$('div[role=dialog]').on('dialogclose', function () {
			public.attemptResume();
		});
	}

	public.setUserPreference = function (newValue) {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preference'),
			data: {'code':preferenceName,'value':newValue},
			type:'POST'
		});
	}

	public.setTimerValue = function (newTime) {

		public.timerValue = newTime;

		if (newTime != 0) {
			public.Start(newTime);
			$(public.container).parent().show();
		} else {
			if(public.progressTimer){
				public.progressTimer.stop();
			}
			$(public.container).parent().hide();
		}
	}

	public.resetTimer = function () {
		if (! public.isManual())
			public.Restart();
	}

	public.init();

	return public;
	
};