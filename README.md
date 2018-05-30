# uniprot-disease
REST API for UniProtKB supporting data diseases see https://www.uniprot.org/diseases/

UniProtKB (Universal Protein Knowledge Base) is a collection of functional information on proteins. Proteins can be involved in the human diseases. Diseases will have their definitions, acronyms, alternative names and other properties/information. User (website/machine) can use this REST API to search or get all information about human diseases referred in UniProtKB.

Standalone application, you need java8 and maven to startup.

## Technologies
* Java 8
* Spring boot 2.0.1
* Embedded mongodb 2.0.3
* Maven 3.5.2
* Junit 5.01
* Mockito 2.18.3
* Jackson 2.9.5
* Assertj 3.9.1
* Docker 17.12
* Apache commons-csv 1.5

## Getting started
1. Download diseases data file from ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/humdisease.txt on local file system
1. Download/clone the code from github `git clone https://github.com/ebi-uniprot/uniprot-disease.git`
1. Open file *uniprot-disease/src/main/resources/application.properties* and change the value **spring.mongodb.embedded.storage.database-dir=** with the path where you want to create your mongo database 
1. Go to uniprot-disease directory from terminal/command prompt
1. run command `mvn package`
1. For First time only (import data into database from txt file) run command `java -jar target/uniprot-disease-0.0.1-SNAPSHOT.jar humdisease.txt`
  1. It will delete existing database first and then start to import data
  1. **Note:** I have downloaded *humdisease.txt* file in same directory. You have to give the complete path of file if it is not in same directory
  1. Server will remain started and entertain requests
  1. If you want to stop server and just want to import data use `java -jar target/uniprot-disease-0.0.1-SNAPSHOT.jar humdisease.txt --stopserver`
1. To start server second time (without import) use `java -jar target/uniprot-disease-0.0.1-SNAPSHOT.jar`

## Endpoints
Endpoint | Description
-------- | -----------
http://localhost:8080/accession/DI-04904 | Return the single disease exact match on accession=DI-04904 (case-sensitive)
http://localhost:8080/acronym/ACHP | Return the single disease exact match on acronym=ACHP (case-sensitive)
http://localhost:8080/identifier/Acatalasemia | Return single disease exact match on identifier=Acatalasemia (case-sensitive)
http://localhost:8080/identifier/all/1A | Returns the collection of all the matching diseases which contains the "1A" after ignoring case in identifiers.
http://localhost:8080/search/lyase 3KTD | Returns the unique collection of all the matching diseases which contains the "lyase" or "3KTD" after ignoring case in identifier or accession or acronym or synonyms (alternative names) or definition.

## Getting started with Docker
You can build image [locally](docker) as well as use docker hub to pull image.

to pull from docker hub and start container in backgroud
```
docker run -d --name disease -p8080:8080 impo/disease_api:2018_05
```
Need any help regarding docker commands see [docker](https://github.com/rizwan-ishtiaq/wiki/blob/master/commands/docker.txt) for quick reference.

## Code Explanation
1. Package name convention, using the plural for packages with homogeneous contents and the singular for packages with heterogeneous contents.
1. Main Class uk.ac.ebi.uniprot.uniprotdisease.UniprotDiseaseApplication
1. Single Controller for API uk.ac.ebi.uniprot.uniprotdisease.controller.DefaultController
1. Controller interacting with service and service interacting with repository
1. Import/Parse files logic is in uk.ac.ebi.uniprot.uniprotdisease.import_data package
1. Dataset while never (too slow) grow, therefore making following to make application fast
   1. While importing loading all lines from file to memory
   1. Create / persist list of all (5000) object into database at once

## License
This software is licensed under the Apache 2 license, quoted below.

Copyright (c) 2018, ebi-uniprot

Licensed under the [Apache License, Version 2.0.](LICENSE) You may not
use this project except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
