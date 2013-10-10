JClic applets must be signed using a digital key.
To generate a key for test purposes:

- Choose a password. It must be at least 6 characters long. For example, 'passwd'

- Execute the following command, replacing 'passwd' by the password. 
  Note: the 'keytool' utility is usually located into the 'bin' folder 
  of the JDK. If this folder is not in $path, write its full pathname.
  
keytool -genkey -keyalg rsa -alias keyForJClic -keystore my.keystore -storepass myStorePassword -keypass myKeyPassword -validity 365 -v

- Answer the questions. You will be prompted about:
  * Name
  * Organizational unit
  * Organization
  * City
  * State / Province
  * Two letter country code (see http://en.wikipedia.org/wiki/Country_code)
  
  End writting "yes"
  
  This will generate the file 'testcert.keystore'. Of course, you can change all this parameters (name of the keystore file, validity, password, alias, etc.) to fit your needs.
  
- You can export a public certificate using this command:
  
keytool -export -alias testcert -keystore testcert.keystore -storepass passwd -file testcert.cer -v

This will generate the file 'testcert.cer'

Save the files (.keystore and .cer) in a folder of your choice (for example: ~/.keystore), and change the permissions in order to protect it.
  
To use this certificate in the build process of JClic, create a new text file in your home directory and name it ".ant-global.properties". The content of this file should be similar to this:

author=Author name
keystore.dir=/home/user/.keystore
keystore.file=my.keystore
keystore.storepass=myStorePassword
keystore.keypass=myKeyPassword
keystore.alias=keyForJClic

That's all: launch the build process of JClic ("ant clean" followed with "ant") and the .jar files will be signed with your new certificate.
