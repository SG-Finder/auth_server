exports.matchingGame = function (playerArr) {
    let playerAIndex, playerBIndex;
    let playerA, playerB;

    playerAIndex = Math.floor(Math.random() * playerArr.length);
    playerBIndex = Math.floor(Math.random() * playerArr.length);
    while (playerBIndex !== playerAIndex) {
        playerBIndex = Math.floor(Math.random() * playerArr.length);
    }

    playerA = playerArr[playerAIndex];
    playerB = playerArr[playerBIndex];
    let room = generateRoomId();
    let matchingPlayers = {
        playerId: {
            playerA: playerA.user_id,
            playerB: playerB.user_id
        },
        roomId: room
    }

    return matchingPlayers;
};

exports.generateRoomId = function () {
    return 1;
};