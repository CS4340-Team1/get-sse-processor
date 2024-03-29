# GetSSE Processor

-----


# Getting Started

```bash
git clone https://github.com/CS4340-Team1/get-sse-processor.git
```

### Maven and Java versions

- Java version is required to be 21.0.0 or higher
- Maven is required to build project

To verify you Java version and Maven installation run:

 ```bash
 mvn --version && java --version
 ```

### NiFi

This Project uses NiFi 2.0.0-M2

- Download [NiFi](https://dlcdn.apache.org/nifi/2.0.0-M2/nifi-2.0.0-M2-bin.zip)
- Follow Nifi's [quickstart guide](https://nifi.apache.org/docs/nifi-docs/html/getting-started.html)

### Project Structure

```.
nifi-GetSSE-processor
├── nifi-GetSSE-processor-nar
│   └── pom.xml
├── nifi-GetSSE-processor-processors
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com
│       │   │       └── it
│       │   │           └── processors
│       │   │               └── getsse
│       │   │                   └── GetSSE.java
│       │   └── resources
│       │       └── META-INF
│       │           └── services
│       │               └── org.apache.nifi.processor.Processor
│       └── test
│           └── java
│               └── com
│                   └── it
│                       └── processors
│                           └── getsse
│                               └── GetSSETest.java
└── pom.xml
```

-----

# Build

From the nifi-GetSSE-processor directory, run:

```bash
mvn clean install
```
This will generate the "target" directory for the build, you will need the generated `nifi-GetSSE-processor-nar-1.0.nar`
file in the next step.

```properties
    target
    ├── classes
    │   └── META-INF
    │       └──bundled-dependencies
    ├── maven-archiver
    ├── maven-shared-archive-resources
    ├── META-INF
    │   └── docs
    ├── nifi-GetSSE-processor-nar-1.0.nar <- You will need this file
    └── test-classes




```

-----

# Install

copy the `nifi-GetSSE-processor-nar-1.0.nar` and paste into your installed NiFi package's `lib` directory.

```
nifi
├── bin <- paste your nar file here
├── conf
├── content_repository
├── database_repository
├── docs
.
.
.
└── work

```

-----

# Run

-----

# Test

-----

# Contributing

-----

- Chris Burrows-Riggan
- Andrew Barovic
- Daniel Atkinson

If using command line to contribute, use the following commands. If using IntelliJ, you can manage your remotes form the VCS menu.

```bash
git add .

git commit -m "Commiting to NiFi GetSEE Project!"

git remote add origin https://github.com/CS4340-Team1/get-sse-processor.git

git push -u origin master
```

**Remember to commit and push your repo after work is complete, and pull / update your local repo before starting work!**

# Licence

-----

Copyright 2024 Chris Burrows-Riggan, Andrew Barovic, Daniel Atkinson

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the 
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
specific language governing permissions and limitations under the License.