#!/usr/bin/env bash

# curl command to upload the csv that's used in the unit-test
# to the same content node location, but on a real jcr via SlingPostServlet.
# You can see, that the whole thing is possible without jcr-api, just SlingDefault GET/POST servlets

curl -u admin:admin -F"sling:resourceType=javafxjcrbrowser:sample" \
-Ftitle="Test Csv binary" \
-Ftext="To be shown in javaFx table" \
-F csvBlob=\<./src/test/resources/testdata/exampleFxTable.csv -F csvBlob@TypeHint=Binary \
http://localhost:8080/content/javafxjcrbrowser/sample/csv 
