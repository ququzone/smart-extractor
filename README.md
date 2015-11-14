html smart extractor
====================

### Introduction

A micro-service for extract main content from url.

### Usage

1. Package

	```
	$ mvn clean package
	```

2. Run

	```
	$ java -jar target/smart-extractor.jar
	$ open http://localhost:8080
	```

### API

1. Extract `http://localhost:8080/extract?url={url}`

	```
	$ curl -i -X GET http://localhost:8080/extract\?url\=https://medium.com/@benjaminhardy/8-things-every-person-should-do-before-8-a-m-cc0233e15c8d
	```

### Build Docker Image

	$ mvn clean package
	$ mvn package docker:build
