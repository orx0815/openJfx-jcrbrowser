# OpenJfx Jcr Browser


Basic scaffolding for visual Rapid-Application-Development of business-applications completely in Java. 

- Github actions generate a variety of deployables with maven.
JPMS-jlink-jpackaged installer's for Win/Mac/Linux, aot-compiled binaries with graal-vm, several browser-based variants, old-school excecutable jars.

- Quickstart with WYSIWGY Gui-Editor ([Scene Builder](https://gluonhq.com/products/scene-builder/)) to help with getting into javaFx development.

- Proof-of-concept application that connects to existing content-management servers running [apache-sling](https://sling.apache.org/), e.g [Adobe-AEM](https://business.adobe.com/products/experience-manager/adobe-experience-manager.html) via the  [Java-Content-Repositoriy API](https://dzone.com/articles/java-content-repository-best). Data-Maintenance for employees/CMS-authors with a user in the repository. No need to install anything on the server. 

### How to run
##### Requirements
- java11+ 
- maven 3.6.x

Make sure `JAVA_HOME` is properly set to the Java installation directory.

**Build the project**

    mvn install

Builds the modules.

    
**Run the project**

    mvn -f modules/desktop/pom.xml exec:java

or from spring-boot-maven-plugin:
     
    mvn -f modules/desktop/pom.xml spring-boot:run
    
or with the [javafx-maven-plugin](https://github.com/openjfx/javafx-maven-plugin)

    mvn -f modules/desktop/pom.xml javafx:run
 
Note that 'mvn javafx:jlink' won't work, as the application is not modularized due to non-modular dependencies.

**Debug** (and prevent screen-grab from ide) [1]

    mvn exec:exec -Dexec.executable=$JAVA_HOME"/bin/java" -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044 -Dglass.disableGrab=true org.motorbrot.javafxjcrbrowser.Main"
    
Wait for "Listening for transport dt_socket at address: 1044" and then connect the debugger to port 1044.

### Sling server backend
PoC application reads and writes it's data into an external server using http. On the serverside, the JCR api is exposed by "Apache Sling Simple WebDAV Servlet" in OSGi-config.

Whether that's a sane thing to do is open for discussion. (It would be possible to use sling's [Default GET](https://sling.apache.org/documentation/bundles/rendering-content-default-get-servlets.html) and [Default POST](https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html) servlets for the basic CRUD operations on the repository.)

But using the familiar jcr-api also on the client gives the same access/view to the content as in the serverside development.
It's fun putting a clean api behind a Spring Bean, mocking it with same [UnitTest mock's](https://sling.apache.org/documentation/development/jcr-mock.html)

**Without a server** - think of JCR as just one example of any java api (SQL/JDBC driver, web-service-client, data from local file-system), that might come with additional dependencies, possibly not JPMS/jlink modularized yet.

## Distribute win/mac/linux builds with github actions 

See [ReadMe](./deploy/ReadMe.md) in the deploy folder. It contains maven setups to build deployable artifacts.
Native installers or binaries can only be generated for the platform the build is running on. 
You can only generate windows installers on windows, linux on linux, mac on mac. 
That's when github action come handy as you can run them on each platform and take the resulting artivact .
See .github/workflows/

When you're logged into GitHub, you can go to 
 >Actions > Workflow_for_your_machine > last run > Artifacts
 
and download the binary.

[![Windows MSI/EXE installers, executable jars](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/win_installer.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/win_installer.yml)
[![Windows EXE](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/win_native.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/win_native.yml)
[![Mac PKG/DMG installers, executable jars](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/mac_installer.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/mac_installer.yml)
[![MacOS BIN](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/mac_native.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/mac_native.yml)
[![Linux DEB/RPM installers, executable jars](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/linux_installer.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/linux_installer.yml)
[![Linux BIN](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/linux_native.yml/badge.svg)](https://github.com/orx0815/openJfx-jcrbrowser/actions/workflows/linux_native.yml)


### Enterprisey Springboot

This javaFX application itself is useless, it's just a PoC around the tooling - Rapid-Application-Development robot gui testing, springboot and deployment.










### Used frameworks, libraries 
- Obviously [OpenJfx](https://openjfx.io/). JavaFX, but now open source.
- Visual Rapid-Application-Development with [Scene Builder](https://gluonhq.com/products/scene-builder/), a WYSIWYG editor to generate [FXML](https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html) from.
- FXML includes for separation of java controllers. Without it, the main controller develops quickly into a god-class. This in turn leads to the need for some Dependency-Injection, as it's otherwise quite cumbersome to pass around those controller-instances so they can reach each other.
- Springboot. This is a bit like 'taking a sledgehammer to crack a nut' to solve the above. There are more lightweight DI frameworks able do this, google-guice or [afterburner.fx](https://github.com/AdamBien/afterburner.fx). 
On the other hand it comes with some useful things like easy jar generation, lots of developers have experience with it and can be thrown at it to take care about business logic and to provide beans to be used inside javaFX controllers.

- junit5, robot api for gui and integration tests, sling jcr-mock
  
 
 
### The Jcr browser application itself

Think of [JCR](https://dzone.com/articles/java-content-repository-best) as an example for ANY 3rd party java dependency, that's not JPMS / Jigsaw modularized.

I started playing with this when javaFX was still part of the oracle jvm. The idea was to use a JcrNode as data-model for a TreeView and then do some CRUD operations on the content.
There is no need for a Java-Content-Repository to run the application, it will show an 'DisplayShelf' from some old Netbeans example projects just to show that javaFX is more then boring backoffice applications. (Even though this is basically the usecase here) 

If you want to connect to a Jcr and don't have an 'Adobe Experience Manager' at hand you can download the [sling-starter-standalone.jar](https://sling.apache.org/downloads.cgi#sling-application), run the jar with java11 and press the Login button in the browser application with the prefilled values.

 It uses a servlet from sling-jcr-davex bundle to access remote repository the same way crx.de does in AEM.
 
***

## Build and run

#### Requirements

java11+ 
maven 3.6.x

Make sure `JAVA_HOME` is properly set to the Java installation directory.


**Build the project**

    mvn install
    

**Run the project**
    
    mvn exec:java


or from spring-boot-maven-plugin:
     
    mvn spring-boot:run
    
    
or with the [javafx-maven-plugin](https://github.com/openjfx/javafx-maven-plugin)

    mvn javafx:run
 
Note that 'mvn javafx:jlink' won't work, as the application is not modularized due to non-modular dependencies.

**Debug** (and prevent screen-grab from ide) [1]

    mvn exec:exec -Dexec.executable=$JAVA_HOME"/bin/java" -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044 -Dglass.disableGrab=true org.motorbrot.javafxjcrbrowser.Main"
   

   




***
[1] https://netbeans.org/bugzilla/show_bug.cgi?id=253594
