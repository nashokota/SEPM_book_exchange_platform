# API Notes

## Overview

The project exposes a REST API for public book browsing, admin category management, seller book management, and buyer/seller request workflows. The main REST controllers are:

- `BookRestController`
- `CategoryRestController`
- `ExchangeRequestRestController`

The API uses JSON request/response bodies and a global REST exception handler for consistent error responses.

## Base Paths

- Public books: `/api/books`
- Seller books: `/api/seller/books`
- Categories: `/api/categories`
- Buyer requests: `/api/requests`
- Seller request actions: `/api/seller/requests`

## Authentication and Access Rules

- Public:
  - `GET /api/books`
  - `GET /api/books/{id}`
  - `GET /api/categories`
  - `GET /api/categories/{id}`
- Authenticated user:
  - `POST /api/requests`
  - `GET /api/requests/my`
- Seller only:
  - `GET /api/seller/books`
  - `POST /api/books`
  - `PUT /api/books/{id}`
  - `DELETE /api/books/{id}`
  - `GET /api/seller/requests`
  - `PATCH /api/seller/requests/{id}/approve`
  - `PATCH /api/seller/requests/{id}/reject`
- Admin only:
  - `POST /api/categories`
  - `PUT /api/categories/{id}`
  - `DELETE /api/categories/{id}`

CSRF is ignored for `/api/**`, which makes API testing easier while the web interface continues using Spring Security form login.

## Books API

### `GET /api/books`
Returns paginated public book listings.

Query parameters:
- `q` — search by title or author
- `categoryId` — category filter
- `page` — page number
- `size` — page size

Response:
- `200 OK`
- body type: `PageResponse<BookResponse>`

### `GET /api/books/{id}`
Returns a single public available book.

Response:
- `200 OK`
- body type: `BookResponse`

### `GET /api/seller/books`
Returns the logged-in seller's own listings.

Response:
- `200 OK`
- body type: `List<BookResponse>`

### `POST /api/books`
Creates a new seller listing.

Response:
- `201 Created`
- body type: `BookResponse`

### `PUT /api/books/{id}`
Updates a seller-owned listing.

Response:
- `200 OK`
- body type: `BookResponse`

### `DELETE /api/books/{id}`
Deletes a seller-owned listing.

Response:
- `204 No Content`

## Categories API

### `GET /api/categories`
Returns all categories.

Response:
- `200 OK`
- body type: `List<CategoryResponse>`

### `GET /api/categories/{id}`
Returns one category.

Response:
- `200 OK`
- body type: `CategoryResponse`

### `POST /api/categories`
Creates a new category.

Response:
- `201 Created`
- body type: `CategoryResponse`

### `PUT /api/categories/{id}`
Updates a category.

Response:
- `200 OK`
- body type: `CategoryResponse`

### `DELETE /api/categories/{id}`
Deletes a category.

Response:
- `204 No Content`

## Requests API

### `POST /api/requests`
Creates a buyer request for a book.

Request type can be:
- `BUY`
- `EXCHANGE`

Response:
- `201 Created`
- body type: `ExchangeRequestResponse`

### `GET /api/requests/my`
Returns the logged-in buyer's own requests.

Response:
- `200 OK`
- body type: `List<ExchangeRequestResponse>`

### `GET /api/seller/requests`
Returns incoming requests for the logged-in seller.

Response:
- `200 OK`
- body type: `List<ExchangeRequestResponse>`

### `PATCH /api/seller/requests/{id}/approve`
Approves a pending request.

Response:
- `200 OK`
- body type: `ExchangeRequestResponse`

### `PATCH /api/seller/requests/{id}/reject`
Rejects a pending request.

Response:
- `200 OK`
- body type: `ExchangeRequestResponse`

## Main DTOs

### `BookResponse`
Contains:
- book identity and metadata
- category id and category name
- seller id and seller name
- listing mode, condition, availability, price
- description and timestamps

### `CategoryResponse`
Contains:
- id
- name
- description
- timestamps

### `ExchangeRequestResponse`
Contains:
- request id
- book id and title
- buyer id and buyer name
- seller id and seller name
- request type and status
- buyer message and offered book information
- seller remarks
- timestamps

### `PageResponse<T>`
Used for paginated book results.
Contains:
- `content`
- `page`
- `size`
- `totalElements`
- `totalPages`
- `first`
- `last`

## Example Requests

### Public books
```http
GET /api/books?q=clean&categoryId=1&page=0&size=6
```

### Create category
```http
POST /api/categories
Content-Type: application/json

{
  "name": "History",
  "description": "History related books"
}
```

### Create seller book
```http
POST /api/books
Content-Type: application/json

{
  "title": "Distributed Systems",
  "author": "Tanenbaum",
  "isbn": "123456789",
  "condition": "GOOD",
  "listingMode": "SELL_ONLY",
  "availabilityStatus": "AVAILABLE",
  "price": 500.00,
  "exchangePreference": null,
  "imageUrl": null,
  "description": "Good condition",
  "categoryId": 1
}
```

### Create buyer request
```http
POST /api/requests
Content-Type: application/json

{
  "bookId": 1,
  "requestType": "BUY",
  "message": "I want to buy this book."
}
```

### Approve seller request
```http
PATCH /api/seller/requests/5/approve
Content-Type: application/json

{
  "remarks": "Approved by seller."
}
```

## Error Handling

REST errors are returned through a global exception handler using `ApiErrorResponse`.

Error response fields:
- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `validationErrors`

Main mappings:
- `404 Not Found`
  - resource not found
- `409 Conflict`
  - duplicate category name
  - duplicate email
- `400 Bad Request`
  - invalid listing rules
  - request not allowed
  - invalid seller application state
  - validation failure
- `403 Forbidden`
  - access denied
- `500 Internal Server Error`
  - unexpected server error

## Notes

- Public API only exposes available books.
- Seller write endpoints operate on seller-owned data only.
- Approving a request marks the related book as unavailable.
- Password handling and web login are managed by Spring Security form authentication, not by a separate JWT API.
