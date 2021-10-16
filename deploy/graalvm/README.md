# OpenJfx Jcr Browser

## GraalVM AOT compilation

Runs with maven [gluonfx-plugin](hhttps://github.com/gluonhq/gluonfx-maven-plugin) and [spring-native](https://github.com/spring-projects-experimental/spring-native) (they finally work together!) to generate AOT compiled executable. This doesn't even require a jvm at all anymore.

### Required additional software
Download [graalvm 21.2.x](https://github.com/graalvm/graalvm-ce-builds/releases)
Set `GRAALVM_HOME` environment variable similar like setting `JAVA_HOME`. Install additional software required, read [here](https://docs.gluonhq.com/#_platforms) on what's required on which platform.

You can only compile for the platform the build is executed from.

**Mac** needs Xcode version 11 or higher.
On **Linux** I have also installed libavcodec-dev libavformat-dev libavutil-dev libasound2-dev to make it work.
On **windows** you'll need [Visual Studio 2019 Community Edition](https://visualstudio.microsoft.com/downloads) plus specific components. Maven needs to be started in `x64 Native Tools Command Prompt for VS 2019`. NOT just the cmd or "Developer Command Prompt". Make sure it's x64. 

### Build
    mvn clean package gluonfx:build
    
 Then you can run it:
    
    ./target/gluonfx/YOUR_PLATFORM/OpenJfxJcrBrowser-Desktop-Graalvm-bin

### Reflection    
You might run into ClassNotFound issues, when introducing a new lib that makes use of reflection.
You can add those classes to the reflectionList of the gluonfx-maven-plugin. Alternatively you can run the app in hotspot-mode with tracing-agent:

    mvn gluonfx:runagent

and manually click the app and trigger code execution of that lib. Generates tons of configs that can graalvm give hints in:
src/main/resources/META-INF/native-image

https://docs.gluonhq.com/#_gluonfxrunagent
