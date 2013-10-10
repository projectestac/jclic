function launchApplet(url){
    launchAppletWH(url, 788, 540);
}

function launchAppletWH(url, popW, popH){
    var w = 800, h = 600, x0=0, y0=0;
    if (document.all || document.layers) {
      w = screen.availWidth;
      h = screen.availHeight;
    }
    if(popW>(w-12))
      popW=w-12;
    if(popH>(h-28))
      popH=h-28;
    var leftPos = 0, topPos = 0;
    if(w>800 && h>600){
      leftPos = (w-popW)/2;
      topPos = (h-popH)/2;
    }
    window.open(url,'JClicAppletWindow','scrollbars=no,resizable=yes,width=' + popW + ',height=' + popH + ',top=' + topPos + ',left=' + leftPos+ ',screenY=' + topPos + ',screenX=' + leftPos);
}
