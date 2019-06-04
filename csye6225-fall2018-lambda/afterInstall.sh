#!/bin/bash
bucket=$s3_bucket
aws lambda update-function-code --function-name LambdaApp --region us-east-1 --s3-bucket $bucket --s3-key lambdaapp.zip
