## Shared/Common
* [x] Uses Apache 2.0 license
* [x] Module build MUST produce a valid module descriptor
* [X] Module descriptor MUST include interface requirements for all consumed APIs
* [x] Third party dependencies use an Apache 2.0 compatible license
* [x] Installation documentation is included
* [x] Personal data form is completed, accurate, and provided as `PERSONAL_DATA_DISCLOSURE.md` file
* [x] Sensitive and environment-specific information is not checked into git repository
* [x] Module is written in a language and framework from the [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) page
* [x] Module only uses FOLIO interfaces already provided by previously accepted modules _e.g. a UI module cannot be accepted that relies on an interface only provided by a back end module that hasn't been accepted yet_
* [x] Module gracefully handles the absence of third party systems or related configuration
* [x] Sonarqube hasn't identified any security issues, major code smells or excessive (>3%) duplication
* [x] Uses [officially supported](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) build tools
* [x] Unit tests have 80% coverage or greater, and are based on [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies)

## Backend
* [x] Module's repository includes a compliant Module Descriptor
* [x] Module includes executable implementations of all endpoints in the provides section of the Module Descriptor
* [x] Environment vars are documented in the ModuleDescriptor
* [x] If a module provides interfaces intended to be consumed by other FOLIO Modules, they must be defined in the Module Descriptor "provides" section
* [x] All API endpoints are documented in RAML or OpenAPI
* [x] All API endpoints protected with appropriate permissions as per the following guidelines and recommendations, e.g. avoid using `*.all` permissions, all necessary module permissions are assigned, etc.
* [ ] Module provides reference data (if applicable), e.g. if there is a controlled vocabulary where the module requires at least one value
* [x] If provided, integration (API) tests must be written in an [officially approved technology](https://wiki.folio.org/display/TC/Officially+Supported+Technologies)
* [x] Data is segregated by tenant at the storage layer
* [x] The module doesn't access data in DB schemas other than its own and public
* [x] The module responds with a tenant's content based on x-okapi-tenant header
* [x] Standard GET `/admin/health` endpoint returning a 200 response
  * -_note: read more at https://wiki.folio.org/display/DD/Back+End+Module+Health+Check+Protocol_
* [x] High Availability (HA) compliant
  * Possible red flags:
    * Connection affinity / sticky sessions / etc. are used
    * Local container storage is used
    * Services are stateful
* [x] Module only uses infrastructure / platform technologies on the [officially approved technologies](https://wiki.folio.org/display/TC/Officially+Supported+Technologies) list.
