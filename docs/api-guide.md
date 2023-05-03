# Mod Consortia API Guide

## Consortia API

### Endpoints

| Method | URL                       | Permissions                         | Description                                      |
|--------|---------------------------|-------------------------------------|--------------------------------------------------|
| GET    | /consortia                | consortia.consortium.collection.get | Return list of consortia with `limit`, `offset`  |
| POST   | /consortia                | consortia.consortium.item.post      | Return saved consortia                           |
| GET    | /consortia/{consortiumId} | consortia.consortium.item.get       | Return a consortium based on `consortiumId`      |
| PUT    | /consortia/{consortiumId} | consortia.consortium.item.put       | Return updated consortia based on `consortiumId` |

## Tenants API

### Endpoints

| METHOD | URL                 | Permission                       | DESCRIPTION                                   |
|--------|---------------------|----------------------------------|-----------------------------------------------|
| GET    | /tenants            | consortia.tenants.collection.get | Return list of tenants with `limit`, `offset` |
| POST   | /tenants            | consortia.tenants.item.post      | Return saved tenant                           |
| PUT    | /tenants/{tenantId} | consortia.tenants.item.put       | Return updated tenant based on `consortiumId` |
| DELETE | /tenants/{tenantId} | consortia.tenants.item.delete    | Deleted tenant based on `consortiumId`        |

## User Tenants Associations API

### Endpoints

| METHOD | URL                                                    | Permission                            | DESCRIPTION                                                                                          |
|--------|--------------------------------------------------------|---------------------------------------|------------------------------------------------------------------------------------------------------|
| GET    | /consortia/{consortiumId}/user-tenants                 | consortia.user-tenants.collection.get | Return list of user tenant associations based on `userId`, `username`, `tenantId`, `limit`, `offset` |
| GET    | /consortia/{consortiumId}/user-tenants/{associationId} | consortia.user-tenants.item.get       | Return user-tenant with provided `associationId`                                                     |
| POST   | /consortia/{consortiumId}/user-tenants                 | consortia.user-tenants.item.post      | Return saved user-tenant association                                                                 |
| DELETE | /consortia/{consortiumId}/user-tenants                 | consortia.user-tenants.item.delete    | Deleted user-tenant association based on `userId` and `tenantId`                                     |

### Supported params options

| Option     | Example                                       | Description                                                             |
|------------|-----------------------------------------------|-------------------------------------------------------------------------|
| `userId`   | `id = "1bddbe67-fd27-436f-901e-9fa66fe4ad1d"` | Find user-tenants with ID (UUID) `1bddbe67-fd27-436f-901e-9fa66fe4ad1d` |
| `username` | `username = "testuser"`                       | Find user-tenants with username `testuser`                              |

### Other resources

This module's [API documentation](https://dev.folio.org/reference/api/#mod-consortia).
