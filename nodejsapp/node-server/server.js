const http = require('http');
const url = require('url');
const client = require('prom-client');
const collectDefaultMetrics = client.collectDefaultMetrics;
const Registry = client.Registry;
const register = new Registry();
collectDefaultMetrics({ register });

const server = http.createServer(async (req, res) =>  {

    if (req.url == '/data') { //check the URL of the current request
            console.log('GET Request /data');
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.write(JSON.stringify({ message: "Hello World"}));
            res.end();
    } else if (req.url == '/metrics') {
        console.log('GET Request /metrics');
        res.setHeader('Content-Type', register.contentType);
        res.end(await register.metrics());

    }
});

server.listen(8000);

console.log('Node.js web server at port 8000 is running..')
