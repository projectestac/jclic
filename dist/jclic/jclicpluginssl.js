//
// New version of jclicplugin.js (2017-03-10)
// Now using JClic.js (HTML5 player) instead of JClic java applet
//
// Old script is also available at:
// http://clic.xtec.cat/dist/jclic/jclicplugin-applet.js
//
// For more information see: https://clic.xtec.cat/repo/index.html?page=info
//


// Location of jclic.js
var jsBase = 'https://clic.xtec.cat/dist/jclic.js/jclic.min.js';
function setJsBase(base) {
  jsBase = base;
}

// jarBase is not used bt HTML5
var jarBase = 'https://clic.xtec.cat/dist/jclic';
function setJarBase(base) {
  jarBase = base;
}

var useLanguage = false;
var language = '';
var country = '';
var variant = '';
function setLanguage(l, c, v) {
  if (l) {
    language = l.toString();
    if (c) country = c.toString();
    if (v) variant = v.toString();
    useLanguage = true;
  }
}

var useReporter = false;
var reporterClass = '';
var reporterParams = '';
function setReporter(rClass, rParams) {
  if (rClass) {
    reporterClass = rClass.toString();
    if (rParams) reporterParams = rParams.toString();
    useReporter = true;
  }
}

var useSkin = false;
var skinName = '';
function setSkin(skName) {
  if (skName) {
    skinName = skName.toString();
    useSkin = true;
  }
}

var useCookie = false;
var cookie = '';
function setCookie(text) {
  if (text) {
    cookie = text.toString();
    useCookie = true;
  }
}

var useExitUrl = false;
var exitUrl = '';
function setExitUrl(text) {
  if (text) {
    exitUrl = text.toString();
    useExitUrl = true;
  }
}

var useInfoUrlFrame = false;
var infoUrlFrame = '';
function setInfoUrlFrame(text) {
  if (text) {
    infoUrlFrame = text.toString();
    useInfoUrlFrame = true;
  }
}

var useSequence = false;
var sequence = '';
function setSequence(text) {
  if (text) {
    sequence = text.toString();
    useSequence = true;
  }
}

var useSystemSounds = false;
var systemSounds = false;
function setSystemSounds(value) {
  if (value) {
    systemSounds = value.toString();
    useSystemSounds = true;
  }
}

var useCompressImages = false;
var compressImages = true;
function setCompressImages(value) {
  if (value) {
    compressImages = value.toString();
    useCompressImages = true;
  }
}

var useTrace = false;
var trace = false;
function setTrace(value) {
  if (value) {
    trace = value.toString();
    useTrace = true;
  }
}

var useMyURL = true;
var myURL = window.location.href;

var isFile = false;
if (myURL.indexOf("file:") == 0) {
  isFile = true;
}

function writePlugin(project, width, height, rWidth, rHeight) {
  document.writeln(getPlugin(project, width, height, rWidth, rHeight));
}

function writeParams(project, p) {
  document.writeln(getParams(project, p));
}


function writeTable(w, h, nsw, nsh, s) {
  document.write(getTable(w, h, nsw, nsh, s));
}

function getPlugin(project, width, height, rWidth, rHeight) {
  // Load jclic.js if not already loaded
  if (typeof JClicObject === 'undefined') {
    var jclicScript = document.createElement('script');
    jclicScript.setAttribute('type', 'text/javascript');
    jclicScript.setAttribute('src', jsBase);
    jclicScript.setAttribute('charset', 'utf-8');
    document.head.appendChild(jclicScript);
  }

  var w = width.toString();
  var h = height.toString();
  var nsw = w;
  var nsh = h;
  if (rWidth != null) w = rWidth.toString();
  if (rHeight != null) h = rHeight.toString();

  return '<div class="JClic" ' + getParams(project, w, h) + ' ></div>';

}

function getParams(project, w, h) {

  var options = '';

  if (w)
    options = options + ',"with":"' + w + '"';

  if (h)
    options = options + ',"height":"' + h + '"';

  if (useSequence)
    options = options + ',"sequence":"' + sequence + '"';

  if (useLanguage) {
    var l = language;
    if (country !== '')
      l = l + '_' + country;
    if (variant !== '')
      l = l + '@' + variant;
    options = options + ',"lang":"' + l + '"';
  }

  if (useSkin)
    options = options + ',"skin":"' + skinName + '"';

  if (useExitUrl)
    options = options + ',"exitUrl":"' + exitUrl + '"';

  if (useInfoUrlFrame)
    options = options + ',"infoUrlFrame":"' + infoUrlFrame + '"';

  if (useReporter) {
    options = options + ',"reporter":"' + reporterClass + '"';
    if (reporterParams !== '') {
      reporterParams.split(';').forEach(function (s) {
        if (s.indexOf('=') > 0) {
          var kv = s.split('=');
          options = options + ',"' + kv[0] + '":"' + kv[1] + '"';
        }
      });
    }
  }

  if (useCookie)
    options = options + ',"cookie":"' + cookie + '"';

  if (useSystemSounds)
    options = options + ',"systemSounds":"' + systemSounds + '"';

  if (useCompressImages)
    options = options + ',"compressImages":"' + compressImages + '"';

  if (useTrace)
    options = options + ',"logLevel":"trace"';

  if (useMyURL)
    options = options + ',"myURL":"' + myURL + '"';

  // Check if equivalent project of type 'jclic.js' exists
  if (project.indexOf('clic.xtec.cat/projects') > 0 && project.indexOf('/jclic/') > 0 && project.match(/\.jclic\.zip$/)) {
    project = project.replace(/^http:\/\//, 'https://').replace(/\/jclic\//, '/jclic.js/').replace(/\.zip$/, '');
  }

  var htmlcode = ' data-project="' + project + '"';
  if (options.length > 0)
    htmlcode = htmlcode + ' data-options=\'{' + options.substring(1) + '}\'';

  return htmlcode;
}

function getTable(w, h, nsw, nsh, s) {
  var htmlcode = '';
  htmlcode += '<table ' + s;
  if (_ie == true) {
    if (w != null) htmlcode += ' width=' + w;
    if (h != null) htmlcode += ' height=' + h;
  }
  else {
    if (nsw != null) htmlcode += ' width=' + nsw;
    if (nsh != null) htmlcode += ' height=' + nsh;
  }
  htmlcode += '>';
  return htmlcode;
}
