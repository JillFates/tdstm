<%--
-- This is the standard include of CSS and Javascript files necessary throughout the TM application
--%>

<link rel="shortcut icon" type="image/x-icon" href="${resource(dir: 'images', file: 'favicon.ico')}"/>

<!-- Bootstrap 3.3.5 -->
<link rel="stylesheet" href="${resource(dir: 'dist/js/vendors/bootstrap/dist/css', file: 'bootstrap.min.css')}">
<!-- Font Awesome -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/fontawesome', file: 'font-awesome.min.css')}">
<!-- Ionicons -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/ionicons/2.0.1/css', file: 'ionicons.min.css')}">
<!-- Theme style -->
<link rel="stylesheet" href="${resource(dir: 'dist/css', file: 'TDSTMLayout.min.css')}">
<!-- General Template Style -->
<link rel="stylesheet" href="${resource(dir: 'tds/web-app/css', file: 'style.css')}">
<!-- Blue Skin -->
<link rel="stylesheet" href="${resource(dir: 'dist/css/skins', file: 'skin-blue.min.css')}">

<!-- Added to support user menu -->
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'main.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'tds.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'ui.core.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'ui.dialog.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'ui.theme.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'ui.datetimepicker.css')}"/>
<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'jquery-ui-smoothness.css')}"/>
<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet" type="text/css"
      href="${resource(dir: 'plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness', file: 'jquery-ui-1.8.15.custom.css')}"/>


<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
<script src="${resource(dir: 'dist/js/vendors/html5shiv/dist', file: 'html5shiv.min.js')}"></script>
<script src="${resource(dir: 'dist/js/vendors/respond/dest', file: 'respond.min.js')}"></script>
<![endif]-->

<!-- jQuery -->
<script src="${resource(dir: 'dist/js/vendors/jquery/dist', file: 'jquery.min.js')}"></script>
<script  src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"
  integrity="sha256-T0Vest3yCU7pafRw9r+settMBX6JkKN06dqBnpQ8d30="
  crossorigin="anonymous"></script>
<script src="${resource(dir: 'tds/node_modules/tinymce', file: 'tinymce.min.js')}"></script>
<script src="${resource(dir: 'tds/node_modules/tinymce/themes/modern', file: 'theme.min.js')}"></script>
<!-- LEGACY CODE END -->

<!-- Boosttrap -->
<script src="${resource(dir: 'dist/js/vendors/bootstrap/dist/js', file: 'bootstrap.min.js')}"></script>
<!-- SlimScroll -->
<script src="${resource(dir: 'dist/js/vendors/jquery-slimscroll', file: 'jquery.slimscroll.min.js')}"></script>
<!-- FastClick -->
<script src="${resource(dir: 'dist/js/vendors/fastclick/lib', file: 'fastclick.js')}"></script>
<!-- TDSTMLayout App -->
<script src="${resource(dir: 'dist/js', file: 'TDSTMLayout.min.js')}"></script>

<!-- Added to support user menu -->
<g:javascript src="moment.min.js" />
<g:javascript src="moment-timezone-with-data.min.js" />
<g:javascript src="timezone/jquery.timezone-picker.min.js" />
<g:javascript src="timezone/jquery.maphilight.min.js" />
<script src="${resource(dir: 'js', file: 'tds-common.js')}"></script>
<g:javascript src="person.js"/>
<g:javascript src="PasswordValidation.js" />

<!-- Added to support user menu -->
<script type="text/javascript">

    var currentURL = '';
    (function ($) {
        currentURL = window.location.pathname;
    })(jQuery);

    // TODO : JPM 10/2014 : Need to refactor this javascript functions to not be global
    var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
    var dateRegExpForExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
    var currentMenuId = "";
    var taskManagerTimePref = "60"

</script>
<g:javascript src="tdsmenu.js" />