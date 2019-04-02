; Required for Java detection
!include GetJavaVersion.nsh

; Product version
!ifndef PRODUCT_VERSION
!define PRODUCT_VERSION "0.1.1.8"
!endif

; Constants and variables for Java version check
!define GET_JAVA_URL "http://www.java.com"
!define MIN_JAVA_VERSION "1.7"
Var JAVA_HOME
Var JAVA_VERSION

; Constants for app registry
!define SHCNE_ASSOCCHANGED 0x8000000
!define SHCNF_IDLIST 0

; Define your application name
!define APPNAME "JClic (offline)"
!define APPNAMEANDVERSION "JClic 0.3"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
!define MUI_LANGDLL_ALLLANGUAGES


;BGGradient 000000 800000 FFFFFF
XPStyle on

; Main Install settings
Name "${APPNAMEANDVERSION}"
InstallDir "$PROGRAMFILES\JClic"
InstallDirRegKey HKLM "Software\${APPNAME}" ""
OutFile "jclic-${PRODUCT_VERSION}.exe"

; Modern interface settings
!include "MUI.nsh"

!insertmacro MUI_PAGE_WELCOME
Page custom CheckJavaVersion "" " - Java system check"
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"

!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Set languages (first is default language)
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Arabic"
;!insertmacro MUI_LANGUAGE "Asturian"
!insertmacro MUI_LANGUAGE "Basque"
!insertmacro MUI_LANGUAGE "Bosnian"
!insertmacro MUI_LANGUAGE "Catalan"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro MUI_LANGUAGE "French"
!insertmacro MUI_LANGUAGE "Galician"
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "Greek"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro MUI_LANGUAGE "Portuguese"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro MUI_LANGUAGE "Turkish"
;!insertmacro MUI_LANGUAGE "Venetian"

!insertmacro MUI_RESERVEFILE_LANGDLL

;--------------------------------
;Version Information
  VIProductVersion ${PRODUCT_VERSION}
  VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "JClic"
  ;VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "A test comment"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "XTEC - Departament d'Educació"
  ;VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalTrademarks" "Test Application is a trademark of Fake company"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "© Francesc Busquets & Departament d'Educació de la Generalitat de Catalunya"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "Windows installer of JClic modules"
  VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${PRODUCT_VERSION}"
;--------------------------------


LangString javaNeeded ${LANG_CATALAN} "El JClic necessita un motor Java actualitzat, versió 1.7 o superior. El podeu descarregar gratuïtament des de http://www.java.com"
LangString javaNeeded ${LANG_ENGLISH} "JClic needs an updated Java engine, version 1.7 or higher. You can download it from http://www.java.com"
LangString javaNeeded ${LANG_SPANISH} "JClic necesita un motor Java actualizado, versión 1.7 o superior. Puede descargarlo gratuitamente desde http://www.java.com"
LangString javaNeeded ${LANG_ITALIAN} "JClic richiede un motore Java aggiornato, versione 1.7 o superiore. Puoi scaricarla da http://www.java.com"
LangString javaNeeded ${LANG_GREEK} "Το JClic χρειάζεται μια ενημερωμένη μηχανή Java, έκδοση 1.7 ή μεταγενέστερη. Μπορείτε να την κατεβάσετε από τη διεύθυνση http://www.java.com"
LangString javaNeeded ${LANG_BASQUE} "JClic-ek Java 1.7 edo aurreragoko bertsioa behar du. Hemen lortu ahal duzu: http://www.java.com"
LangString javaNeeded ${LANG_PORTUGUESE} "O Jclic necessita um motor Java actualizado (versão 1.7 ou superior). Pode desacrregá-lo em http://www.java.com"
LangString javaNeeded ${LANG_ARABIC} "JClic needs an updated Java engine, version 1.7 or higher. You can download it from http://www.java.com"
LangString javaNeeded ${LANG_FRENCH} "JClic requiert une version plus récente de la machine java, ver. 1.7 ou supérieure. Vous pouvez la télécharger sur http://www.java.com"
LangString javaNeeded ${LANG_RUSSIAN} "JClic необходима Java машина версии 1.7 или выше. Вы можете загрузить её с сайта http://www.java.com"
LangString javaNeeded ${LANG_GERMAN} "JClic benötigt Java Version 1.7 oder höher. Download unter http://www.java.com"
LangString javaNeeded ${LANG_GALICIAN} "JClic precisa un motor Java actualizado, na versión 1.7 ou posterior. Pode descargala de http://www.java.com"
LangString javaNeeded ${LANG_DUTCH} "JClic heeft een aangepaste Java engine nodig, versie 1.7 of beter. Je kan deze downloaden via http://www.java.com"
LangString javaNeeded ${LANG_PORTUGUESEBR} "JClic requer Java atualizado, versão 1.7 ou superior. Você pode obtê-lo em http://www.java.com"
LangString javaNeeded ${LANG_CZECH} "JClic vyžaduje novější Java engine, verze 1.7 nebo vyšší. Můžete jej nahrát z: http://www.java.com"
LangString javaNeeded ${LANG_TRADCHINESE} "JClic 需要 1.7 以上版本的 Java 引擎， 您可以從 http://www.java.com 網站下載。"
;LangString javaNeeded ${LANG_ASTURIAN} "JClic necesita un motor Java anováu, versión 1.7 o superior. Pues descargalu de baldre dende http://www.java.com"
LangString javaNeeded ${LANG_BOSNIAN} "JClic treba ažuriranu Java engine, verzija 1.7 ili novija. Možete ga preuzeti sa http://www.java.com"
LangString javaNeeded ${LANG_TURKISH} "JClic, 1.7 veya üstü sürümde bir Java motoru gerektiriyor. http://www.java.com'dan indirebilirsiniz."
;LangString javaNeeded ${LANG_VENETIAN} "Senghe vol un motor Java axornà par JClic, version 1.7 o superior. Te pol descargarla da http://www.java.com"

