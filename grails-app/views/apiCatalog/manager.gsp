<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="topNav"/>

	<title>API Dictionaries</title>
	<link rel="stylesheet" href="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.css"/>
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'codemirror/codemirror.css')}" />
	<style type="text/css">
        div#dictionaryView {
            overflow: scroll;
            height: 600px;
        }
        .CodeMirror {
            border: 1px solid #d2d6de;
            height: auto;
        }
        .CodeMirror-scroll {
            overflow: auto;
            height: 600px;
            /* This is needed to prevent an IE[67] bug where the scrolled content
			   is visible outside of the scrolling box. */
            position: relative;
        }
	</style>
</head>
<body>
<tds:subHeader title="API Dictionaries" crumbs="['Admin','API Dictionaries']"/>

<div class="row">
    <div class="col-md-6">
        <div class="form-group">
            <label class="col-sm-4 col-form-label text-right" for="apiCatalogs" style="margin-top: 4px">
                API Dictionaries Actions:
            </label>
            <div class="col-sm-8">
                <select id="apiCatalogs" name="apiCatalogs" class="form-control"></select>
            </div>
        </div>
    </div>
    <div class="col-md-6 text-right">
        <div class="btn-group" role="group" aria-label="">
            <button type="button" class="btn btn-primary collapse">
                <span class="glyphicon glyphicon-resize-small" aria-hidden="true"></span>
                Collapse
            </button>
            <button type="button" class="btn btn-primary expand">
                <span class="glyphicon glyphicon-resize-full" aria-hidden="true"></span>
                Expand
            </button>
            <button type="button" class="btn btn-primary toggle">
                <span class="glyphicon glyphicon-random" aria-hidden="true"></span>
                &nbsp;
                Toggle
            </button>
            <button type="button" class="btn btn-primary toggle-1">
                <span class="glyphicon glyphicon-transfer" aria-hidden="true"></span>
                &nbsp;
                Toggle 1
            </button>
            <button type="button" class="btn btn-primary toggle-2">
                <span class="glyphicon glyphicon-transfer" aria-hidden="true"></span>
                &nbsp;
                Toggle 2
            </button>
        </div>
    </div>
</div>
<div class="row">
	<div class="col-md-6">
		<form method="post">
			<fieldset>
				<textarea class="form-control" name="dictionary" id="dictionary" rows="30" style="width: 100%;"></textarea>
				<br>
				<div class="col-md-12">
                    <button class="btn btn-primary" type="button" onclick="viewMergedDictionaryJson()">
                        <i class="fa fa-fw fa-eye"></i>
                        View Merged
                    </button>
                    <button class="btn btn-primary" type="button" onclick="saveDictionaryJson()">
                        <i class="fa fa-fw fa-floppy-o"></i>
                        Save
                    </button>
                    <button class="btn btn-danger" type="button" onclick="deleteSelectedOne()">
                        <i class="fa fa-fw fa-trash-o"></i>
                        Delete
                    </button>
                    <input name="id" id="id" type="hidden" value=""/>
                    <input name="version" id="version" type="hidden" value=""/>
				</div>
                <br>
			</fieldset>
		</form>
	</div>
	<div class="col-md-6">
		<div class="form-control" id="dictionaryView"></div>
	</div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.textcomplete/1.8.4/jquery.textcomplete.js"></script>
