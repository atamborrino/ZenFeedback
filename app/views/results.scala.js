@(roomName: String)(implicit request: RequestHeader)

(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var socket = new WS("@routes.Application.connectResultsWS(roomName).webSocketURL()");

  var nbTotalVotes = 0;
  var answers = {};

  /* json event schema:
   {
   name: string,
   uuid: UUID,
   nbVotes: number
   }
   */
  var update = function(answer) {
    answers[answer.uuid] = answer;
    nbTotalVotes += 1;
    render();
  };

  var render = function() {
    $.each(answers, function(uuid, answer) {
      var $progressBar = $('#answer-' + answer.uuid).find('.progress-bar');
      var percentage = Math.floor(answer.nbVotes / nbTotalVotes * 100);
      $progressBar.css('width', percentage + '%');
      var withS = answer.nbVotes <= 1 ? '':'s';
      $progressBar.text(answer.nbVotes + ' vote' + withS + ' (' + percentage + '%)');
      $progressBar.attr('aria-valuenow', percentage);
    });
  };

  var receiveEvent = function(event) {
    var data = JSON.parse(event.data);
    if(data.error) {
      socket.close();
    } else {
      //update(data);
    }
  };

  socket.onmessage = receiveEvent;
})();
