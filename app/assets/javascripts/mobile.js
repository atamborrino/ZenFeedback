/**
 * mobile.js
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
            $('.empty').addClass('hidden');
            var data = JSON.parse(event.data);
            if(data.question) {
                window.zenFeedBack.newQuestion(data.question, data.answers);
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
            isReady = true;
            pendingCallback && pendingCallback();
        };
    };

    ZenFeedBack.prototype.newQuestion = function(question, answers) {
        var questionTmpl = '<p class="name">'+ question.name +'</p><input type="hidden" name="question-id" value="'+ question.uuid +'"/>';
        var answersTmpl = '<ul class="answsers">' + answers.map(function(answer) {
            var t =
                '<li>\
                <span class="color"></span><button class="answer" data-value="$answerId">$label</button>\
                </li>';

            return t.replace(/\$answerId/g, answer.uuid)
                .replace(/\$label/g, answer.name);
        }).join('');

        var tmpl = questionTmpl + answersTmpl;
        $('form[name=question]').html(tmpl);
    };

    ZenFeedBack.prototype.answerQuestion = function(questionId, answerId) {
        var answer = {
            questionId: questionId,
            answerId: answerId
        };
        window.zStream.send(answer);
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
    onReady(function() {
        $('body').on('click tap', 'form[name=question] button.answer', function(e) {
            e.preventDefault();
            var questionId = $('input[name=question-id]').val();
            var answerId =  $(this).attr('data-value');
            window.zenFeedBack.answerQuestion(questionId, answerId);
            $('.empty').removeClass('hidden');
            $('form[name=question]').empty();
        });
    });
});