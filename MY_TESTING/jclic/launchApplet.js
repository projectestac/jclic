
function launchApplet(url, popW, popH) {
  var p = (url || '').indexOf('?');
  if (p > 0 && p < url.length) {
    var search = url.substr(p + 1).split('&');
    var searchObj = {};
    for (var i = 0; i < search.length; i++) {
      var k = search[i].indexOf('=');
      if (k >= 0)
        searchObj[search[i].substring(0, k)] = search[i].substring(k + 1);
    }
    if (searchObj.project && searchObj.project.indexOf('//clic.xtec.cat/projects/') > 0)
      url = searchObj.project.replace(/^http:\/\//, 'https://').replace(/\/jclic\/[\w\/]*\.jclic\.zip$/, '/jclic.js/index.html');
    // TODO: Process additional params
  }
  launchAppletWH_legacy(url, popW || 840, popH || 620);
}

function launchAppletWH(url, popW, popH) {
  launchApplet(url, popW, popH);
}

function launchApplet_legacy(url) {
  launchAppletWH(url, 788, 540);
}

function launchAppletWH_legacy(url, popW, popH) {
  var w = window.screen.availWidth || 840,
    h = screen.availHeight || 720,
    x0 = 0, y0 = 0;
  if (popW > (w - 12))
    popW = w - 12;
  if (popH > (h - 28))
    popH = h - 28;
  var leftPos = 0, topPos = 0;
  if (w > 800 && h > 600) {
    leftPos = (w - popW) / 2;
    topPos = (h - popH) / 2;
  }
  var params = 'scrollbars=no,resizable=yes,width=' + popW + ',height=' + popH + ',top=' + topPos + ',left=' + leftPos + ',screenY=' + topPos + ',screenX=' + leftPos;
  window.open(url, 'JClicAppletWindow', params);
}
