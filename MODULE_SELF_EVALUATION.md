## Shared/Common
* [ ] Uses Apache 2.0 license
* [ ] Module build MUST produce a valid module descriptor
* [ ] Module descriptor MUST include interface requirements for all consumed APIs
* [ ] Third party dependencies use an Apache 2.0 compatible license
* [ ] Installation documentation is included
  * -_note: read more at https://github.com/folio-org/mod-search/blob/master/README.md_
* [ ] Personal data form is completed, accurate, and provided as `PERSONAL_DATA_DISCLOSURE.md` file
* [ ] Sensitive and environment-specific information is not checked into git repository
* [ ] Module is written in a language and framework from the [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) page
* [ ] Module only uses FOLIO interfaces already provided by previously accepted modules _e.g. a UI module cannot be accepted that relies on an interface only provided by a back end module that hasn't been accepted yet_
* [ ] Module gracefully handles the absence of third party systems or related configuration
* [ ] Sonarqube hasn't identified any security issues, major code smells or excessive (>3%) duplication
* [ ] Uses [officially supported](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) build tools
* [ ] Unit tests have 80% coverage or greater, and are based on [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies)

## Backend
* [ ] Module's repository includes a compliant Module Descriptor
  * -_note: read more at https://github.com/folio-org/okapi/blob/master/okapi-core/src/main/raml/ModuleDescriptor.json_
* [ ] Module includes executable implementations of all endpoints in the provides section of the Module Descriptor
* [ ] Environment vars are documented in the ModuleDescriptor
  * -_note: read more at [https://wiki.folio.org/pages/viewpage.action?pageId=65110683](https://wiki.folio.org/pages/viewpage.action?pageId=65110683)_
* [ ] If a module provides interfaces intended to be consumed by other FOLIO Modules, they must be defined in the Module Descriptor "provides" section
* [ ] All API endpoints are documented in RAML or OpenAPI
* [ ] All API endpoints protected with appropriate permissions as per the following guidelines and recommendations, e.g. avoid using `*.all` permissions, all necessary module permissions are assigned, etc.
  * -_note: read more at https://dev.folio.org/guidelines/naming-conventions/ and https://wiki.folio.org/display/DD/Permission+Set+Guidelines_
* [ ] Module provides reference data (if applicable), e.g. if there is a controlled vocabulary where the module requires at least one value
* [ ] If provided, integration (API) tests must be written in an [officially approved technology](https://wiki.folio.org/display/TC/Officially+Supported+Technologies)
  * -_note: while it's strongly recommended that modules implement integration tests, it's not a requirement_
  * -_note: these tests are defined in https://github.com/folio-org/folio-integration-tests_
* [ ] Data is segregated by tenant at the storage layer
* [ ] The module doesn't access data in DB schemas other than its own and public
* [ ] The module responds with a tenant's content based on x-okapi-tenant header
* [ ] Standard GET `/admin/health` endpoint returning a 200 response
  * -_note: read more at https://wiki.folio.org/display/DD/Back+End+Module+Health+Check+Protocol_
* [ ] High Availability (HA) compliant
  * Possible red flags:
    * Connection affinity / sticky sessions / etc. are used
    * Local container storage is used
    * Services are stateful
* [ ] Module only uses infrastructure / platform technologies on the [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) list.
  * _e.g. PostgreSQL, ElasticSearch, etc._

## TCR Process Improvements
[_Please include here any suggestions that you feel might improve the TCR Processes._]
