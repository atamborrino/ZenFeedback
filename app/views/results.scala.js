@(roomName: String)(implicit request: RequestHeader)

(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var socket = new WS("@routes.Application.connectResultsWS(roomName).webSocketURL()");

  var nbTotalVotes = 0;
  var answers = {};

  var init = function() {
    socket.onmessage = receiveEvent;
  };

  /* json event schema:
   {
   name: string,
   uuid: UUID,
   nbVotes: number
   }
   */
  var update = function(data) {
    updateQuestion(data.question);
    updateNbTotalVotes(data.answers);
    updateAnswers(data.answers);
  };

  var updateQuestion = function(question) {
    if (question !== undefined) {
      $('.questionName').text(question.name);
    }
  };

  var updateAnswers = function(answers) {
    if (answers !== undefined) {
      for (var i = 0; i < answers.length; i++) {
        updateAnswer(answers[i]);
      }
    }
  };

  var updateNbTotalVotes = function(answers) {
    if (answers !== undefined) {
      nbTotalVotes = 0;
      for (var i = 0; i < answers.length; i++) {
        nbTotalVotes += answers[i].nbVotes;
      }
    }
  };

  var getAnswerId = function(answerUuid) {
    return 'answer-' + answerUuid;
  };

  var updateAnswer = function(answer) {
    if($('#' + getAnswerId(answer.uuid)).length == 0) {
      cloneAnswerTemplate(answer.uuid);
    }
    renderAnswerName(answer.uuid, answer.name);
    renderAnswerScore(answer.uuid, answer.nbVotes);
  };

  var cloneAnswerTemplate = function(answerUuid) {
    var $answer = $('#answerTemplate').clone();
    var answerId = getAnswerId(answerUuid);
    $answer.attr({'style': ''}).attr({'id': getAnswerId(answerUuid)});
    $('#answers').append($answer);
  };

  var renderAnswerName = function(answerUuid, answerName) {
    $('#' + getAnswerId(answerUuid)).find('.answerName').text(answerName);
  };

  var renderAnswerScore = function(answerUuid, nbVotes) {
    var $progressBar = $('#' + getAnswerId(answerUuid)).find('.progress-bar');
    var percentage = Math.floor(nbVotes / nbTotalVotes * 100);
    $progressBar.css('width', percentage + '%');
    var withS = nbVotes <= 1 ? '':'s';
    $progressBar.text(nbVotes + ' vote' + withS + ' (' + percentage + '%)');
    $progressBar.attr('aria-valuenow', percentage);
  };

  var receiveEvent = function(event) {
    var data = JSON.parse(event.data);
    console.log("DATA : ", data);
    if(data.error) {
      socket.close();
    } else {
      update(data);
    }
  };


  init();

})();
