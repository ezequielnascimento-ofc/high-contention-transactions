# API errors

Every error is returned as [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) with `Content-Type: application/problem+json`:

```json
{
  "type": "https://github.com/ezequielnascimento-ofc/high-contention-transactions/blob/main/docs/errors.md#insufficient-stock",
  "title": "Insufficient stock",
  "status": 409,
  "detail": "Insufficient stock for inventory 0b6f…: available=0, requested=1",
  "instance": "/api/v1/inventories/0b6f…/decrease"
}
```

`type` identifies the problem and links to its section below. Clients should branch on `type` and `status`, never on `detail`, which is for humans.

| Type | Status | When |
|---|---|---|
| [validation-failed](#validation-failed) | 400 | Bean Validation rejected one or more fields |
| [malformed-request](#malformed-request) | 400 | The body is missing or is not valid JSON |
| [invalid-parameter](#invalid-parameter) | 400 | A path or query parameter has the wrong type |
| [invalid-request](#invalid-request) | 400 | The domain rejected the values (for example, a non-positive quantity) |
| [resource-not-found](#resource-not-found) | 404 | The product, the inventory or the route does not exist |
| [insufficient-stock](#insufficient-stock) | 409 | A decrease asked for more than the available stock |
| [concurrency-conflict](#concurrency-conflict) | 409 | A retrying strategy gave up after repeated concurrent updates |
| [inventory-already-exists](#inventory-already-exists) | 409 | The product already has an inventory |
| [unknown-product](#unknown-product) | 422 | An inventory refers to a product that does not exist |
| [internal-error](#internal-error) | 500 | Anything else; the detail never exposes internals |

## validation-failed
The body was valid JSON, but at least one field broke a constraint. The `errors` property lists each one:

```json
"errors": [{ "field": "quantity", "message": "must be greater than 0" }]
```

## malformed-request
The body could not be read: empty, not JSON, or a value of the wrong JSON type.

## invalid-parameter
A path or query parameter could not be converted, for example an `id` that is not a UUID.

## invalid-request
The values were well formed, but the domain rejected them.

## resource-not-found
The resource in the URL does not exist, or there is no endpoint for that method and path.

## insufficient-stock
The decrease was refused and the stock was left unchanged. Retrying the same request will fail again unless the stock grows.

## concurrency-conflict
Only with the `optimistic` or `serializable` strategy. The decrease lost to concurrent writers on every one of `inventory.concurrency.max-attempts` tries and gave up; the stock was left unchanged. Unlike `insufficient-stock`, retrying later can succeed.

## inventory-already-exists
Each product has at most one inventory, enforced by a unique constraint. When two requests create it at the same time, exactly one succeeds and the other receives this problem.

## unknown-product
The request is well formed, but `productId` refers to a product that does not exist (foreign key violation). It is 422 rather than 404 because the URL itself was found.

## internal-error
An unexpected failure. It is logged on the server with the stack trace; the response carries no internal detail.
