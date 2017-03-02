<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>Import Tasks</title>
</head>
<body>
<tds:subHeader title="Import Tasks " crumbs="['Task','Step1', 'Upload']"/>
	<script>
		$(document).ready(function() {
			$("#uploadBtn").on('change', function() {
				var fileName  = $(this).val().split(/(\\|\/)/g).pop();
				$("#uploadFile").val(fileName);
			});
		});
	</script>
	<div class="body body-disabled-float">
		<g:if test="${flash.message}">
			<div class="message">${raw(flash.message)}</div>
		</g:if>

		<div>
			<br />
			<div class="panel panel-default account-import-step1">
				<div class="panel-heading">This form is used to bulk import tasks.</div>
				<div class="panel-body">
					Select the spreadsheet file to be uploaded:<br>
					<g:uploadForm action="importTask">

						<input id="uploadFile" placeholder="Choose File" disabled="disabled" />
						<div class="fileUpload btn btn-default">
							<span>Select</span>
							<input id="file" type="file" class="upload" name="${fileParamName}" />
						</div>

						<input type="hidden" name="step" value="upload" />

						<br />
						<br>
						<button type="submit" class="btn btn-default" id="importTaskSubmitButton" disabled>
							Upload Spreadsheet
							<span class="uploadIcon glyphicon glyphicon-upload" aria-hidden="true"></span>
						</button>

					</g:uploadForm>
				</div>
			</div>

		</div>
	</div>
	<script>
		$('.menu-parent-tasks').addClass('active');
		$('.menu-parent-tasks-import-tasks').addClass('active');
        $(document).ready(function() {
                $("#file").on('change', function() {
                    var fileName  = $(this).val().split(/(\\|\/)/g).pop();
                    var fileExt = fileName.split('.').pop()
                    if(["xls", "xlsx"].indexOf(fileExt) >= 0){
                        $("#uploadFile").val(fileName);
						$("#importTaskSubmitButton").attr('disabled', false)
                    }else{
						$("#importTaskSubmitButton").attr('disabled', true)
						$("#uploadFile").val(null);
                        alert("Please, select a valid file.")
                    }

                });
        });

	</script>
</body>
</html>