LangString oldJavaMsg ${LANG_CATALAN} "El sistema Java d'aquest ordinador és antic."
LangString oldJavaMsg ${LANG_ENGLISH} "There is an old version of Java installed on this computer."
LangString oldJavaMsg ${LANG_SPANISH} "El sistema Java de este ordenador es antiguo."
LangString oldJavaMsg ${LANG_ITALIAN} "C'è una vecchia versione di Java installata in questo computer."
LangString oldJavaMsg ${LANG_GREEK} "Υπάρχει μια παλαιότερη έκδοση Java εγκατεστημένη σε αυτόν τον υπολογιστή."
LangString oldJavaMsg ${LANG_BASQUE} "Ordenagailu honetan Javaren bertsio zaharra dago instalatuta"
LangString oldJavaMsg ${LANG_PORTUGUESE} "Possui uma versão antiga do Java instalada neste computador."
LangString oldJavaMsg ${LANG_ARABIC} "يوجد إصدار قديم لنظام Java مثبّت على هذا الحاسوب."
LangString oldJavaMsg ${LANG_FRENCH} "Possui uma versão antiga do Java instalada neste computador."
LangString oldJavaMsg ${LANG_RUSSIAN} "Предыдущая версия Java установлена на этом компьютере."
LangString oldJavaMsg ${LANG_GERMAN} "Es ist keine aktuelle Java Version auf diesem Computer installiert."
LangString oldJavaMsg ${LANG_GALICIAN} "Neste ordenador hai instalada unha versión vella de Java."
LangString oldJavaMsg ${LANG_DUTCH} "Er is een oudere versie van Java op deze computer geïnstalleerd."
LangString oldJavaMsg ${LANG_PORTUGUESEBR} "Há uma versão antiga do Java instalada em seu computador."
LangString oldJavaMsg ${LANG_CZECH} "Na vašem pčítači existuje stará verze Javy."
LangString oldJavaMsg ${LANG_TRADCHINESE} "有一個舊版本的 Java 已經安裝在您的電腦中。"
;LangString oldJavaMsg ${LANG_ASTURIAN} "El sistema Java d'esti ordenador ye mui vieyu."
LangString oldJavaMsg ${LANG_BOSNIAN} "Stara verzija Java softvera je instalirana na vašem komjuteru."
LangString oldJavaMsg ${LANG_TURKISH} "Bilgisayarınızda eski sürüm bir Java kurulu."
;LangString oldJavaMsg ${LANG_VENETIAN} "Ghe xé na version vècia de Java istaƚada inte sto ordenaor"

LangString noJavaMsg ${LANG_CATALAN} "No s'ha pogut trobar cap sistema Java instal·lat a aquest ordinador."
LangString noJavaMsg ${LANG_ENGLISH} "It was impossible to find any Java system installed on this computer."
LangString noJavaMsg ${LANG_SPANISH} "No se ha encontrado ningún sistema Java instalado en este ordenador."
LangString noJavaMsg ${LANG_ITALIAN} "Non è stato possibile trovare una versione di Java installata in questo computer."
LangString noJavaMsg ${LANG_GREEK} "Είναι αδύνατον να βρεθεί σύστημα της Java εγκατεστημένο σε αυτόν τον υπολογιστή."
LangString noJavaMsg ${LANG_BASQUE} "Ezin izan da Java sistemarik aurkitu instalatuta ordenagailu honetan"
LangString noJavaMsg ${LANG_PORTUGUESE} "Não foi possível encontrar uma instalação do Java neste computador."
LangString noJavaMsg ${LANG_ARABIC} "يستحيل العثور على نظام Java مثبّت على هذا الحاسوب."
LangString noJavaMsg ${LANG_FRENCH} "Aucune machine Java n'a été détectée sur cet ordinateur."
LangString noJavaMsg ${LANG_RUSSIAN} "Не удалось найти установленную Java на этом компьютере."
LangString noJavaMsg ${LANG_GERMAN} "Auf diesem Computer konnte kein Java-System gefunden werden."
LangString noJavaMsg ${LANG_GALICIAN} "Foi imposíbel atopar ningunha versión de Java instalada neste ordenador."
LangString noJavaMsg ${LANG_DUTCH} "Het was onmogelijk om een geïnstalleerd Java systeem te vinden op deze computer."
LangString noJavaMsg ${LANG_PORTUGUESEBR} "Não foi possível localizar algum sistema de Java em seu computador."
LangString noJavaMsg ${LANG_CZECH} "Nemohu najít žádný instalovaný Java systém na tomto počítači."
LangString noJavaMsg ${LANG_TRADCHINESE} "在您的電腦找不到任何已經安裝的 Java 系統。"
;LangString noJavaMsg ${LANG_ASTURIAN} "Nun s'atopó sistema Java dengún instaláu nel ordenador."
LangString noJavaMsg ${LANG_BOSNIAN} "Nije bilo je moguće pronaći bilo kakav Java sistem instaliran na ovom računalu."
LangString noJavaMsg ${LANG_TURKISH} "Bilgisayarınızda herhangi yüklü Java bulunamadı."
;LangString noJavaMsg ${LANG_VENETIAN} "No xé stà posììbiƚƚe de catar nisun sistèma Java inte sto ordenador"

