// core module
const express = require('express');
const app = express();
const redis = require('redis')
const redisClient = redis.createClient(6379, '127.0.0.1');
const server = require('http').createServer(app);
const io = require('socket.io').listen(server);
let port = process.env.PORT || 3000;
const moment =  require('moment');

// DB module
const mongo = require('mongodb');
const mongoClient = mongo.MongoClient;
const dbURL = 'mongodb://127.0.0.1:27017/';

// customizing module
const sessionManage = require('./redis_dao/session');

// util
const game = require('./util/matchingGame');


app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});

let bronzeRoomUserList = [];
let silverRoomUserList = [];
let goldRoomUserList = [];
let player = {};
const matchingSpace = io.of('/matching');

matchingSpace.on('connection', function (socket) {
    //console.log(socket.id);
    socket.on('ack', function (data) {
        let key = "session:" + data.session_token + ":" + data.userId;
        sessionManage.isValidSession(redisClient, key, function (isValidSession) {
            if (isValidSession) {
                sessionManage.findSessionData(redisClient, key, function (value) {
                    let newSession = JSON.parse(value);
                    newSession.last_updated_at = moment().format("YYYY-MM-DDThh:mm:ss");
                    sessionManage.updateSession(redisClient, key, JSON.stringify(newSession));
                    socket.emit('authorized', { permit: true });
                });
            }
            else {
                socket.emit('authorized', { permit: false });
                socket.disconnect();
            }
        });

        mongoClient.connect(dbURL, function (err, db) {
            if (err) {
                throw err;
            }
            let dbo = db.db('genie_ai');
            let query = { user_id: data.gameId };
            dbo.collection('players').find(query).toArray(function (err, playerData) {
                if (err) {
                    socket.emit('getData', { dataAccess : false });
                    throw err;
                }
                player[socket.id] = playerData[0];
                player[socket.id].socket_id = socket.id;
                db.close();
            });

            socket.emit('getData', { dataAccess : true });
        });
    });

    socket.on('ready', function (data) {
        // todo delete event it is just testing event
        if (player[socket.id] == null) {
            socket.emit('retryReady', { error: "connection DB is fail"});
            return;
        }
        console.log(player[socket.id].user_id);
        socket.join(player[socket.id].tier, function () {
            matchingSpace.to(player[socket.id].tier).emit('entry', {
                //todo sending data
            });
        });

        if (player[socket.id].tier === 'BRONZE') {
            bronzeRoomUserList[bronzeRoomUserList.length] = player[socket.id];
        }
        else if (player[socket.id].tier === 'SILVER') {
            silverRoomUserList[silverRoomUserList.length] = player[socket.id];
        }
        else if (player[socket.id].tier === 'GOLD') {
            goldRoomUserList[goldRoomUserList.length] = player[socket.id];
        }
        console.log(io.sockets.adapter.rooms);
    });

    // todo seperate
    socket.on('disconnect', function (reason) {
        console.log(reason);
        socket.leave(player[socket.id].tier, function () {
           matchingSpace.to(player[socket.id]).emit('leave', {
               //todo sending data
           });
        });

        //todo delete element of array with player array
        // player[socket.id] 삭제
        if (player[socket.id].tier === 'BRONZE') {
            bronzeRoomUserList[bronzeRoomUserList.length] = player[socket.id];
        }
        else if (player[socket.id].tier === 'SILVER') {
            silverRoomUserList[silverRoomUserList.length] = player[socket.id];
        }
        else if (player[socket.id].tier === 'GOLD') {
            goldRoomUserList[goldRoomUserList.length] = player[socket.id];
        }
    });

    socket.on('gameStart', function (data) {
        //todo matching
        socket.emit('matching', data);

        socket.on('matchingAck', function (data) {
            //todo matching data 비교 후 matching Success 후 disconnect
            socket.emit('matchingResult', {
                matching : true,
                afterEvent : "disconnect"
            });
            socket.disconnect();
        });
    });

    socket.on('sendMessage', function (msg) {
        matchingSpace.to(player[socket.id].tier).emit('receiveMessage', {
            from: player[socket.id].user_id,
            message: msg.message
        });
        console.log(player[socket.id]);
    })
});

server.listen(port, function () {
    console.log('matching server listening on port :' + port);
});