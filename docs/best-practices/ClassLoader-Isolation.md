# Best Practices - GemFire ClassLoader Loader Isolation 


GemFire members are started with classloader loader isolation enabled by default. The details of classloader isolation can found here: https://docs.vmware.com/en/VMware-GemFire/10.0/gf/configuring-cluster_config-classloader_isolation.html


## Best Practices

- Use Java build tools such as [Gradle’s shadowJar](https://imperceptiblethoughts.com/shadow/) or [Maven Shade](https://maven.apache.org/plugins/maven-shade-plugin/) to create a Java Jar with the needed dependencies included
- Use compile only option to exclude core GemFire classes (ex: org.apache.geode.* or group: 'com.vmware.gemfire')
- Minimize the dependencies to the essentials needed for needed components
- Verify included dependencies created in the Jar(here is an example to verify the Jar dependencies using gradle: gradle dependencies --configuration runtimeClasspath)
[Add any needed manifest attribute](https://imperceptiblethoughts.com/shadow/configuration/#configuring-the-jar-manifest) required by included dependencies such as multi-java release attribute “Multi-Release” : true (see below)


Example Compile Only Gradle

```groovy
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath "gradle.plugin.com.github.johnrengelman:shadow:7.1.2"
    }
}

plugins {
    id 'java-library'
    id 'com.github.johnrengelman.shadow' version '7.1.2'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

apply plugin: 'signing'
apply plugin: 'maven-publish'

version = '2.0.0-SNAPSHOT'

allprojects {
    tasks.withType(Javadoc) {
        options.addStringOption('Xdoclint:none', '-quiet')
    }
}

group = 'io.pivotal.services.dataTx'
archivesBaseName = 'gemfire-extensions-core'

ext {
    gemFireVersion = '10.0.0'
}

signing {
    sign(publishing.publications)
}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url "https://packages.broadcom.com/artifactory/gemfire/"
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
        credentials {
            username repoUsername
            password repoPassword
        }
    }
}
dependencies {
    compileOnly 'org.apache.logging.log4j:log4j-api:2.20.0'
    compileOnly group: 'com.vmware.gemfire', name: 'gemfire-core', version: gemFireVersion
    compileOnly 'com.vmware.gemfire:gemfire-search:1.0.0'
    compileOnly 'com.fasterxml.jackson.core:jackson-databind:2.14.1'
    compileOnly 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1'
    compileOnly group: 'com.vmware.gemfire', name: 'gemfire-cq', version: gemFireVersion
    compileOnly group: 'com.vmware.gemfire', name: 'gemfire-wan', version: gemFireVersion
    compileOnly 'org.slf4j:slf4j-api:1.7.36'
    implementation("com.zaxxer:HikariCP:4.0.3")
            {
                exclude group: "org.slf4j"
            }
    implementation 'org.postgresql:postgresql:42.5.1'
    implementation("com.github.nyla-solutions:nyla.solutions.core:1.5.1") {
        exclude group: "org.junit.jupiter"
    }

    testImplementation 'org.apache.logging.log4j:log4j-api:2.20.0'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind:2.15.1'

    testImplementation group: 'com.vmware.gemfire', name: 'gemfire-core', version: gemFireVersion
    testImplementation 'com.vmware.gemfire:gemfire-search:1.0.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.0'
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation group: 'org.mockito', name: 'mockito-junit-jupiter', version: '4.6.1'
    testImplementation group: 'org.mockito', name: 'mockito-core', version: '4.6.1'
    testImplementation group: 'org.junit.jupiter', name: 'junit-jupiter', version: '5.9.0'
    testImplementation group: 'org.junit.jupiter', name: 'junit-jupiter-engine', version: '5.9.0'
    testImplementation group: 'com.h2database', name: 'h2', version: '2.1.214'
    testImplementation group: 'org.postgresql', name: 'postgresql', version: '42.2.9'
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

}

jar {
    manifest {
        attributes 'Multi-Release' : 'true'
    }
}

shadowJar {
}

publishing {
    publications {
        maven(MavenPublication) {
            pom {
                name = 'gemfire-extensions-core'
                groupId = group
                artifactId = 'gemfire-extensions-core'
                description = 'This Java API provides support for GemFire'
                packaging = 'jar'
                url = 'https://github.com/ggreen/gemfire-extensions'
                licenses {
                    license {
                        url = 'https://github.com/ggreen/gemfire-extensions/blob/main/LICENSE'
                    }
                }
                developers {
                    developer {
                        id = 'ggreen'
                        name = 'Gregory Green'
                        email = 'gregoryg@vmware.com'
                    }
                }
                scm {
                    connection = 'scm:git:https://github.com/ggreen/gemfire-extensions.git'
                    developerConnection = 'scm:git:https://github.com/ggreen/gemfire-extensions.git'
                    url = 'https://github.com/ggreen/gemfire-extensions.git'
                }
            }
            from components.java
        }
    }
    repositories {
        maven {
            name = "CentralMaven" //  optional target repository name
            url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            credentials {
                username = user
                password = pwd
            }
        }
    }
}
```

```groovy
shadowJar {
    manifest {
        attributes 'Multi-Release' : 'true'
    }
}
```


## Known Issues

Exclude the following dependencies: 
- org.apache.logging.log4j
- org.slf4j
- com.fasterxml.jackson

Example to exclude these dependencies

```groovy
dependencies {
    compileOnly 'org.apache.logging.log4j:log4j-api:2.20.0'
    compileOnly 'com.fasterxml.jackson.core:jackson-databind:2.14.1'
    compileOnly 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1'
    implementation("com.zaxxer:HikariCP:4.0.3")
            {
                exclude group: "org.slf4j"
            }
    //.....
}
```

This readme provides an example  JDBC Cache Loader that uses classloader isolations.

https://github.com/ggreen/gemfire-extensions/blob/main/docs/demo/local/JDBC_CacheLoader_DEMO.md

