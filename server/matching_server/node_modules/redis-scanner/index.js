var Scanner = require('./lib/scanner');

function bindScanners(client){

    client.scan = client.SCAN = function(){
        createScanner(client, 'SCAN', null, [].slice.call(arguments)).start();
    };

    client.hscan = client.HSCAN = function(key){
        createScanner(client, 'HSCAN', key, [].slice.call(arguments, 1)).start();
    };

    client.sscan = client.SSCAN = function(key){
        createScanner(client, 'SSCAN', key, [].slice.call(arguments, 1)).start();
    };

    client.zscan = client.ZSCAN = function(key){
        createScanner(client, 'ZSCAN', key, [].slice.call(arguments, 1)).start();
    };

}

exports.bindScanners = bindScanners;
exports.Scanner = Scanner;

function createScanner(client, type, key, options){
    var opts;
    var has_args = options[0] instanceof Array;

    if(typeof options[0] !== 'object' || has_args){
        if(!has_args){
            options.unshift([]);
        }
        opts = {
            args: options[0],
            onData: options[1],
            onEnd: options[2]
        };
    } else {
        opts = options[0];
    }

    return new Scanner(client, type, key, opts);
}