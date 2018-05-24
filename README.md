# uniprot-disease
REST API for UniProtKB supporting data diseases see https://www.uniprot.org/diseases/

Standalone application, you need java8 and maven to startup.

## Technologies
* Java 8
* Spring boot 2.0.1
* Embedded mongodb 2.0.3
* Maven 3.5.2
* Junit 5.01
* Mockito 2.18.3
* jackson 2.9.5
* assertj 3.9.1
* docker 17.12

## Endpoints
Endpoint | Description
-------- | -----------
http://localhost:8080/accession/DI-04904 | Return the single disease exact match on accession=DI-04904 (case-sensitive)
http://localhost:8080/acronym/ACHP | Return the single disease exact match on acronym=ACHP (case-sensitive)
http://localhost:8080/identifier/Acatalasemia | Return single disease exact match on identifier=Acatalasemia (case-sensitive)
http://localhost:8080/identifier/all/1A | Returns the collection of all the matching diseases which contains the "1A" after ignoring case in identifiers.
http://localhost:8080/search/lyase 3KTD | Returns the unique collection of all the matching diseases which contains the "lyase" or "3KTD" after ignoring case in identifier or accession or acronym or synonyms (alternative names) or definition.

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
