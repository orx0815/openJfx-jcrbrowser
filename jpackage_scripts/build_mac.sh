#!/bin/bash

# The script depends on various environment variables set when called via maven

source ./jpackage_scripts/jLink_jDeps.sh   

# ------ PACKAGING ----------------------------------------------------------
# A loop iterates over the various packaging types supported by jpackage. In
# the end we will find all packages inside the target/installer directory.

for type in "dmg" "pkg"
do
  echo "Creating mac installer of type ... $type"

  $JAVA_HOME/bin/jpackage \
  --type $type \
  --dest target/installer \
  --input target/installer/input/libs \
  --name $NAME \
  --main-class ${MAIN_CLASS} \
  --main-jar ${MAIN_JAR} \
  --java-options -Xmx2048m \
  --runtime-image target/java-runtime \
  --icon src/main/logo/logo_128x128.icns \
  --app-version ${APP_VERSION} \
  --vendor "${VENDOR}" \
  --copyright "${COPYRIGHT}" \
  --mac-package-identifier ${MAIN_CLASS} \
  --mac-package-name $NAME
  
done

