// snippet-start:[sqs.JavaScript.messages.sendMessage]
// Load the AWS SDK for Node.js
var AWS = require('aws-sdk');
var https = require('https');
const promClient = require('prom-client');
const osModule = require('os');

// Set the region 
AWS.config.update({region: 'us-west-2'});

// Create an SQS service object
var sqs = new AWS.SQS({apiVersion: '2012-11-05'});

function sleep(milliseconds) {
  const date = Date.now();
  let currentDate = null;
  do {
    currentDate = Date.now();
  } while (currentDate - date < milliseconds);
}

function eatMemory(count) {
  let store = [];
  for(let i=0;i < count * 1024;i++) {
    store.push(i);
  }
}

const networkInterfaces = osModule.networkInterfaces;
const Counter = promClient.Counter;
const Histogram = promClient.Histogram;
const Gauge = promClient.Gauge;
const collectDefaultMetrics = promClient.collectDefaultMetrics;
const globalRegister = promClient.register;

// console.log("Network Interfaces Object: "+networkInterfaces);
const nets = networkInterfaces();
// console.log("Network Interfaces: "+nets);
const networkAddresses = {}; // Or just '{}', an empty object

for (const name of Object.keys(nets)) {
  for (const net of nets[name]) {
      // Skip over non-IPv4 and internal (i.e. 127.0.0.1) addresses
      if (net.family === 'IPv4' && !net.internal) {
          if (!networkAddresses[name]) {
              networkAddresses[name] = [];
          }
          networkAddresses[name].push(net.address);
      }
  }
}

let labelNames = ['job', 'function_name', 'version', 'instance', 'namespace'];
let labels = {
  namespace: 'AWS/Lambda',
  asserts_tenant: 'chief'
};

let instance;
let keys = Object.keys(networkAddresses);
if( keys.length > 0 ) {
  instance = networkAddresses[keys[0]][0];
  labels.instance = instance + '/'+ process.pid;
}

collectDefaultMetrics({
  labelNames: labelNames,
	timeout: 10000,
	gcDurationBuckets: [0.001, 0.01, 0.1, 1, 2, 5], // These are the default buckets.
});

const upMetric = new Gauge({
	name: 'up',
	help: `Heartbeat metric`,
	registers: [globalRegister],
	labelNames: labelNames
});


const invocations = new Counter({
	name: 'aws_lambda_invocations_total',
	help: `AWS Lambda Invocations Count`,
	registers: [globalRegister],
	labelNames: labelNames
});

const errors = new Counter({
	name: 'aws_lambda_errors_total',
	help: `AWS Lambda Errors Count`,
	registers: [globalRegister],
	labelNames: labelNames
});

const latency = new Histogram({
	name: 'aws_lambda_duration_seconds',
	help: `AWS Lambda Duration Histogram`,
	registers: [globalRegister],
	labelNames: labelNames
});

globalRegister.registerMetric(upMetric);
globalRegister.registerMetric(invocations);
globalRegister.registerMetric(errors);
globalRegister.registerMetric(latency);

let pushMetrics = async () => {
  // console.log("Push Metrics called... : " + JSON.stringify(labels));
  if(labels.function_name) {
    labels.function_name = labels.function_name + '-prom-client-direct';
    labels.job = labels.function_name;
    // console.log("Set job : " + JSON.stringify(labels));
    globalRegister.setDefaultLabels(labels);
    upMetric.set({}, 1);
    let metrics = await globalRegister.metrics();
    globalRegister.resetMetrics();
    // console.log(JSON.stringify(networkAddresses));
    // console.log("\n\nMETRICS\n\n"+metrics+"\n\n");
    // console.log("Pushing metrics to gateway with labels:" + JSON.stringify(labels));
    const options = {
      hostname: 'chief.tsdb.dev.asserts.ai',
      port: 443,
      path: '/api/v1/import/prometheus',
      method: 'POST',
      headers: {
        'Authorization': 'Basic ' + Buffer.from('chief:chieftenant').toString('base64'),
        'Content-Type': 'text/plain',
        'Content-Length': metrics.length
      }
    };

    const req = https.request(options, res => {
      console.log(`POST https://chief.tsdb.dev.asserts.ai/api/v1/import/prometheus statusCode: ${res.statusCode}`);
      
      if(res.statusCode.toString()==="400") {
        console.log(res.toString());
      }
    
      res.on('data', d => {
        process.stdout.write(d);
      })
    })
    
    req.on('error', error => {
      console.error('POST https://chief.tsdb.dev.asserts.ai/api/v1/import/prometheus resulted in an error: '+error.toString());
    })
    
    req.write(metrics);
    req.end();
  } else {
    console.log("Job name not known yet");
  }
  setTimeout(pushMetrics, 15000);
};

setTimeout(pushMetrics, 15000);

exports.handler = asserts.wrapHandler(async (event, context) => {
  let start = Date.now();

  labels.function_name = context.functionName; 
  labels.version = context.functionVersion;
  
  invocations.inc({}, 1);
  
  let error = true;
  try {
  
    // console.log("Num Messages in batch: "+event.Records.length);
    let firstRecord = event.Records[0];
    // console.log(firstRecord);
    let body = JSON.parse(firstRecord.body);
    // console.log("Body: "+body);
    // console.log("Problem :"+body.problem);
    // console.log("About to put message in SQS Queue https://sqs.us-west-2.amazonaws.com/342994379019/Fullfilment");
    
    var params = {
       // Remove DelaySeconds parameter and value for FIFO queues
      DelaySeconds: 10,
      MessageAttributes: {
        "Title": {
          DataType: "String",
          StringValue: "The Whistler"
        },
        "Author": {
          DataType: "String",
          StringValue: "John Grisham"
        },
        "WeeksOn": {
          DataType: "Number",
          StringValue: "6"
        }
      },
      MessageBody: "Information about current NY Times fiction bestseller for week of 12/11/2016.",
      QueueUrl: "https://sqs.us-west-2.amazonaws.com/342994379019/Fullfilment"
    };
  
    console.log("v3 About to put message in SQS Queue https://sqs.us-west-2.amazonaws.com/342994379019/Queue6");
    sqs.sendMessage(params, function(err, data) {
      if (err) {
        console.log("v2 Failed to put message in SQS Queue", err);
      } else {
        console.log("v2 Successfully put message in SQS Queue", data.MessageId);
      }
    });
    
    if(body['problem']==='normal') {
      console.log("Act normal");
    } else if(body['problem']==='latency') {
      console.log("Latency");
      sleep(body['measure']);
    } else if(body['problem']==='memory') {
      console.log("Memory");
      eatMemory(body['measure']);
    } else if(body['problem']==='error') {
      let toss = 100 * Math.random();
      let check = body['measure'];
      if(toss < check) {
        console.log(toss+" < " + check+": Error ");
        throw new Error.Error("Controlled Error");
      } else {
        console.log(toss+" > " + check+": Success ");
      }
    }
    const response = {
        statusCode: 200,
        event: JSON.stringify('Hello from Lambda!'),
    };
    error = false;
    let end = Date.now();
    latency.observe({}, (end - start)/1000);
    return response;
  } finally{
    if(error) {
      errors.inc({}, 1);
    }
  }
});