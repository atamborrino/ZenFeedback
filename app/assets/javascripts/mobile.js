/**
 * mobile.js
 */

(function() {
    var ZenFeedBack = function(streamURL, init) {
        var self = this;
        var socket = new WebSocket(streamURL);

        socket.onopen = function() {
            init(socket);
        };
    };

    var Stream = function(socket) {
        this.socket = socket;

        this.socket.onmessage = function onmessage(event) {
            console.log(event.data);
        };
    };

    Stream.prototype.send = function(data) {
        this.socket.send(JSON.stringify(data));
    };

    // Let's go
    var roomName = $('body').attr('data-room');
    window.zenFeedBack = new ZenFeedBack(
        'ws://localhost:9000/rooms/' + roomName + '/ws',
        function init(socket) {
            window.zStream = new Stream(socket);
        }
    );

})();

$(document).ready(function() {
    console.log('Welcome to ZenFeedBack !');
    window.zStream.send({ msg: 'ping!'});
});