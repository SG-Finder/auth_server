Redis Scanner [![Build Status](https://travis-ci.org/iwhitfield/redis-scanner.svg?branch=master)](https://travis-ci.org/iwhitfield/redis-scanner) [![Code Climate](https://codeclimate.com/github/iwhitfield/redis-scanner/badges/gpa.svg)](https://codeclimate.com/github/iwhitfield/redis-scanner) [![Test Coverage](https://codeclimate.com/github/iwhitfield/redis-scanner/badges/coverage.svg)](https://codeclimate.com/github/iwhitfield/redis-scanner)
=============

- [Setup](#setup)
- [Integration](#integration)
	- [Binding](#binding)
	- [Scanner](#scanner)
- [API Usage](#api)
	- [Standard API](#standard-api)
	- [Object API](#object-api)
- [Notes](#notes)
	- [Early Exists](#early-exits)
	- [Error](#errors)
- [Issues](#issues)

Redis-scanner provides a way to use Redis `SCAN` in Node.js either synchronously or asynchronously. In terms of compatibility, `redis-scanner` is built on [TravisCI](https://travis-ci.org/iwhitfield/it.each) after every commit using Node 0.10.x and 0.12.x. In addition to this, the latest version of io.js is also covered in these builds. Build results are submitted to [Code Climate](https://codeclimate.com/github/iwhitfield/redis-scanner) for analysis.

The current version is below v1.0.0 so the potential does exist for breaking changes, however *I don't plan on doing this*. This module begins as an experiment into the best way to use `SCAN`. Naturally opinions of this may change over time.

### Setup

`redis-scanner` is available on [npm](https://www.npmjs.com/package/redis-scanner), so simply install it:

```bash
$ npm install redis-scanner
```

### Integration

You can bind the methods to your own `RedisClient` instance, or you can simply create an instance of the Scanner for use.

#### Binding

Below is an example of binding the methods to your own RedisClient instance. This surfaces the `SCAN` methods on the client by default.

```javascript
var redis = require('redis');
var redis_scanner = require('redis-scanner');

var client = redis.createClient();

redis_scanner.bindScanners(client);
```

Once this is done, you can call any of the scanning methods. These methods can be called using either lower or upper case.

```javascript
client.scan();
client.hscan();
client.sscan();
client.zscan();
```

#### Scanner

Creating an instance of Scanner for use is pretty simple:

```javascript
var redis = require('redis');
var redis_scanner = require('redis-scanner');

var client = redis.createClient();

var scanner = new redis_scanner.Scanner(client, 'SCAN', key, options);
```

In the above case, `key` is equal to the key of the object you're scanning. For `SCAN`, this is simply `null`.

The `options` object looks like the following. Instead of providing `args` you can optionally provide `pattern` and `count` to generate arguments internally. **Do not use both.**

```javascript
var options1 = {
    args: ['MATCH','key:*','COUNT','5'],
    onData: function(result[, done]){
    
    },
    onEnd: function(err){
    
    }
};

var options2 = {
    count: '5',
    pattern: 'key:*',
    onData: function(result[, done]){
    
    },
    onEnd: function(err){
    
    }
};
```

Scanners are reusable. Once you set one up, it will clean up after itself so it can be used again the moment `onEnd` has been called (i.e. don't run the same Scanner in two places at the same time). To execute, simply call `start()` on a `Scanner` instance.

### API

#### Standard API

There are two ways of using `redis-scanner`, which basically just boils down to personal preference (the backend is the same). The first is designed to be typical of the existing client methods, so it blends into the existing APIs transparently. This takes the form of `([key], [args], onData, onEnd)`.


```javascript
// async
client.scan([], function(result, done){
    done();
}, function(err){

});

// sync
client.scan([], function(result){

}, function(err){

});
```

Depending on the scanning method used, the `result` argument can be either a single string or an object containing `key` and `value`.

```javascript
// SCAN and SSCAN
'value'

// HSCAN and ZSCAN
{ 'key': 'field', 'value': 'value' }
```

Please note than `SCAN` does not take a `key` argument. However for the other methods `HSCAN`, `SSCAN` and `ZSCAN`, a key must be provided. If none is provided, an error will be thrown.

```javascript
client.hscan('my_hash', function(entry){

}, function(err){

});
```

#### Object API

The alternative is to simply pass all parameters within an object to the scanner. This is similar to how you would use the Scanner constructor directly as shown above.

Please note that `key` is still a separate parameter when using this API. Arguments passed to `onData` operate the same way as the Standard API.

```javascript
client.hscan('my_hash', {
    count: '5',
    pattern: 'key:*',
    onData: function(result[, done]){
    
    },
    onEnd: function(err){
    
    }
});
```

### Notes

#### Early Exits

Clearly you may sometimes have a need to exit before the iterator is complete (which is incidentally part of the reason why I felt the need to create this library). In this case, the remaining results are simply discarded and execution is halted. The context bound to the function provided as `onData` contains an `end` method which will end the processing and exit. Your `onEnd` will still be called, naturally.

```javascript
var count = 0;

// only process first 5 hits
client.scan([], function(entry){
    if(++count === 5){
        this.end();
    }
}, function(err){
    console.log(count); // 5
});
```

#### Errors

Any errors returned from the Redis client will be passed straight to the `onEnd` function and the process will be stopped. This is to avoid the case where multiple requests are timing out against the server and causing the process to hang unnecessarily.

Passing an error to the `done` callback in `onData` will also halt progress, and forward your `err` to the `onEnd` function.

```javascript
client.scan([], function(entry, done){
    done(new Error('Failed!');
}, function(err){
    console.log(err.message); // 'Failed!'
});
```

### Tests

Naturally, to run the tests you need Redis running. Tests operate on the default database and *will* wipe existing data, so be careful. Tests are controlled by Grunt.

```bash
$ npm test
# or
$ grunt test
```

You can also generate coverage reports in HTML format using:

```bash
$ grunt coverage
```

and `lcov` format using:

```bash
$ npm run lcov
```

### Issues

If you find any issues inside this module, feel free to open an issue [here](https://github.com/iwhitfield/redis-scanner/issues "Redis Scanner Issues").