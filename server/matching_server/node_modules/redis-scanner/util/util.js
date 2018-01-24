var asyncLoop = function asyncLoop(arr, funk, exit){
    var index = 0;
    var loop = {
        next:function(){
            if (index < arr.length) {
                funk(arr[index++], loop);
            } else {
                exit();
            }
        },
        break:function(){
            exit();
        }
    };
    loop.next();
    return loop;
};

exports.asyncLoop = asyncLoop;