LangString standardType ${LANG_CATALAN} "Estàndard"
LangString standardType ${LANG_ENGLISH} "Standard"
LangString standardType ${LANG_SPANISH} "Estándar"
LangString standardType ${LANG_ITALIAN} "Tipica"
LangString standardType ${LANG_GREEK} "Τυπική"
LangString standardType ${LANG_BASQUE} "Estandarra"
LangString standardType ${LANG_PORTUGUESE} "Padrão"
LangString standardType ${LANG_ARABIC} "معياري"
LangString standardType ${LANG_FRENCH} "Par défaut"
LangString standardType ${LANG_RUSSIAN} "Стандартная"
LangString standardType ${LANG_GERMAN} "Standard"
LangString standardType ${LANG_GALICIAN} "Estándar"
LangString standardType ${LANG_DUTCH} "Standaard"
LangString standardType ${LANG_PORTUGUESEBR} "Padrão"
LangString standardType ${LANG_CZECH} "Standardní"
LangString standardType ${LANG_TRADCHINESE} "標準"
;LangString standardType ${LANG_ASTURIAN} "Estándar"
LangString standardType ${LANG_BOSNIAN} "Standardno"
LangString standardType ${LANG_TURKISH} "Standart"
;LangString standardType ${LANG_VENETIAN} "Standard"

LangString fullType ${LANG_CATALAN} "Completa"
LangString fullType ${LANG_ENGLISH} "Full"
LangString fullType ${LANG_SPANISH} "Completa"
LangString fullType ${LANG_ITALIAN} "Completa"
LangString fullType ${LANG_GREEK} "Πλήρης"
LangString fullType ${LANG_BASQUE} "Osoa"
LangString fullType ${LANG_PORTUGUESE} "Completa"
LangString fullType ${LANG_ARABIC} "كامل"
LangString fullType ${LANG_FRENCH} "Complète"
LangString fullType ${LANG_RUSSIAN} "Полная"
LangString fullType ${LANG_GERMAN} "Komplett"
LangString fullType ${LANG_GALICIAN} "Completa"
LangString fullType ${LANG_DUTCH} "Volledig"
LangString fullType ${LANG_PORTUGUESEBR} "Completa"
LangString fullType ${LANG_CZECH} "Plná"
LangString fullType ${LANG_TRADCHINESE} "完全"
;LangString fullType ${LANG_ASTURIAN} "Completa"
LangString fullType ${LANG_BOSNIAN} "Puno"
LangString fullType ${LANG_TURKISH} "Tam"
;LangString fullType ${LANG_VENETIAN} "Conplèta"

LangString liteType ${LANG_CATALAN} "Mínima"
LangString liteType ${LANG_ENGLISH} "Lite"
LangString liteType ${LANG_SPANISH} "Mínima"
LangString liteType ${LANG_ITALIAN} "Minima"
LangString liteType ${LANG_GREEK} "Ελάχιστη"
LangString liteType ${LANG_BASQUE} "Gutxienekoa"
LangString liteType ${LANG_PORTUGUESE} "Mínima"
LangString liteType ${LANG_ARABIC} "خفيف"
LangString liteType ${LANG_FRENCH} "Minimale"
LangString liteType ${LANG_RUSSIAN} "Минимальная"
LangString liteType ${LANG_GERMAN} "Minimal"
LangString liteType ${LANG_GALICIAN} "Mínima"
LangString liteType ${LANG_DUTCH} "Licht"
LangString liteType ${LANG_PORTUGUESEBR} "Mínima"
LangString liteType ${LANG_CZECH} "Minimální"
LangString liteType ${LANG_TRADCHINESE} "Lite"
;LangString liteType ${LANG_ASTURIAN} "Mínima"
LangString liteType ${LANG_BOSNIAN} "Lite"
LangString liteType ${LANG_TURKISH} "Temel"
;LangString liteType ${LANG_VENETIAN} "Minimal"

InstType "$(standardType)"
InstType "$(fullType)"
InstType "$(liteType)"

