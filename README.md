## Order Workflow

(Optional) Validate the SAM template
`sam validate --lint`

To build run
`sam build`

To deploy SAM for test:
`sam deploy --config-env test`

To deploy SAM for prod:
`sam deploy --config-env prod`

To test a single lambda function:
`sam local invoke --event 'test.txt' SendEmailAlertFunction`