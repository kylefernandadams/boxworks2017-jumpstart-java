BoxWorks 2017 - JumpStart Java Lambda Example
=============================================

## Instructions
1. From your Box Developer console, download a new configuration json file and add it to the src/main/resources directory.
2. Package the shaded jar file with Maven.
```
mvn clean package
```
3. Upload the jar file to your AWS Lambda function.
4. Create a new webhook configured to call your AWS API Gateway + Lamnda function.
```
curl https://api.box.com/2.0/webhooks \
-H "Authorization: Bearer access_token_here" \
-H "Content-Type: application/json" -X POST \
-d '{"target": {"id": "<TARGET_ID>", "type": "folder"}, "address": "<AWS_API_GATEWAY_URL>", "triggers": ["FILE.UPLOADED"]}'
```
5. Upload a file to your target folder and enjoy.