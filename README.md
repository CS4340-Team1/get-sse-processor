# NiFi GetSSE Processor

### About this project

NiFi GetSSE Processor project is Software Maintenance CS 4340 Team One's Semester Project. This project was selected for
a modernization of Apache NiFi to incorporate an SSE Processor into NiFi based on an issue submitted to NiFi's [Jira
board](https://issues.apache.org/jira/browse/NIFI-12544) requesting a new processor that can handle ingest from more
modern data sources, specifically a Server Sent Event sink.

The goal for this project is to develop a basic SSE processor, develop a basic design map, and provide sufficient testing.

If time permits, the inclusion of additional features 
like OAuth2, SSL Certificates, and Server Time Out events.


-------------------------------------

## Getting Started

```
git clone https://github.com/CS4340-Team1/get-sse-processor.git
```

--------------------------------------

## Using NiFi

This project uses Apache NiFi version 2.21.0 which is an older version of NiFi.

Download this version [here](https://archive.apache.org/dist/nifi/1.21.0/nifi-1.21.0-bin.zip). 

* Unzip to C: directory

* Follow Apache NiFi [quick start guide](https://nifi.apache.org/documentation/v1/)

--------------------------------------

## Contributing

If using command line to contribute, use the following commands. If using IntelliJ, you can manage your remotes form the 
VCS menu.

``` 
git add .

git commit -m <my-commit-message> 

git remote add origin https://github.com/CS4340-Team1/get-sse-processor.git

git push -u origin master
```
#### **Remember to commit and push your repo after work is complete, and pull / update your local repo before starting work!**

--------------------------------------

## Licence

Copyright 2024 Chris Burrows-Riggan, Andrew Barovic, Daniel Atkinson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.