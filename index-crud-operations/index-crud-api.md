## CRUD

- Create Index
```
PUT /books
```

### Create
- Insert Documents
```
POST /books/_doc
{
    "title": "To Kill a MockingBird",
    "author": "Harper Lee",
    "year": 1960,
    "genre": "Fiction",
    "rating": 4.9    
}

POST /books/_doc
{
  "title": "1984",
  "author": "George Orwell",
  "year": 1949,
  "genre": "Dystopian",
  "rating": 4.8
}

```
- To store docs with IDs

```
DELETE /books

PUT /books

POST /books/_doc/1
{
  "title": "To Kill a Mockingbird",
  "author": "Harper Lee",
  "year": 1960,
  "genre": "Fiction",
  "rating": 4.9
}

POST /books/_doc/2
{
  "title": "1984",
  "author": "George Orwell",
  "year": 1949,
  "genre": "Dystopian",
  "rating": 4.8
}

POST /books/_doc/3
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "year": 1925,
  "genre": "Fiction",
  "rating": 4.7
}

POST /books/_doc/4
{
  "title": "Pride and Prejudice",
  "author": "Jane Austen",
  "year": 1813,
  "genre": "Romance",
  "rating": 4.6
}
```

### Read 
- To get the doc with ID

```
GET /books/_doc/3
```

- Query All
```
GET /books/_serach
```

- Search for documents containing `...`
```
GET /books/_search?q=Lee
GET /books/_serach?q=Fiction
GET /books/_search?q=4.6
```

### Update

- To update a doc with ID.
  - POST will also work. PUT/POST will have the `upsert` behavior.
```
PUT /books/_doc/3
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "year": 1926,
  "genre": "Fiction",
  "rating": 4.7
}

GET /books/_doc/3

PUT /books/_doc/3
{
  "year": 1925
}

GET /books/_doc/3
```