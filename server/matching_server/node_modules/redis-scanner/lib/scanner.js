var async = require('async');
var util = require('../util/util');

function Scanner(client, cmd, key, options){

    this.args = options.args || [];

    if(key){
        if(typeof key !== 'string'){
            throw new Error('Invalid key provided!');
        } else {
            this.key = key;
        }
    }

    if(options){
        if(options.match || options.pattern){
            this.args.push('MATCH', options.match || options.pattern);
        }

        if(options.count){
            this.args.push('COUNT', options.count);
        }
    }

    this.cursor = undefined;

    this.ended = false;

    this.end = function(){
        this.ended = true;
    };

    this.error = false;

    this.isEnded = function(){
        return this.ended;
    };

    this.onData = options.onData || function(){ };

    this.onEnd = options.onEnd || function(){ };

    this.reset = function(){
        this.cursor = undefined;
        this.ended = false;
        this.error = false;
    };

    this.start = function(){

        async.whilst(

            // condition phase
            function () {
                return this.cursor !== 0 && !this.isEnded();
            }.bind(this),

            // process phase
            function (callback) {

                // calculate arguments
                var args = [this.cursor || 0].concat(this.args);

                // account for keys
                if(this.key){
                    args.unshift(this.key);
                }

                // scan from the last known cursor
                client.send_command(cmd, args, function(err, response){

                    if (err) {
                        // set error state
                        this.error = true;

                        // call error handler
                        this.onEnd(err);

                        // end process
                        return this.end();
                    }

                    // store the latest cursor
                    this.cursor = parseInt(response[0], 10);

                    // grab the matches
                    var matches = response[1];

                    // account for hscan and zscan mapping
                    if(cmd === 'HSCAN' || cmd === 'ZSCAN'){

                        var tmp = [];

                        // map array to objects
                        for(var i = 0; i < matches.length; i += 2){
                            tmp.push({
                                key: matches[i],
                                value: matches[i + 1]
                            });
                        }

                        // set matches
                        matches = tmp;
                    }

                    // loop all matches
                    util.asyncLoop(matches, function(match, loop){

                        // provide async
                        if(this.onData.length === 2){

                            // provide match and async callback
                            this.onData.bind(this)(match, function(err){

                                if(err){
                                    // set error state
                                    this.error = true;

                                    // call error handler
                                    this.onEnd(err);

                                    // end process
                                    this.end();
                                }

                                // if ended break, else loop
                                loop[this.isEnded() ? 'break' : 'next']();

                            }.bind(this));

                        } else {

                            // call user function
                            this.onData.bind(this)(match);

                            // if ended break, else loop
                            loop[this.isEnded() ? 'break' : 'next']();

                        }

                    }.bind(this), callback);

                }.bind(this));

            }.bind(this),

            // end phase
            function(){
                // reset Scanner state
                this.reset();

                // call end handler if no error
                if(!this.error){
                    this.onEnd();
                }
            }.bind(this)

        );

    };

}

module.exports = Scanner;