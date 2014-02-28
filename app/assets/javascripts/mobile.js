/**
 * mobile.js
 */

(function() {

    // STREAM
    var Stream = function(socket) {
        this.socket = socket;

        this.socket.onmessage = function onmessage(event) {
            var data = JSON.parse(event.data);
            if(data.type === 'question') {
                window.zenfeedback.newQuestion(data.question, data.answers);
            }
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
        };
    };

    ZenFeedBack.prototype.newQuestion = function(question, answers) {
        var questionTmpl = '<li><p class="question">'+ question +'</p>';
        var answersTmpl = '<ul class="answsers">' + answers.map(function(answer) {
            var t =
           '<li>\
                <label for="$name">$label</label>\
                <input type="checked" name="$id"/>\
            </li>';

            return t.replace(/\$id/g, answer.id)
                    .replace(/\$label/g, answer.label);

        }).join('') + '</ul>';

        var tmpl = questionTmpl + answersTmpl;
        ('#questions').html(tmpl);
    };

    ZenFeedBack.prototype.answerQuestion = function(questionId, answerId) {
        var anwser = {
            questionId: questionId,
            answerId: answerId
        };
        window.zStream.send(JSON.stringify(anwser));
    };

    // MAIN
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