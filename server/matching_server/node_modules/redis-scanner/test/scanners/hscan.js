var redis = require('redis');
var should = require('should');

var client = redis.createClient();

require('../../index').bindScanners(client);

describe('HSCAN', function(){

    describe('using standard configuration', function(){

        before('bootstrap keys', function(start){

            var hash = {};
            for(var i = 0; i < 50; i++){
                hash['key:' + i] = i;
            }

            client.hmset('test_hash', hash, function(err, result){
                should(err).not.be.ok;
                should(result).eql('OK');

                start();
            });

        });

        it('iterates through all keys', function(next){

            var count = 0;

            client.hscan('test_hash', function(){
                count++;
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(50);
                next();
            });

        });

        it('allows asynchronous execution', function(next){

            var count = 0;
            var results = [];

            client.hscan('test_hash', function(result, done){
                setTimeout(function(){
                    results.push(count++);
                    done();
                }, 10);
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(50);

                var counter = 0;
                results.forEach(function(result){
                    should(result).eql(counter++);
                });

                next();
            });

        });

        it('allows pattern matching', function(next){

            var count = 0;

            client.hscan('test_hash', ['MATCH','key:1*'], function(result){
                should(result.key).match(/key:1*/);
                count++;
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(11);
                next();
            });

        });

        it('exits when specified', function(next){

            var count = 0;

            client.hscan('test_hash', function(){
                if(count++ === 5){
                    this.end();
                }
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(6);
                next();
            });

        });

        it('exits on an error', function(next){

            client.hscan('test_hash', function(result, done){
                done(new Error('Fake Error!'));
            }, function(err){
                should(err).be.ok;
                should(err.message).eql('Fake Error!');
                next();
            });

        });

        it('can use upper case method names', function(next){

            var count = 0;

            client.HSCAN('test_hash', function(){
                if(count++ === 5){
                    this.end();
                }
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(6);
                next();
            });

        });

        after(function(done){
            client.flushdb(done);
        });

    });

    describe('using object configuration', function(){

        before('bootstrap keys', function(start){

            var hash = {};
            for(var i = 0; i < 50; i++){
                hash['key:' + i] = i;
            }

            client.hmset('test_hash', hash, function(err, result){
                should(err).not.be.ok;
                should(result).eql('OK');

                start();
            });

        });

        it('iterates through all keys', function(next){

            var count = 0;

            client.hscan('test_hash', {
                onData: function(){
                    count++;
                },
                onEnd: function(){
                    should(count).eql(50);
                    next();
                },
                onError: next
            });

        });

        it('allows asynchronous execution', function(next){

            var count = 0;
            var results = [];

            client.hscan('test_hash', {
                onData: function(result, done){
                    setTimeout(function(){
                        results.push(count++);
                        done();
                    }, 10);
                },
                onEnd: function(){
                    should(count).eql(50);

                    var counter = 0;
                    results.forEach(function(result){
                        should(result).eql(counter++);
                    });

                    next();
                },
                onError: next
            });

        });

        it('allows pattern matching', function(next){

            var count = 0;

            client.hscan('test_hash', {
                pattern: 'key:1*',
                onData: function(result){
                    should(result.key).match(/key:1*/);
                    count++;
                },
                onEnd: function(){
                    should(count).eql(11);
                    next();
                },
                onError: next
            });

        });

        it('exits when specified', function(next){

            var count = 0;

            client.hscan('test_hash', {
                onData: function(){
                    if(count++ === 5){
                        this.end();
                    }
                },
                onEnd: function(){
                    should(count).eql(6);
                    next();
                },
                onError: next
            });

        });

        it('exits on an error', function(next){

            client.hscan('test_hash', {
                onData: function(key, done){
                    done(new Error('Fake Error!'));
                },
                onEnd: function(err){
                    should(err).be.ok;
                    should(err.message).eql('Fake Error!');
                    next();
                }
            });

        });

        it('can use upper case method names', function(next){

            var count = 0;

            client.HSCAN('test_hash', {
                onData: function(){
                    if(count++ === 5){
                        this.end();
                    }
                },
                onEnd: function(){
                    should(count).eql(6);
                    next();
                },
                onError: next
            });

        });

        after(function(done){
            client.flushdb(done);
        });

    });

});