@ECHO OFF

rem The script depends on various environment variables set when called via maven

set MAIN_JAR=%ARTEFACT_ID%-%PROJECT_VERSION%.jar

rem build path with backslash from mainclass name
set MAIN_CLASS_FILEPATH=%MAIN_CLASS:.=\%

echo name: %NAME%
echo java home: %JAVA_HOME%
echo java version: %JAVA_VERSION%
echo project version: %PROJECT_VERSION%
echo app version: %APP_VERSION%
echo main JAR file: %MAIN_JAR%
echo main-class: %MAIN_CLASS%
echo mainclass filePath part: %MAIN_CLASS_FILEPATH%
echo vendor: %VENDOR%
echo copyright: %COPYRIGHT%


rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST target\java-runtime rmdir /S /Q  .\target\java-runtime
IF EXIST target\installer rmdir /S /Q target\installer

xcopy /S /Q target\libs\* target\installer\input\libs\
copy target\%MAIN_JAR% target\installer\input\libs\

rem ------ REQUIRED MODULES ---------------------------------------------------
rem Use jlink to detect all modules that are required to run the application.
rem Starting point for the jdep analysis is the set of jars being used by the
rem application.

echo detecting required modules

"%JAVA_HOME%\bin\jdeps" ^
  --multi-release %JAVA_VERSION% ^
  --ignore-missing-deps ^
  --class-path "target\installer\input\libs\*" ^
  --print-module-deps target\classes\%MAIN_CLASS_FILEPATH%.class > temp.txt

set /p detected_modules=<temp.txt

echo detected modules: %detected_modules%

rem ------ MANUAL MODULES -----------------------------------------------------
rem jdk.crypto.ec has to be added manually bound via --bind-services or
rem otherwise HTTPS does not work.
rem
rem See: https://bugs.openjdk.java.net/browse/JDK-8221674

set manual_modules=jdk.crypto.ec,jdk.localedata,java.desktop,java.naming,java.scripting,jdk.unsupported
echo manual modules: %manual_modules%

rem ------ RUNTIME IMAGE ------------------------------------------------------
rem Use the jlink tool to create a runtime image for our application. We are
rem doing this is a separate step instead of letting jlink do the work as part
rem of the jpackage tool. This approach allows for finer configuration and also
rem works with dependencies that are not fully modularized, yet.

echo creating java runtime image

call "%JAVA_HOME%\bin\jlink" ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2 ^
  --strip-debug ^
  --strip-native-commands ^
  --add-modules %detected_modules%,%manual_modules% ^
  --output target/java-runtime


rem ------ PACKAGING ----------------------------------------------------------
rem In the end we will find the package inside the target/installer directory.

rem this creates MSI installer with WIX toolset
call "%JAVA_HOME%\bin\jpackage" ^
  --type msi ^
  --dest target/installer ^
  --input target/installer/input/libs ^
  --name %NAME%_setup ^
  --main-class %MAIN_CLASS% ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image target/java-runtime ^
  --icon src/main/logo/logo_200x200.ico ^
  --app-version %APP_VERSION% ^
  --vendor "%VENDOR%" ^
  --copyright "%COPYRIGHT%" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-per-user-install ^
  --win-menu

rem this creates a setup .exe with INNO setup   
call "%JAVA_HOME%\bin\jpackage" ^
  --type msi ^
  --dest target/installer ^
  --input target/installer/input/libs ^
  --name %NAME%_setup ^
  --main-class %MAIN_CLASS% ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image target/java-runtime ^
  --icon src/main/logo/logo_200x200.ico ^
  --app-version %APP_VERSION% ^
  --vendor "%VENDOR%" ^
  --copyright "%COPYRIGHT%" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-per-user-install ^
  --win-menu  
