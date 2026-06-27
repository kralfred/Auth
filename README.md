A webapp that users can login to and view or modify reservations based on their authority.


Frontend - Javascript
Backend - Java
Database - Postgre (runnable with Docker)

Sensetive information is kept in a secret.env file, which is in a directory above both front-end and back-end, so it is necesary to set that up in order to run localy :) the file
should look something like this:

DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_NAME=db_name
DB_PORT=5432

The docker compose file will not register the file, unless it's in the same folder, so you need to use this command before running the app, so that the path gets set correctly:

docker compose --env-file ../secret.env up -d


Backend: 

You can check the APIs with swagger

```mermaid
flowchart TB
    subgraph group2["Group 1"]
        user4["User 4"]
        user5["User 5"]
        user6["User 6"]
        user7["User 7"]
    end

    user4:::group2Style
    user5:::group2Style
    user6:::group2Style
    user7:::group2Style
    classDef group1Style stroke:#818cf8,fill:#eef2ff
    classDef group2Style stroke:#2dd4bf,fill:#f0fdfa

```


```mermaid
%%{init: {"theme":"dark","flowchart":{"curve":"linear"}}}%%

flowchart LR

customer["<b>customer</b><hr/>
UUID id<br/>
VARCHAR(50) first_name<br/>
VARCHAR(50) last_name<br/>
VARCHAR(100) email"]

product["<b>product</b><hr/>
UUID id<br/>
VARCHAR(100) name<br/>
DECIMAL price<br/>
INTEGER stock_quantity"]

customer_order["<b>customer_order</b><hr/>
UUID id<br/>
UUID customer_id<br/>
TIMESTAMP order_date<br/>
DECIMAL total_amount"]

order_item["<b>order_item</b><hr/>
UUID id<br/>
UUID order_id<br/>
UUID product_id<br/>
INTEGER quantity<br/>
DECIMAL unit_price"]

product_review["<b>product_review</b><hr/>
UUID id<br/>
UUID product_id<br/>
UUID customer_id<br/>
INTEGER rating<br/>
TEXT review_text"]

customer -->|"1 : many"| customer_order
customer_order -->|"1 : many"| order_item
product -->|"1 : many"| order_item
customer -->|"1 : many"| product_review
product -->|"1 : many"| product_review
```
```mermaid
---
config:
  layout: elk
  theme: dark
---
erDiagram
	direction TB
	customer_order {
		UUID id  ""  
		UUID customer_id  ""  
		TIMESTAMP order_date  ""  
		DECIMAL total_amount  ""  
	}

	customer {
		UUID id  ""  
		VARCHAR(50) first_name  ""  
		VARCHAR(50) last_name  ""  
		VARCHAR(100) email  ""  
	}

	order_item {
		UUID id  ""  
		UUID order_id  ""  
		UUID product_id  ""  
		INTEGER quantity  ""  
		DECIMAL unit_price  ""  
	}

	product {
		UUID id  ""  
		VARCHAR(100) name  ""  
		DECIMAL price  ""  
		INTEGER stock_quantity  ""  
	}

	product_review {
		UUID id  ""  
		UUID product_id  ""  
		UUID customer_id  ""  
		INTEGER rating  ""  
		TEXT review_text  ""  
	}

	customer_order}o--||customer:"references"
	order_item}o--||customer_order:"references"
	order_item}o--||product:"references"
	product_review}o--||product:"references"
	product_review}o--||customer:"references"
```

