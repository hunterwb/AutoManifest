# AutoManifest

##### Maven usage:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.hunterwb</groupId>
                        <artifactId>automanifest</artifactId>
                        <version>0.1.0</version>
                    </path>
                </annotationProcessorPaths>
                <showWarnings>true</showWarnings>
                <compilerArgs>
                    <arg>-Aautomanifest=Automatic-Module-Name,Main-Class,Custom-Attribute:custom_value</arg>
                </compilerArgs>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.2.2</version>
            <configuration>
                <archive>
                    <manifestFile>${project.build.outputDirectory}/META-INF/MANIFEST.MF</manifestFile>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

##### Gradle usage:

```groovy
dependencies {
    annotationProcessor 'com.hunterwb:automanifest:0.1.0'
}

compileJava {
    options.compilerArgs.add('-Aautomanifest=Automatic-Module-Name,Main-Class,Custom-Attribute:custom_value')
}

jar {
    manifest {
        from compileJava.destinationDirectory.file('META-INF/MANIFEST.MF')
    }
}
```