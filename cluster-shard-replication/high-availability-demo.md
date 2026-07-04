- Ensure that 3 nodes cluster should be up and running

```
# Create index
PUT /products

# Get the information about shards
GET /_cat/shards/products?v

# Store some documents
POST /products/_doc
{
    "name" : "Product 1"
}

# Delete index
DELETE /products
```

- Send CURL requests to search for documents

```curl
CURL http://localhost:[PORT]/products/_search
```

- Bring the primary shard node down
- Send the CURL request to search for documents