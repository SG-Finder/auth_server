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
        let key = "session:" + data.token + ":" + data.userID;
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
        if (player[socket.id] == null) {
            socket.emit("retryReady", data);
            return;
        }
        console.log(player[socket.id].user_id);
        socket.join(player[socket.id].tier, function () {
            let room = Object.keys(socket.rooms);
            console.log(room);
            matchingSpace.to(player[socket.id].tier).emit('entry', player[socket.id].user_id + "님이 입장하셨습니다.");
        });

        if (player[socket.id].tier === 'BRONZE') {
            bronzeRoomUserList[bronzeRoomUserList.length] = player;
        }
        else if (player[socket.id].tier === 'SILVER') {
            silverRoomUserList[silverRoomUserList.length] = player;
        }
        else if (player[socket.id].tier === 'GOLD') {
            goldRoomUserList[goldRoomUserList.length] = player;
        }
        console.log(io.sockets.adapter.rooms);
    });

    // todo seperate
    socket.on('disconnect', function (reason) {
        console.log(reason);
        // if (player.tier === 'BRONZE') { //bronze
        //     socket.leave('bronze', function () {
        //         matchingSpace.to('bronze').emit('entry', player.user_id + "님이 퇴장하셨습니다.");
        //     });
        // }
        // else if (player.tier === 'SILVER') { //silver
        //     socket.leave('silver', function () {
        //         matchingSpace.to('silver').emit('entry', player.user_id + "님이 퇴장하셨습니다.");
        //     });
        // }
        // else if (player.tier === 'GOLD') { //gold
        //     socket.leave('gold', function () {
        //         matchingSpace.to('gold').emit('entry', player.user_id + "님이 퇴장하셨습니다.");
        //     });
        // }
    });

    socket.on('matching', function (data) {
        //todo matching
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