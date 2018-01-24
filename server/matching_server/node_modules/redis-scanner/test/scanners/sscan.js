var redis = require('redis');
var should = require('should');

var client = redis.createClient();

require('../../index').bindScanners(client);

describe('SSCAN', function(){

    describe('using standard configuration', function(){

        before('bootstrap keys', function(start){

            var set = [];
            for(var i = 0; i < 50; i++){
                set.push('key:' + i);
            }

            client.sadd('test_set', set, function(err, result){
                should(err).not.be.ok;
                should(result).eql(50);

                start();
            });

        });

        it('iterates through all keys', function(next){

            var count = 0;

            client.sscan('test_set', function(){
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

            client.sscan('test_set', function(result, done){
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

            client.sscan('test_set', ['MATCH','key:1*'], function(result){
                should(result).match(/key:1*/);
                count++;
            }, function(err){
                should(err).not.be.ok;
                should(count).eql(11);
                next();
            });

        });

        it('exits when specified', function(next){

            var count = 0;

            client.sscan('test_set', function(){
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

            client.sscan('test_set', function(result, done){
                done(new Error('Fake Error!'));
            }, function(err){
                should(err).be.ok;
                should(err.message).eql('Fake Error!');
                next();
            });

        });

        it('can use upper case method names', function(next){

            var count = 0;

            client.SSCAN('test_set', function(){
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

            var set = [];
            for(var i = 0; i < 50; i++){
                set.push('key:' + i);
            }

            client.sadd('test_set', set, function(err, result){
                should(err).not.be.ok;
                should(result).eql(50);

                start();
            });

        });

        it('iterates through all keys', function(next){

            var count = 0;

            client.sscan('test_set', {
                onData: function(){
                    count++;
                },
                onEnd: function(err){
                    should(err).not.be.ok;
                    should(count).eql(50);
                    next();
                }
            });

        });

        it('allows asynchronous execution', function(next){

            var count = 0;
            var results = [];

            client.sscan('test_set', {
                onData: function(result, done){
                    setTimeout(function(){
                        results.push(count++);
                        done();
                    }, 10);
                },
                onEnd: function(err){
                    should(err).not.be.ok;
                    should(count).eql(50);

                    var counter = 0;
                    results.forEach(function(result){
                        should(result).eql(counter++);
                    });

                    next();
                }
            });

        });

        it('allows pattern matching', function(next){

            var count = 0;

            client.sscan('test_set', {
                pattern: 'key:1*',
                onData: function(result){
                    should(result).match(/key:1*/);
                    count++;
                },
                onEnd: function(err){
                    should(err).not.be.ok;
                    should(count).eql(11);
                    next();
                }
            });

        });

        it('exits when specified', function(next){

            var count = 0;

            client.sscan('test_set', {
                onData: function(){
                    if(count++ === 5){
                        this.end();
                    }
                },
                onEnd: function(err){
                    should(err).not.be.ok;
                    should(count).eql(6);
                    next();
                }
            });

        });

        it('exits on an error', function(next){

            client.sscan('test_set', {
                onData: function(result, done){
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

            client.SSCAN('test_set', {
                onData: function(){
                    if(count++ === 5){
                        this.end();
                    }
                },
                onEnd: function(err){
                    should(err).not.be.ok;
                    should(count).eql(6);
                    next();
                }
            });

        });

        after(function(done){
            client.flushdb(done);
        });

    });

});