LangString captionStr ${LANG_CATALAN} "Instal·lació del JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_ENGLISH} "JClic ${PRODUCT_VERSION} offline setup"
LangString captionStr ${LANG_SPANISH} "Instalación de JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_ITALIAN} "JClic ${PRODUCT_VERSION} offline setup"
LangString captionStr ${LANG_GREEK} "JClic ${PRODUCT_VERSION} offline εγκατάσταση"
LangString captionStr ${LANG_BASQUE} "JClic-en instalazioa ${PRODUCT_VERSION}"
LangString captionStr ${LANG_PORTUGUESE} "JClic ${PRODUCT_VERSION} instalação offline"
LangString captionStr ${LANG_ARABIC} "JClic ${PRODUCT_VERSION} offline setup"
LangString captionStr ${LANG_FRENCH} "Installation hors-ligne de JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_RUSSIAN} "Автономная установка JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_GERMAN} "JClic ${PRODUCT_VERSION} offline setup"
LangString captionStr ${LANG_GALICIAN} "Instalación de JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_DUTCH} "JClic ${PRODUCT_VERSION} offline setup"
LangString captionStr ${LANG_PORTUGUESEBR} "Configuração offline JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_CZECH} "JClic ${PRODUCT_VERSION} off-line nastavení"
LangString captionStr ${LANG_TRADCHINESE} "JClic ${PRODUCT_VERSION} 離線設定"
;LangString captionStr ${LANG_ASTURIAN} "Instalación offline de JClic ${PRODUCT_VERSION}"
LangString captionStr ${LANG_BOSNIAN} "JClic ${PRODUCT_VERSION} isključena postava"
LangString captionStr ${LANG_TURKISH} "JClic ${PRODUCT_VERSION} çevrimdışı kurulum"
;LangString captionStr ${LANG_VENETIAN} "Istaƚasion de JClic ${PRODUCT_VERSION} fora linia"

Caption "$(captionStr)"

Section "-JClic core" Section1
	
	SectionIn 1 2 3

	; Set Section properties
	SetOverwrite ifnewer

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\"
	File "..\dist\jclic\jclic.jar"
	File "..\LICENSE"
	File "..\CREDITS.txt"
	File "CHANGES.txt"
	File "..\HACKING.txt"
	File "..\INSTALL.txt"

SectionEnd

Section "JClic player" Section2

	SectionIn 1 2 3
	
	; Set Section properties
	SetOverwrite ifnewer
	SetShellVarContext all

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\icons"
	File "..\dist\jclic\icons\install.ico"
	File "..\dist\jclic\icons\jclic.ico"
	SetOutPath "$INSTDIR\"
	
  CreateShortCut "$DESKTOP\JClic.lnk" "javaw" "-jar jclic.jar" "$INSTDIR\icons\jclic.ico"
	CreateDirectory "$SMPROGRAMS\JClic"
	CreateShortCut "$SMPROGRAMS\JClic\JClic.lnk" "javaw" "-jar jclic.jar" "$INSTDIR\icons\jclic.ico"
	
  ReadRegStr $R0 HKCR ".jclic" ""
  StrCmp $R0 "JCLICFile" 0 +2
    DeleteRegKey HKCR "JCLICFile"

  ReadRegStr $R0 HKCR ".jclic.zip" ""
  StrCmp $R0 "JCLIC.ZIPFile" 0 +2
    DeleteRegKey HKCR "JCLIC.ZIPFile"

  WriteRegStr HKCR ".jclic" "" "JClic.project"
  WriteRegStr HKCR ".jclic.zip" "" "JClic.project"
  WriteRegStr HKCR "JClic.project" "" "JClic project file"
  WriteRegStr HKCR "JClic.project\DefaultIcon" "" "$INSTDIR\icons\jclic.ico"
  ReadRegStr $R0 HKCR "JClic.project\shell\open\command" ""
  StrCmp $R0 "" 0 no_open
    WriteRegStr HKCR "JClic.project\shell" "" "open"
    WriteRegStr HKCR "JClic.project\shell\open\command" "" 'javaw -jar "$INSTDIR\jclic.jar" "%1"'
  no_open:

  ReadRegStr $R0 HKCR ".jclic.inst" ""
  StrCmp $R0 "JCLIC.INSTFile" 0 +2
    DeleteRegKey HKCR "JCLIC.INSTFile"
		
  WriteRegStr HKCR ".jclic.inst" "" "JClic.installer"
  WriteRegStr HKCR "JClic.installer" "" "JClic project install script"
  WriteRegStr HKCR "JClic.installer\DefaultIcon" "" "$INSTDIR\icons\install.ico"
  ReadRegStr $R0 HKCR "JClic.install\shell\open\command" ""
  StrCmp $R0 "" 0 no_open_inst
    WriteRegStr HKCR "JClic.install\shell" "" "open"
    WriteRegStr HKCR "JClic.install\shell\open\command" "" 'javaw -jar "$INSTDIR\jclic.jar" "%1"'
  no_open_inst:
	
  System::Call 'Shell32::SHChangeNotify(i ${SHCNE_ASSOCCHANGED}, i ${SHCNF_IDLIST}, i 0, i 0)'
	
SectionEnd

