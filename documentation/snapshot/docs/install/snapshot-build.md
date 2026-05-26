## Access to the latest `master` snapshot

Whenever a commit is added to the `master` branch a snapshot build is automatically uploaded to [Sonatype's snapshots repository](https://central.sonatype.com/repository/maven-snapshots//com/ktlint/ktlint/).
If you are eager to try upcoming changes (that might or might not be included in the next stable release) you can do so by changing the version of ktlint to `<latest-version>-SNAPSHOT`, and adding the Sonatype snapshot repository location.

!!! important
    Snapshots are kept until 90 days after being published. Due to some bug at sonatype, it is currently not possible to browse the snapshot directories. But, building against a snapshot version is possible.

### Maven

```xml
...
<repository>
    <id>sonatype-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
    <releases>
        <enabled>false</enabled>
    </releases>
</repository>
...
```

### Gradle

```groovy
repositories {
  maven {
    url "https://central.sonatype.com/repository/maven-snapshots/"
  }
}
```

### Kotlin development version snapshot

Additionally, the project publishes snapshots build against the latest kotlin development version. To use them, change the version of ktlint to `<latest-version>-kotlin-dev-SNAPSHOT`.
