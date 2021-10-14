# OpenJfx Jcr Browser

#### GraalVM
Runs with maven [gluonfx-plugin](hhttps://github.com/gluonhq/gluonfx-maven-plugin) and [spring-native](https://github.com/spring-projects-experimental/spring-native) (they finally work together!) to generate AOT compiled executable. This doesn't even require a jvm at all anymore.

Download [graalvm 21.2.x](https://github.com/graalvm/graalvm-ce-builds/releases)
Set `GRAALVM_HOME` environment variable similar like setting `JAVA_HOME`
Install additional software required, read [here](https://docs.gluonhq.com/#_platforms) on what's required on which platform.

On ubuntu I have also installed libavcodec-dev libavformat-dev libavutil-dev libasound2-dev to make it work.

On windows you'll need [Visual Studio 2019 Community Edition](https://visualstudio.microsoft.com/downloads) plus specific components.
Maven needs to be started in `x64 Native Tools Command Prompt for VS 2019` NOT just the cmd or "Developer Command Prompt". Make sure it's x64. 

    mvn clean package gluonfx:build
    ./target/gluonfx/YOUR_PLATFORM/OpenJfxJcrBrowser-Desktop-Graalvm-bin
    




