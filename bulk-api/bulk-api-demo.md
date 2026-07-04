## Bulk API

### Demo 1
```
# delete index
DELETE /my-index

# create an index
PUT /my-index

# bulk insert
POST /my-index/_bulk
{ "create" : {} }
{ "name" : "item1" }
{ "create" : {} }
{ "name" : "item2" }
{ "create" : {} }
{ "name" : "item3" }

# query all
GET /my-index/_search

# bulk insert with id
POST /my-index/_bulk
{ "create" : { "_id": 1 } }
{ "name" : "item1" }
{ "create" : { "_id": 2 } }
{ "name" : "item2" }
{ "create" : { "_id": 3 } }
{ "name" : "item3" }

# query all
GET /my-index/_search

# bulk insert / update / delete
POST /my-index/_bulk
{ "create" : { "_id": 1 } }
{ "name" : "item1" }
{ "create" : { "_id": 2 } }
{ "name" : "item2" }
{ "create" : { "_id": 3 } }
{ "name" : "item3" }
{ "update" : { "_id": 2 } }
{ "doc": { "name" : "item2-updated" }}
{ "create" : { "_id": 4 } }
{ "name" : "item4" }
{ "delete" : { "_id": 1 } }

# query all
GET /my-index/_search
```