Section "JClic author" Section3

  sectionIn 1 2

	; Set Section properties
	SetOverwrite ifnewer
	SetShellVarContext all

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\"
	File "..\dist\jclic\jclicauthor.jar"
	SetOutPath "$INSTDIR\icons"
	File "..\dist\jclic\icons\author.ico"
	SetOutPath "$INSTDIR\"

	CreateShortCut "$DESKTOP\JClic Author.lnk" "javaw" "-Xmx128m -jar jclicauthor.jar" "$INSTDIR\icons\author.ico"
	CreateDirectory "$SMPROGRAMS\JClic"
	CreateShortCut "$SMPROGRAMS\JClic\JClic Author.lnk" "javaw" "-Xmx128m -jar jclicauthor.jar" "$INSTDIR\icons\author.ico"
	
  ReadRegStr $R0 HKCR ".jclic.zip" ""
  StrCmp $R0 "JCLIC.ZIPFile" 0 +2
    DeleteRegKey HKCR "JCLIC.ZIPFile"

  WriteRegStr HKCR ".jclic" "" "JClic.project"
  WriteRegStr HKCR ".jclic.zip" "" "JClic.project"
  WriteRegStr HKCR "JClic.project" "" "JClic project file"
  WriteRegStr HKCR "JClic.project\DefaultIcon" "" "$INSTDIR\icons\jclic.ico"
  ReadRegStr $R0 HKCR "JClic.project\shell\edit\command" ""
  StrCmp $R0 "" 0 no_edit
    ;WriteRegStr HKCR "JClic.project\shell" "" "edit"
    WriteRegStr HKCR "JClic.project\shell\edit\command" "" 'javaw -Xmx128m -jar "$INSTDIR\jclicauthor.jar" "%1"'
  no_edit:

SectionEnd

Section /o "JClic applet" Section4

	SectionIn 2
	
	; Set Section properties
	SetOverwrite ifnewer
	SetShellVarContext all

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\"
	File "..\dist\jclic\jclicplugin.js"
	File "..\dist\jclic\launchApplet.js"
	File "..\dist\jclic\author.html"

SectionEnd

Section "JClic reports" Section5

	SectionIn 2
	
	; Set Section properties
	SetOverwrite ifnewer
	SetShellVarContext all

	; Set Section Files and Shortcuts
	SetOutPath "$INSTDIR\"
	File "..\dist\jclic\jclicreports.jar"
	SetOutPath "$INSTDIR\icons"
	File "..\dist\jclic\icons\reports.ico"
	SetOutPath "$INSTDIR\"
	
	CreateShortCut "$DESKTOP\JClic Reports.lnk" "javaw" "-jar jclicreports.jar" "$INSTDIR\icons\reports.ico"
	CreateDirectory "$SMPROGRAMS\JClic"
	CreateShortCut "$SMPROGRAMS\JClic\JClic Reports.lnk" "javaw" "-jar jclicreports.jar" "$INSTDIR\icons\reports.ico"
		
SectionEnd


Section -FinishSection

	WriteRegStr HKLM "Software\${APPNAME}" "" "$INSTDIR"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\uninstall.exe"
	WriteUninstaller "$INSTDIR\uninstall.exe"

SectionEnd

; A LangString for the section name
LangString JClicPlayerDesc ${LANG_CATALAN} "Aplicació per visualitzar i realitzar activitats JClic."
LangString JClicPlayerDesc ${LANG_ENGLISH} "Application to play JClic activities."
LangString JClicPlayerDesc ${LANG_SPANISH} "Aplicación para visualizar y realizar actividades JClic."
LangString JClicPlayerDesc ${LANG_ITALIAN} "Applicazione per avviare attività JClic."
LangString JClicPlayerDesc ${LANG_GREEK} "Εφαρμογή αναπαραγωγής δραστηριοτήτων του JClic"
LangString JClicPlayerDesc ${LANG_BASQUE} "JClic jarduerak egiteko aplikazioa"
LangString JClicPlayerDesc ${LANG_PORTUGUESE} "Programa para correr actividades Jclic."
LangString JClicPlayerDesc ${LANG_ARABIC} "تطبيق للعلب على JClic"
LangString JClicPlayerDesc ${LANG_FRENCH} "Programme d'exécution des activités JClic."
LangString JClicPlayerDesc ${LANG_RUSSIAN} "Application to play JClic activities."
LangString JClicPlayerDesc ${LANG_GERMAN} "Anwendung zum Abspielen von JClic Übungen."
LangString JClicPlayerDesc ${LANG_GALICIAN} "Aplicación para reproducir as actividades JClic"
LangString JClicPlayerDesc ${LANG_DUTCH} "Toepassing om JClic activiteiten te spelen"
LangString JClicPlayerDesc ${LANG_PORTUGUESEBR} "Aplicativo para rodar atividades do JClic"
LangString JClicPlayerDesc ${LANG_CZECH} "Aplikace pro přehrávání JClic aktivit."
LangString JClicPlayerDesc ${LANG_TRADCHINESE} "Application to play JClic activities."
;LangString JClicPlayerDesc ${LANG_ASTURIAN} "Aplicación pa xugar a les xeres de JClic."
LangString JClicPlayerDesc ${LANG_BOSNIAN} "Aplikacije za reprodukciju JClic aktivnosti."
LangString JClicPlayerDesc ${LANG_TURKISH} "Application to play JClic activities."
;LangString JClicPlayerDesc ${LANG_VENETIAN} "Programa da inviar par mostrar atività de JClic"