<script type="text/javascript" src="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.js"></script>
<g:javascript src="codemirror/codemirror.js" />
<script>
    // dictionary editor code mirror instance
    var editor = null;

    function viewMergedDictionaryJson() {
        try {
            var payload = {
                id: null,
                version: 0,
                dictionary: editor.getValue()
            };

            $.ajax('/tdstm/apiCatalog/viewMerged', {
                type       : 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType   : 'json',
                data       : JSON.stringify(payload),
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        return
                    }

                    showJson(data.model.dictionaryTransformed)
                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
        }
    }

    function saveDictionaryJson() {
        try {
            var payload = {
                id: $('input#id').val() === '' ? null : $('input#id').val(),
                version: $('input#version').val() === '' ? 0 : $('input#version').val(),
                dictionary: editor.getValue()
            };

            $.ajax('/tdstm/apiCatalog/save', {
                type       : 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType   : 'json',
                data       : JSON.stringify(payload),
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        return
                    }
                    refreshCatalogsDropDown(data.model.id)
                    showJson(data.model.dictionaryTransformed)
                    $('input#id').val(data.model.id);
                    $('input#version').val(data.model.version)
                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
        }
    }

    function viewPretty(id) {
        if (id === '-1') {
            cleanFields()
            return;
        }
        try {
            $.ajax('/tdstm/apiCatalog/viewPretty/' + id, {
                type       : 'GET',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        $('input#id').val('');
                        $('input#version').val('');

                        return
                    }

                    showDictionary(data.model.dictionary)
                    showJson(data.model.dictionaryTransformed);
                    $('input#id').val(data.model.id);
                    $('input#version').val(data.model.version);

                },
                error      : function (xhr, status, text) {

                    $('input#id').val('');
                    $('input#version').val('');

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
            $('input#id').val('');
            $('input#version').val('');
        }
    }

    function deleteSelectedOne() {
        var id = $('input#id').val();
        if (id === '-1' || id === '') {
            cleanFields()
            return;
        }

        if (!confirm('Are you sure to delete this Api Catalog?')) {
            return;
        }

        try {
            $.ajax('/tdstm/apiCatalog/delete/' + id, {
                type       : 'DELETE',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        cleanFields()
                        $('div#dictionaryView').html(data.errors);

                        return
                    }

                    cleanFields()
                    refreshCatalogsDropDown()

                },
                error      : function (xhr, status, text) {

                    cleanFields();

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            cleanFields()
            alert(e)
        }
    }

    function refreshCatalogsDropDown(id) {
        try {
            $.ajax('/tdstm/apiCatalog/list', {
                type       : 'GET',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        alert(data.errors)
                        return
                    }

                    var options = $('select#apiCatalogs');
                    options.empty();
                    options.append(new Option('--- Choose an API Dictionary for viewing or editing ---', '-1'));
                    $.each(data.model, function(key, value) {
                        options.append(new Option(value.provider.name + ' - ' + value.name, value.id));
                    });

                    // select option matching id
                    $(options).val(id);

                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        alert(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            alert(msg)
                        }
                    }
                }
            });
        } catch (e) {
            alert(e)
        }
    }

    function showDictionary(dictionary) {
        editor.getDoc().setValue(dictionary);
    }

    function showJson(jsonDictionaryTransformed) {
        $('div#dictionaryView').JSONView(jsonDictionaryTransformed, {collapsed: true});
        $('div#dictionaryView').JSONView('expand', 1);
    }

    function cleanFields() {
        editor.getDoc().setValue('');
        $('div#dictionaryView').html('');
        $('input#id').val('');
        $('input#version').val('');
    }

    $(document).ready(function() {
        editor = CodeMirror.fromTextArea(document.getElementById('dictionary'), {lineNumbers: true, viewportMargin: 199});
        $('select#apiCatalogs').on('change', function() {
            viewPretty($(this).val());
        });
        refreshCatalogsDropDown();
        $('button.collapse').on('click', function() { $('div#dictionaryView').JSONView('collapse'); });
        $('button.expand').on('click', function() { $('div#dictionaryView').JSONView('expand'); });
        $('button.toggle').on('click', function() { $('div#dictionaryView').JSONView('toggle'); });
        $('button.toggle-1').on('click', function() { $('div#dictionaryView').JSONView('toggle', 1); });
        $('button.toggle-2').on('click', function() { $('div#dictionaryView').JSONView('toggle', 2); });
    });

</script>

</body>
</html>
