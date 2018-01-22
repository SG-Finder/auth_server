var express = require('express');
var app = express();
var redis = require('redis'),
    client = redis.createClient(6379, '127.0.0.1');

var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
var port = process.env.PORT || 3000;


app.get('/testRedis', function (req, res) {
    res.send('testRedis');
});

app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});

io.on('connection', function (socket) {
    socket.on('ack', function (data) {
        console.log(data);
        if (data.token !== 'gksxodnd007') {
            socket.emit('authorized', { permit: true });
        }
        else {
            socket.disconnect(true);
        }
    });
});

io.on('ready', function (socket) {
   socket.emit('success-matching', {
       opponentID: "YJW1234",
       room: 142
   });
});

server.listen(port, function () {
    console.log('matching server listening on port :' + port);
});