LangString JClicAuthorDesc ${LANG_CATALAN} "Aplicació per crear i modificar activitats JClic."
LangString JClicAuthorDesc ${LANG_ENGLISH} "Application to create and edit JClic activities."
LangString JClicAuthorDesc ${LANG_SPANISH} "Aplicación para crear y modificar actividades JClic."
LangString JClicAuthorDesc ${LANG_ITALIAN} "Applicazione per creare e modificare attività JClic."
LangString JClicAuthorDesc ${LANG_GREEK} "Εφαρμογή δημιουργίας και επεξεργασίας του JClic."
LangString JClicAuthorDesc ${LANG_BASQUE} "JClic jarduerak sortu eta editatzeko aplikazioa"
LangString JClicAuthorDesc ${LANG_PORTUGUESE} "Programa para criar e editar actividades Jclic."
LangString JClicAuthorDesc ${LANG_ARABIC} "تطبيق لإنشاء وتحرير ألعاب JClic"
LangString JClicAuthorDesc ${LANG_FRENCH} "Application pour créer et modifier des activités JClic."
LangString JClicAuthorDesc ${LANG_RUSSIAN} "Application to create and edit JClic activities."
LangString JClicAuthorDesc ${LANG_GERMAN} "Anwendung zum Erstellen und Editieren von JClic Übungen."
LangString JClicAuthorDesc ${LANG_GALICIAN} "Aplicación para crear e editar actividades JClic"
LangString JClicAuthorDesc ${LANG_DUTCH} "Toepassing om JClic activiteiten aan te maken"
LangString JClicAuthorDesc ${LANG_PORTUGUESEBR} "Aplicativo para criar e editar atividades do JClic"
LangString JClicAuthorDesc ${LANG_CZECH} "Aplikace pro vytváření a úpravu JClic aktivit."
LangString JClicAuthorDesc ${LANG_TRADCHINESE} "Application to create and edit JClic activities."
;LangString JClicAuthorDesc ${LANG_ASTURIAN} "Aplicación pa criar y camudar xeres de JClic."
LangString JClicAuthorDesc ${LANG_BOSNIAN} "Aplikacije za stvaranje i uređivanje JClic aktivnosti."
LangString JClicAuthorDesc ${LANG_TURKISH} "Application to create and edit JClic activities."
;LangString JClicAuthorDesc ${LANG_VENETIAN} "Programa par prodùxer e modifegar atività de JClic"

LangString JClicAppletDesc ${LANG_CATALAN} "Fitxers que són necessaris només en un servidor que allotgi applets JClic."
LangString JClicAppletDesc ${LANG_ENGLISH} "Files needed only in a server with hosted JClic applets."
LangString JClicAppletDesc ${LANG_SPANISH} "Archivos que son necesarios únicamente en un servidor que aloje applets JClic."
LangString JClicAppletDesc ${LANG_ITALIAN} "Files necessari solo in un server con applets JClic."
LangString JClicAppletDesc ${LANG_GREEK} "Αρχεία απαραίτητα μόνο σε διακομιστή που φιλοξενεί μικροεφαρμογές του JClic."
LangString JClicAppletDesc ${LANG_BASQUE} "JClic applets-ak ostatzen dituen zerbitzarian bakarrik beharrezko diren fitxategiak"
LangString JClicAppletDesc ${LANG_PORTUGUESE} "Ficheiros necessários apenas num servidor com applets JClic."
LangString JClicAppletDesc ${LANG_ARABIC} "Files needed only in a server with hosted JClic applets."
LangString JClicAppletDesc ${LANG_FRENCH} "Fichiers nécessaires uniquement sur un serveur hébergeant des applets JClic."
LangString JClicAppletDesc ${LANG_RUSSIAN} "Файлы требуются только на сервере, хостящем аплеты JClic."
LangString JClicAppletDesc ${LANG_GERMAN} "Diese Dateien werden nur auf einem Server gebraucht, der JClic Applets hostet."
LangString JClicAppletDesc ${LANG_GALICIAN} "Ficheiros só necesarios nun servidor con applets JClic aloxados"
LangString JClicAppletDesc ${LANG_DUTCH} "Bestanden die enkel nodig zijn in een server met JClic applets."
LangString JClicAppletDesc ${LANG_PORTUGUESEBR} "Arquivos necessários apenas em um servidor com applets JClic hospedados."
LangString JClicAppletDesc ${LANG_CZECH} "Soubory potřebné pouze pro server s hostovanými JClic applety."
LangString JClicAppletDesc ${LANG_TRADCHINESE} "Files needed only in a server with hosted JClic applets."
;LangString JClicAppletDesc ${LANG_ASTURIAN} "Ficheros que son necesarios namás nun sirvidor qu'agospie applets de JClic."
LangString JClicAppletDesc ${LANG_BOSNIAN} "Files needed only in a server with hosted JClic applets."
LangString JClicAppletDesc ${LANG_TURKISH} "Files needed only in a server with hosted JClic applets."
;LangString JClicAppletDesc ${LANG_VENETIAN} "Files che senghe vol nòma che inte un server ospitante applets JClic"

