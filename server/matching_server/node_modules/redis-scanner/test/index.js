var redis = require('redis');
var should = require('should');

var client = redis.createClient();
var redis_scanner = require('../index');

redis_scanner.bindScanners(client);

describe('Module', function(){

    it('handles bad parameters', function(next){

        client.scan();

        setTimeout(next, 250);

    });

    it('handles client errors using standard configuration', function(next){

        client.scan(['COUNT',-1], function(){
            // no-op
        }, function(err){
            should(err).be.ok;
            should(err.message).eql('ERR syntax error');
            next();
        });

    });

    it('handles client errors using object configuration', function(next){

        client.scan({
            count: -1,
            onEnd: function(err){
                should(err.message).eql('ERR syntax error');
                next();
            }
        });

    });

    it('handles invalid keys', function(){

        try {
            client.hscan({});
            should.fail();
        } catch(e) {
            should(e.message).eql('Invalid key provided!');
        }

    });

    it('uses the Scanner constructor directly', function(next){

        new redis_scanner.Scanner(client, 'SCAN', null, {
            onEnd: function(err){
                should(err).not.exist;
                next();
            }
        }).start();

    });

    after(function(done){
        client.flushdb(done);
    });

});