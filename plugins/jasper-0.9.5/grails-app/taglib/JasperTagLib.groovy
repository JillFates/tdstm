class JasperTagLib {

    JasperService jasperService

// tags existing as of plugin version 0.9
    def jasperReport = {attrs, body ->
        validateAttributes(attrs)
        String jasperName = attrs['jasper']
        String jasperNameNoPunct = jasperName.replaceAll(/[^a-zA-Z0-9]/, '')
        String appPath = grailsAttributes.getApplicationUri(request)
        String webAppPath = appPath + pluginContextPath
        String idAttr = attrs['id'] ?: ""
        String reportName = attrs['name'] ?: ""
        String delimiter = attrs['delimiter'] ?: "|"
        String delimiterBefore = attrs['delimiterBefore'] ?: delimiter
        String delimiterAfter = attrs['delimiterAfter'] ?: delimiterBefore
        String description = attrs['description'] ?: (reportName ? "<strong>${reportName}</strong>" : "")
        String formClass = attrs['class'] ?: "jasperReport"
        String buttonClass = attrs['buttonClass'] ?: "jasperButton"
        String heightAttr = attrs['height'] ? ' height="' + attrs['height'] + '"' : '' // leading space on purpose
        boolean buttonsBelowBody = (attrs['buttonPosition'] ?: 'top') == 'bottom'

        String controller = attrs['controller'] ?: "jasper"
        String action = attrs['action'] ?: "";

        if (body()) {
            // The tag has a body that, presumably, includes input field(s), so we need to wrap it in a form
            out << renderJavascriptForForm(jasperNameNoPunct)
            out << """<form class="${formClass}"${idAttr ? ' id="' + idAttr + '"' : ''} name="${jasperName}" action="${appPath}/${controller}/${action}">"""
            out << """<input type="hidden" name="_format" />
<input type="hidden" name="_name" value="${reportName}" />
<input type="hidden" name="_file" value="${jasperName}" />
"""

            if (attrs['inline']) {
                out << """<input type="hidden" name="_inline" value="${attrs['inline']}" />\n"""
            }

            if (buttonsBelowBody) {
                out << description << body() << "&nbsp;"
                out << renderButtons(attrs, delimiter, delimiterBefore, delimiterAfter, buttonClass, jasperNameNoPunct, webAppPath,heightAttr)
            } else {
                out << renderButtons(attrs, delimiter, delimiterBefore, delimiterAfter, buttonClass, jasperNameNoPunct, webAppPath,heightAttr)
                out << "&nbsp;" << description << body()
            }

            out << "</form>"
        } else {
            /*
             * The tag has no body, so we don't need a whole form, just a link.
             * Note that GSP processing is not recursive, so we cannot use a g:link here.  It has to already be an A tag.
             */
            String result = delimiterBefore
            attrs['format'].toUpperCase().split(",").eachWithIndex {it, i ->
                if (i > 0) result += delimiter
                result += """ <a class="${buttonClass}" title="${it.trim()}" href="${appPath}/${controller}/${action}?_format=${it.trim().encodeAsHTML()}&_name=${reportName.trim().encodeAsHTML()}&_file=${jasperName.trim().encodeAsHTML()}"> """
                result += """<img border="0" src="${webAppPath}/images/icons/${it.trim()}.gif"${heightAttr} /></a> """
            }
            result += delimiterAfter+' '+description
            out << result

        }
    }

    protected String renderJavascriptForForm(jasperNameNoPunct) {
        // function gets a unique name in case this tag is repeated on the page
        """
      <script type="text/javascript">
        function submit_${jasperNameNoPunct}(link) {
          link.parentNode._format.value = link.title;
          link.parentNode.submit();
          return false;
        }
      </script>
        """
        }

    protected String renderButtons(attrs, delimiter, String delimiterBefore, String delimiterAfter, buttonClass, jasperNameNoPunct, webAppPath,heightAttr) {
        String result = delimiterBefore
        attrs['format'].toUpperCase().split(",").eachWithIndex {it, i ->
            if (i > 0) result += delimiter
            result += """
        <a href="#" class="${buttonClass}" title="${it.trim()}" onClick="return submit_${jasperNameNoPunct}(this)">
        <img border="0" src="${webAppPath}/images/icons/${it.trim()}.gif"${heightAttr} /></a>
      """
        }
        result += delimiterAfter
        return result
    }

    private void validateAttributes(attrs) {
      def requiredAttrs = ['jasper','format']
      requiredAttrs.each {attrName ->
      if (!attrs[attrName]) {
        // TODO more appropriate Exception type
        throw new Exception(message(code:"jasper.taglib.missingAttribute", args:["${attrName}", "${requiredAttrs.join(', ')}"]))
      }
      }
        //Verify the 'format' attribute
        def availableFormats = ["PDF", "HTML", "XML", "CSV", "XLS", "RTF", "TEXT"]
        attrs.format.toUpperCase().split(",").each {
            if (!availableFormats.contains(it.trim())) {
                // TODO more appropriate Exception type
                throw new Exception(message(code: "jasper.taglib.invalidFormatAttribute", args: ["${it}", "${availableFormats}"]))
            }
        }
    }

//Beginning of the new Tags added at plugin version 0.9.5    
    def jasperForm = {attrs, body ->
        if(!attrs['jasper']){throw new Exception(message(code:"jasper.taglib.missingAttribute", args:'jasper'))}
        String jasperName = attrs['jasper']
        String appPath = grailsAttributes.getApplicationUri(request)
        String webAppPath = appPath + pluginContextPath
        String id = attrs['id'] ?: ""
        String reportName = attrs['name'] ?: ""
        String formClass = attrs['class'] ?: "jasperReport"
        String controller = attrs['controller'] ?: "jasper"
        String action = attrs['action'] ?: "";
        
        out << """
            <form class="${formClass}" name="${jasperName}" action="${appPath}/${controller}/${action}/${id}">
            <input type="hidden" name="_format" />
            <input type="hidden" name="_name" value="${reportName}" />
            <input type="hidden" name="_file" value="${jasperName}" />
        """
        out << body()
        out << "</form>"
    }
    
    //One button for each type of output you would like to make available.
    def jasperButton = {attrs ->
        if(!attrs['format']){throw new Exception(message(code:"jasper.taglib.missingAttribute", args:'format'))}
        String buttonClass = attrs['class']
        String format = attrs['format'].toUpperCase()
        String text = attrs['text']

        out << """ 
            <a href="#" class="${format}Button ${buttonClass}" title="${format}" onClick="return submit_jasperForm(this)" >$text</a>
        """
    }
    
    // include required JS and CSS that the other tags depend on
    def jasperIncludes = {
        def jsFolder    = createLinkTo(dir:'plugins',file:'jasper-0.9.5/js')
        def cssFolder   = createLinkTo(dir:'plugins',file:'jasper-0.9.5/css')
        
        out << """
    		<link rel='stylesheet' type="text/css" href="${cssFolder}/jasper.css" />
    		<script type='text/javascript' src="${jsFolder}/jasper.js" />
        """
    }
}