LangString JClicReportsDesc ${LANG_CATALAN} "Servidor d'informes."
LangString JClicReportsDesc ${LANG_ENGLISH} "Reports server."
LangString JClicReportsDesc ${LANG_SPANISH} "Servidor de informes."
LangString JClicReportsDesc ${LANG_ITALIAN} "Server dei rapporti."
LangString JClicReportsDesc ${LANG_GREEK} "Διακομιστής αναφορών"
LangString JClicReportsDesc ${LANG_BASQUE} "Txostenen zerbitzaria."
LangString JClicReportsDesc ${LANG_PORTUGUESE} "Servidor de relatórios."
LangString JClicReportsDesc ${LANG_ARABIC} "خادوم التقارير."
LangString JClicReportsDesc ${LANG_FRENCH} "Serveur de rapports."
LangString JClicReportsDesc ${LANG_RUSSIAN} "Сервер отчётов."
LangString JClicReportsDesc ${LANG_GERMAN} "Reports server."
LangString JClicReportsDesc ${LANG_GALICIAN} "Servidor de informes"
LangString JClicReportsDesc ${LANG_DUTCH} "Rapporten server."
LangString JClicReportsDesc ${LANG_PORTUGUESEBR} "Servidor de relatórios."
LangString JClicReportsDesc ${LANG_CZECH} "Server sestav."
LangString JClicReportsDesc ${LANG_TRADCHINESE} "Reports server."
;LangString JClicReportsDesc ${LANG_ASTURIAN} "Sirvidor d'informes."
LangString JClicReportsDesc ${LANG_BOSNIAN} "Izvješća poslužitelja."
LangString JClicReportsDesc ${LANG_TURKISH} "Sunucu raporları"
;LangString JClicReportsDesc ${LANG_VENETIAN} "Server par i reports"

; Modern install component descriptions
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
	!insertmacro MUI_DESCRIPTION_TEXT ${Section1} "JClic core components"
	!insertmacro MUI_DESCRIPTION_TEXT ${Section2} $(JClicPlayerDesc)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section3} $(JClicAuthorDesc)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section4} $(JClicAppletDesc)
	!insertmacro MUI_DESCRIPTION_TEXT ${Section5} $(JClicReportsDesc)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

;Uninstall section
Section Uninstall

	SetShellVarContext all
	
	;Remove from registry...
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
	DeleteRegKey HKLM "SOFTWARE\${APPNAME}"


  ; Clear registry
  ReadRegStr $R0 HKCR ".jclic" ""
  StrCmp $R0 "JClic.project" 0 +2
    DeleteRegKey HKCR ".jclic"

  ReadRegStr $R0 HKCR ".jclic.zip" ""
  StrCmp $R0 "JClic.project" 0 +2
    DeleteRegKey HKCR ".jclic.zip"

  ReadRegStr $R0 HKCR ".jclic.inst" ""
  StrCmp $R0 "JClic.installer" 0 +2
    DeleteRegKey HKCR ".jclic.inst"
		
  DeleteRegKey HKCR "JClic.project"
  DeleteRegKey HKCR "JClic.installer"

  System::Call 'Shell32::SHChangeNotify(i ${SHCNE_ASSOCCHANGED}, i ${SHCNF_IDLIST}, i 0, i 0)'

	; Delete self
	Delete "$INSTDIR\uninstall.exe"

	; Delete Shortcuts
	Delete "$DESKTOP\JClic.lnk"
	Delete "$SMPROGRAMS\JClic\JClic.lnk"
	Delete "$DESKTOP\JClic Author.lnk"
	Delete "$SMPROGRAMS\JClic\JClic Author.lnk"
	Delete "$DESKTOP\JClic Reports.lnk"
	Delete "$SMPROGRAMS\JClic\JClic Reports.lnk"

	; Clean up JClic core
	Delete "$INSTDIR\activities.jar"
	Delete "$INSTDIR\dbconn.jar"
	Delete "$INSTDIR\extra.jar"
	Delete "$INSTDIR\intl.jar"
	Delete "$INSTDIR\jclic.jar"
	Delete "$INSTDIR\jclicxml.jar"
	Delete "$INSTDIR\jdom.jar"
	Delete "$INSTDIR\jmfhandlers.jar"
	Delete "$INSTDIR\player.jar"
	Delete "$INSTDIR\qt60.jar"
	Delete "$INSTDIR\qt61.jar"
	Delete "$INSTDIR\soundspi.jar"
	Delete "$INSTDIR\utilities.jar"
	Delete "$INSTDIR\COPYING.txt"
  Delete "$INSTDIR\LICENSE"
	Delete "$INSTDIR\CREDITS.txt"
	Delete "$INSTDIR\CHANGES.txt"
	Delete "$INSTDIR\HACKING.txt"
	Delete "$INSTDIR\INSTALL.txt"

	; Clean up JClic
	Delete "$INSTDIR\jclicplayer.jar"
	Delete "$INSTDIR\icons\install.ico"
	Delete "$INSTDIR\icons\jclic.ico"

	; Clean up JClic author
	Delete "$INSTDIR\jclicauthor.jar"
	Delete "$INSTDIR\icons\author.ico"

	; Clean up JClic applet
	Delete "$INSTDIR\jclicapplet.jar"
	Delete "$INSTDIR\jclicplugin.js"
	Delete "$INSTDIR\launchApplet.js"
	Delete "$INSTDIR\author.html"
	Delete "$INSTDIR\edu\xtec\resources\skins\blue.png"
	Delete "$INSTDIR\edu\xtec\resources\skins\blue.xml"
	Delete "$INSTDIR\edu\xtec\resources\skins\listskins.properties"
	Delete "$INSTDIR\edu\xtec\resources\skins\orange.png"
	Delete "$INSTDIR\edu\xtec\resources\skins\orange.xml"
	Delete "$INSTDIR\edu\xtec\resources\skins\simple.png"
	Delete "$INSTDIR\edu\xtec\resources\skins\simple.xml"
	Delete "$INSTDIR\edu\xtec\resources\skins\green.xml"
	Delete "$INSTDIR\edu\xtec\resources\skins\green.png"

	; Clean up JClic reports
	Delete "$INSTDIR\reportServer.jar"
	Delete "$INSTDIR\jclicreports.jar"
	Delete "$INSTDIR\icons\reports.ico"
	
	; Remove remaining directories
	RMDir "$SMPROGRAMS\JClic"
	RMDir "$INSTDIR\edu\xtec\resources\skins\"
	RMDir "$INSTDIR\edu\xtec\resources\"
	RMDir "$INSTDIR\edu\xtec\"
	RMDir "$INSTDIR\edu\"
	RMDir "$INSTDIR\icons\"
	RMDir "$INSTDIR\"

