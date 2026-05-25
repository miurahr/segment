# Changelog

## 4.0.0

- Forked from the original v2.0.4.
- Build improvements
    - Migrated to a Gradle build system.
    - Minimum Java runtime to 11.
    - Integrated SpotBugs static analysis.
    - Provide CLI command scripts by Gradle application.
- Dependencies
    - Migrate JAXB/XJC 2.3 to JAXB/XJC 4.0.
    - Updated other dependencies.
- Publish changes
    - publish with Sonatype Nexus.
    - Change group id to "tokyo.northside"
- Code improvements
    - Fix build error on Java 17+
    - Add @Override annotation and final across classes.
