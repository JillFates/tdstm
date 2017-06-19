import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.InitializingBean

class ContolTagLib implements InitializingBean {

    /**
     * Namespace to use: <tds:{tagname} />
     */
    static String namespace = 'tds'

    /**
     * Render custom fields
     * use on gsp as -> <tds:customField field="${customField}" />
     */
    def customField = { Map attrs ->

        def field = attrs.field;
        String fieldValue = attrs.value;
        def tabOffset = attrs.tabOffset;

        String dataLabelHtml = " data-label='"+field.label+"' ";
        String requiredHtml = "";
        if(field.constraints.required){
            requiredHtml = " required "
        }
        String appendHtml = dataLabelHtml + requiredHtml;

        switch (field.control) {
            case 'Select List':
                this.renderSelectListInput(field, fieldValue, appendHtml, tabOffset);
                break;
            case 'String':
                this.renderStringInput(field, fieldValue, appendHtml, tabOffset);
                break;
            default:
                this.renderStringInput(field, fieldValue, appendHtml, tabOffset);
        }
    }

    private void renderSelectListInput(field, fieldValue, appendHtml, tabOffset){
        def options = field.constraints.values;
        if(fieldValue == null ){
            fieldValue = field.default;
        }
        out << "<select " +
                appendHtml +" "+
                "class='customField "+field.imp+"'" +
                "data-label='"+field.label+"'"+
                "id='" + field.field +"'" +
                "name='"+field.field +"'" +
                "title='"+field.tip+"'>"
        options.each{
            String selectedHtml = "";
            if(fieldValue != null && it == fieldValue){
                selectedHtml = " selected=\"selected\" ";
            }
            out << "<option "+selectedHtml+"  value=\" " << it << "\">" << it << "</option>"
        }
        out << "</select>"
    }

    private void renderStringInput(field, fieldValue, appendHtml, tabOffset){
        Integer min = field.constraints.minSize;
        Integer max = field.constraints.maxSize;

        if(min != null && max != null){
            appendHtml += " required pattern='.{"+min+","+max+"}' data-message='Text should be greater than "+min+" and less than "+max+"' ";
        }

        fieldValue = (fieldValue == null ? "" : fieldValue);
        out << "<input type='text' " +
                appendHtml +" "+
                "id='" + field.field +"'" +
                "class='customField " +field.imp+"'" +
                "name='" +field.field +"'" +
                "value='"+fieldValue+"'" +
                "tabindex='"+(tabOffset+1)+"'" +
                "title='"+field.tip+"'/>"
    }

    private void renderDefault(field , fieldValue, appendHtml, tabOffset){
        this.renderStringInput(field , fieldValue, appendHtml, tabOffset)
    }

    void afterPropertiesSet() {
        // do nothing ..
    }
}
