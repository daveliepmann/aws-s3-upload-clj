# aws-s3-upload-clj

A minimal example of batch uploading files to Amazon Web Service's
Simple Storage Service (AWS S3) using Clojure.

## Why?

Amazon's inability to receive batch file uploads, left unchecked, will
inexorably lead to endless re-implementation of the same boilerplate
code to manually send a batch of files one by one. Plus,
authenticating POSTs to AWS can be tricky.

## Usage

Clone the repo to your local machine. Set the aws-bucket in `demo.clj`
and create environment variables `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`
for your Amazon credentials. Then `lein ring server`, select some
files, and click the upload button. Boom--you just saved yourself
hours of double-checking encodings and writing workaround code.

## How

We present the user with a file selection dialog. Included on the page
is a hidden form with a set of authentication fields, including a
base-64 encoded policy document and a HMAC-SHA1 signature for that
policy document, both created on the server. The policy document
matches the hidden authentication form fields. See
`src/aws_s3_upload_clj/demo.clj`.

A listener waits for file selection events, adding any selected files
to the `upload-queue` atom. When the user initiates the upload, each
file in the `upload-queue` is individually wrapped up in an POST along
with the authentication field data contained in the hidden form. This
POST is sent to S3 with a core.async channel waiting on its
completion. See `src-cljs/upload.cljs`.

## License

Copyright © 2014 David Liepmann

Released under the The MIT License (MIT). See *LICENSE.txt*.
