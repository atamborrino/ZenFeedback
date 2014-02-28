/**
 * orga.js
 */

var pendingCallback;
var isReady = false;
var onReady = function(cb) {
    if(isReady) cb && cb();
    pendingCallback = cb;
};

(function() {

    // STREAM
    var Stream = function(socket) {
        this.socket = socket;

        this.socket.onmessage = function onmessage(event) {
            var data = JSON.parse(event.data);
        };
    };

    Stream.prototype.send = function(data) {
        this.socket.send(JSON.stringify(data));
    };

    // APP
    var ZenFeedBack = function(streamURL, init) {
        var self = this;
        var socket = new WebSocket(streamURL);

        socket.onopen = function() {
            init(socket);
            isReady = true;
            pendingCallback && pendingCallback();
        };
    };

    // MAIN
    var roomName = $('body').attr('data-room');
    window.zenFeedBack = new ZenFeedBack(
        'ws://localhost:9000/rooms/' + roomName + '/orga/ws',
        function init(socket) {
            window.zStream = new Stream(socket);
        }
    );

})();

$(document).ready(function() {
    console.log('Welcome to ZenFeedBack !');
    onReady(function() {
        window.zStream.send({ msg: 'ping!'});
    });
});