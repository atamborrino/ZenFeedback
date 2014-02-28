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
    updateAnswers(data.answers);
    // Il faut que je reçoive le nombre total de votes
    // nbTotalVotes += 1;
  };

  var updateQuestion = function(question) {
    if (question !== undefined) {
      $('.questionName').text(question.name);
    }
  };

  var updateAnswers = function(answers) {
    console.log("answers : ", answers);
    if (answers !== undefined) {
      for (var i = 0; i < answers.length; i++) {
        updateAnswer(answers[i]);
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
    console.log("UUID : ", answer.uuid, getAnswerId(answer.uuid));
    renderAnswerName(answer.uuid, answer.name);
    renderAnswerScore(answer.uuid, answer.nbVotes);
  };

  var cloneAnswerTemplate = function(answerUuid) {
    var $answer = $('#answerTemplate').clone();
    var answerId = getAnswerId(answerUuid);
    console.log("ANSWERID : ", answerId);
    $answer.attr({'style': ''}).attr({'id': getAnswerId(answerUuid)});
    console.log("ANSWER  :" ,$answer);
    $('#answers').append($answer);
  };

  var renderAnswerName = function(answerUuid, answerName) {
    console.log("answerName :", answerName);
    console.log("$answerName : ", $('#' + getAnswerId(answerUuid)));
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
    console.log("EVENT : ", event);
    var data = JSON.parse(event.data);
    if(data.error) {
      socket.close();
    } else {
      update(data);
    }
  };


  init();

})();
