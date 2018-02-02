// Core module
const express = require('express');
const app = express();
const redis = require('redis');
const redisClient = redis.createClient(6379, '127.0.0.1');
const server = require('http').createServer(app);
const io = require('socket.io').listen(server);
let port = process.env.PORT || 3000;
const moment =  require('moment');
const matchingSpace = io.of('/matching');

// DB module
const mongo = require('mongodb');
const mongoClient = mongo.MongoClient;
const dbURL = 'mongodb://127.0.0.1:27017/';

// Customizing module
const sessionManage = require('./redis_dao/session');

// Util
const game = require('./util/matchingGame');
let player = {};
const TIER = {
    BRONZE: 0,
    SILVER: 1,
    GOLD: 2
};
let waitingPlayer = [];
waitingPlayer[TIER.BRONZE] = [];
waitingPlayer[TIER.SILVER] = [];
waitingPlayer[TIER.GOLD] = [];

app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});

matchingSpace.on('connection', function (socket) {
    console.log("someone connects this server");
    socket.on('ack', function (data) {
        console.log(data);
        //TODO BAD REQUEST EXCEPTION
        let key = "session:" + data.session_token + ":" + data.userId;
        let sessionFlag = false;
        sessionManage.isValidSession(redisClient, key, function (isValidSession) {
            if (isValidSession) {
                sessionManage.findSessionData(redisClient, key, function (value) {
                    let newSession = JSON.parse(value);
                    newSession.last_updated_at = moment().format("YYYY-MM-DDThh:mm:ss");
                    sessionManage.updateSession(redisClient, key, JSON.stringify(newSession));
                    socket.emit('authorized', {
                        permit : true,
                        afterEvent : "none"
                    });
                });

                mongoClient.connect(dbURL, function (err, db) {
                    if (err) {
                        console.log(err);
                        //TODO send data
                        socket.emit('getData', {
                            dataAccess : false,
                            afterEvent : "disconnect"
                        });
                        socket.disconnect();
                        return;
                    }
                    let dbo = db.db('genie_ai');
                    let query = { userId: data.userId };
                    dbo.collection('players').find(query).toArray(function (err, playerData) {
                        if (err) {
                            socket.emit('getData', {
                                dataAccess : false,
                                afterEvent : "disconnect"
                            });
                            console.log(err);
                            return;
                        }
                        player[socket.id] = playerData[0];
                        console.log(playerData[0]);
                        player[socket.id].socket_id = socket.id;
                        db.close();
                    });

                    socket.emit('getData', {
                        dataAccess : true,
                        afterEvent : "ready"
                    });
                });

            }
            else {
                socket.emit('authorized', {
                    permit : false,
                    afterEvent : "disconnect"
                });
                socket.disconnect();
                sessionFlag = false;
            }
        });

    });

    socket.on('ready', function (data) {
        //TODO delete event it is just testing event
        if (player[socket.id] == null) {
            socket.emit('retryReady', { error: "connection DB is fail"});
            return;
        }

        socket.join(player[socket.id].tier, function () {
            matchingSpace.to(player[socket.id].tier).emit('entry', {
                entryUser: player[socket.id].userId,
                entryUserScore: player[socket.id].score,
                entryUserTier: player[socket.id].tier
            });
        });
    });

    socket.on('gameStart', function () {
        //TODO modulation && 동시에 접속했을 때의 이슈 && 매칭이 실패했을 때의 이슈
        //TODO 매칭 결과 redis에 저장
        //let timeoutBronzeMatching;
        if (player[socket.id].tier === 'BRONZE') {
            if (waitingPlayer[TIER.BRONZE].length !== 0) {
                let matchingResultData = {};
                let opponentPlayer = waitingPlayer[TIER.BRONZE].shift();
                matchingResultData.playersId = [player[socket.id].userId, opponentPlayer.userId];
                matchingResultData.roomId = game.generateRoomId();
                socket.emit('matchingResult', matchingResultData);
                console.log(opponentPlayer.socket_id);
                matchingSpace.to(opponentPlayer.socket_id).emit('matchingResult', matchingResultData);
                //clearTimeout(timeoutBronzeMatching);
            }
            else {
                waitingPlayer[TIER.BRONZE][waitingPlayer[TIER.BRONZE].length] = player[socket.id];
                // timeoutBronzeMatching = setTimeout(function () {
                //     socket.emit('matchingResult', {
                //         matchingResult: 'fail'
                //     });
                //     waitingPlayer[TIER.BRONZE].shift();
                // }, 300000)
            }
        }
        else if (player[socket.id].tier === 'SILVER') {
            if (waitingPlayer[TIER.SILVER].length !== 0) {
                let matchingResultData = {};
                let opponentPlayer = waitingPlayer[TIER.SILVER].shift();
                matchingResultData.playersId = [player[socket.id].userId, opponentPlayer.userId];
                matchingResultData.roomId = generateRoomId();

                socket.emit('matchingResult', matchingResultData);
                console.log(opponentPlayer.socket_id);
                matchingSpace.to(opponentPlayer.socket_id).emit('matchingResult', matchingResultData);
            }
            else {
                waitingPlayer[TIER.SILVER][waitingPlayer[TIER.SILVER].length] = player[socket.id];
            }
        }
        else {
            if (waitingPlayer[TIER.GOLD].length !== 0) {
                let matchingResultData = {};
                let opponentPlayer = waitingPlayer[TIER.GOLD].shift();
                matchingResultData.playersId = [player[socket.id].userId, opponentPlayer.userId];
                matchingResultData.roomId = generateRoomId();

                socket.emit('matchingResult', matchingResultData);
                matchingSpace.to(opponentPlayer.socket_id).emit('matchingResult', matchingResultData);
                console.log(opponentPlayer.socket_id);
            }
            else {
                waitingPlayer[TIER.GOLD][waitingPlayer[TIER.GOLD].length] = player[socket.id];
            }
        }
    });

    socket.on('sendMessage', function (msg) {
        matchingSpace.to(player[socket.id].tier).emit('receiveMessage', {
            from: player[socket.id].userId,
            message: msg.message
        });
    });

    socket.on('disconnect', function (reason) {
        //console.log(reason);
        console.log('somenoe disconnect this server');
        console.log(socket.id);
        if (player[socket.id] !== undefined ) {
            socket.leave(player[socket.id].tier, function () {
                matchingSpace.to(player[socket.id].tier).emit('leave', {
                    leaveUser: player[socket.id].userId,
                    leaveUserScore: player[socket.id].score,
                    leaveUserTier: player[socket.id].tier
                });
                //delete element in player object
                delete player[socket.id];
                console.log(player);
            });
        }
    });
});

server.listen(port, function () {
    console.log('matching server listening on port :' + port);
});