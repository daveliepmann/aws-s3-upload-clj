# aws-s3-upload-clj

A minimal example of batch uploading files to Amazon Web Service's
Simple Storage Service (AWS S3) using Clojure.

## Usage

Clone the repo to your local machine. Set the aws-bucket in `demo.clj`
and create environment variables `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`
for your Amazon credentials. Then `lein ring server`, select some
files, and click the upload button. Boom--you just saved yourself
hours of hacking through hazy, contradictory AWS documentation.

## License

Copyright © 2014 David Liepmann

Released under the The MIT License (MIT). See *LICENSE.txt*.