SectionEnd

; On initialization
Function .onInit

	!insertmacro MUI_LANGDLL_DISPLAY

FunctionEnd


Function checkJavaVersion
  ${GetJavaVersion} $JAVA_VERSION
	IfErrors NoJava
	Goto JavaFound
		
NoJava:
	MessageBox MB_OK|MB_ICONSTOP "$(noJavaMsg) $(javaNeeded)"

GetJava:		
	ExecShell "open" ${GET_JAVA_URL}
	Quit

JavaFound:
	Push ${MIN_JAVA_VERSION}
	Push $JAVA_VERSION	
	Call CompareVersions
	Pop $R0
	StrCmp $R0 "1" JavaOk
	
	MessageBox MB_OK|MB_ICONSTOP "$(oldJavaMsg) $(javaNeeded)"
	Goto GetJava

JavaOk:
	DetailPrint "Found JRE $JAVA_VERSION"

FunctionEnd

;-----------------------------------------------------------------------------
 ; CompareVersions
 ; input:
 ;    top of stack = existing version
 ;    top of stack-1 = needed version
 ; output:
 ;    top of stack = 1 if current version => neded version, else 0
 ; version is a string in format "xx.xx.xx.xx" (number of interger sections 
 ; can be different in needed and existing versions)

Function CompareVersions
   ; stack: existing ver | needed ver
   Exch $R0 
   Exch
   Exch $R1 
   ; stack: $R1|$R0

   Push $R1
   Push $R0
   ; stack: e|n|$R1|$R0

   ClearErrors
   loop:
      IfErrors VersionNotFound
      Strcmp $R0 "" VersionTestEnd

      Call ParseVersion
      Pop $R0
      Exch

      Call ParseVersion
      Pop $R1 
      Exch

      IntCmp $R1 $R0 +1 VersionOk VersionNotFound
      Pop $R0
      Push $R0

   goto loop
   
   VersionTestEnd:
      Pop $R0
      Pop $R1
      Push $R1
      Push $R0
      StrCmp $R0 $R1 VersionOk VersionNotFound

   VersionNotFound:
      StrCpy $R0 "0"
      Goto end
      
   VersionOk:
      StrCpy $R0 "1"
end:
   ; stack: e|n|$R1|$R0
   Exch $R0
   Pop $R0
   Exch $R0
   ; stack: res|$R1|$R0
   Exch
   ; stack: $R1|res|$R0
   Pop $R1
   ; stack: res|$R0
   Exch
   Pop $R0
   ; stack: res
FunctionEnd

;---------------------------------------------------------------------------------------
 ; ParseVersion
 ; input:
 ;      top of stack = version string ("xx.xx.xx.xx")
 ; output: 
 ;      top of stack   = first number in version ("xx")
 ;      top of stack-1 = rest of the version string ("xx.xx.xx")
Function ParseVersion
   Exch $R1 ; version
   Push $R2
   Push $R3

   StrCpy $R2 1
   loop:
      StrCpy $R3 $R1 1 $R2
      StrCmp $R3 "." loopend
      StrLen $R3 $R1
      IntCmp $R3 $R2 loopend loopend
      IntOp $R2 $R2 + 1
      Goto loop
   loopend:
   Push $R1
   StrCpy $R1 $R1 $R2
   Exch $R1

   StrLen $R3 $R1
   IntOp $R3 $R3 - $R2
   IntOp $R2 $R2 + 1
   StrCpy $R1 $R1 $R3 $R2

   Push $R1
   
   Exch 2
   Pop $R3
   
   Exch 2
   Pop $R2

   Exch 2
   Pop $R1
FunctionEnd
;---------------------------------------------------------------------------------------

; eof
