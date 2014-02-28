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
    var host = $('body').attr('data-host');
    window.zenFeedBack = new ZenFeedBack(
        'ws://'+host+'/rooms/' + roomName + '/orga/ws',
        function init(socket) {
            window.zStream = new Stream(socket);
        }
    );

})();

$(document).ready(function() {
    console.log('Welcome to ZenFeedBack !');
    onReady(function() {
        var $form = $('form[name=new-question]');
        $form.on('submit', function(e) {
            e.preventDefault();
            var newQuestion = $(this).serializeArray().reduce(function(acc, p) {
                if(p.name === 'question') {
                    acc.question = {
                        name: p.value
                    };
                } else if(p.name === 'answer') {
                    if(!acc.answers) acc.answers = [];
                    acc.answers.push({
                        name: p.value
                    });
                }
                return acc;
            }, {});
            window.zStream.send(newQuestion);
            $(this).find('input').val('');
        });
    });
});