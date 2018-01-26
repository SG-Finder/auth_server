exports.matchingGame = function (waitingPlayer, player, TIER, matchingSpace, socket) {
    if (waitingPlayer[TIER].length !== 0) {
        let matchingResultData = {};
        let opponentPlayer = waitingPlayer[TIER].shift();
        matchingResultData.playersId = {
            playerA : player[socket.id].user_id,
            playerB : opponentPlayer.user_id
        };
        matchingResultData.roomId = generateRoomId();
        socket.emit('matchingResult', matchingResultData);
        console.log(opponentPlayer.socket_id);
        matchingSpace.to(opponentPlayer.socket_id).emit('matchingResult', matchingResultData);
    }
    else {
        waitingPlayer[TIER][waitingPlayer[TIER].length] = player[socket.id];
    }
};

generateRoomId = function () {
    return 1;
};