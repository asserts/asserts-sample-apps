# OVERVIEW

This is a sample auction app built using SpringBoot. It is configured to report prometheus metrics. 
It can operate in two modes. **server** mode and **test-client** mode

## Server Mode
This model is enabled by setting the environment property **AUCTION_APP_ROLE** to **server**. 
In this mode the following APIs can be triggered -

### /items
List the items in auction. Each item has a required quantity

### /bids
List the bids. Each bid has an item id, price and quantity

### /useMemory
Repeatedly loads a JSON file bundled in the app and holds on to the objects created to increase memory utilisation

### /useCPU
Repeatedly loads and compresses a JSON file bundled in the app to increase CPU utilisation

### /writeBidsToS3
Writes a list of bids to an S3 bucket in the entry **all-bids**

### /readBidsFromS3
Loads the bids from the S3 bucket entry **all-bids**

### /writeBidsToDynamo
Writes a list of bids to a Dynamo table

### /readBidsFromDynamo
Scans the dynamo table and loads all the bids

### /http/<status-code>
Returns a response with status code set to given status code

## Test Client Mode
This model is enabled by setting the environment property **AUCTION_APP_ROLE** to **test-client**.
In this mode traffic is continuously generated on each of the APIs listed above, except the API _http/<status_code>
The environment property **CLIENT_SLEEP_INTERVAL_SECONDS** can be used to configure how often requests are generated.
The default value is **30**
