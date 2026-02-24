#!/bin/bash
echo "Criando bucket S3 no LocalStack..."
awslocal s3 mb s3://digitalbank-receipts
awslocal s3api put-bucket-acl --bucket digitalbank-receipts --acl public-read
echo "Bucket 'digitalbank-receipts' criado com sucesso!"
