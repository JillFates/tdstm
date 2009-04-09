// currently this is prototype code. 
// TODO: it should be made into pure JS for those who don't use prototype in their grails app
function submit_jasperForm(link) {
    link.up('form').down('input[name="_format"]').value = link.title;
    link.up('form').submit();
    return false;
}
