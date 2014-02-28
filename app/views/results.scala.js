@(roomName: String)(implicit request: RequestHeader)

(function() {
  var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
  var socket = new WS("@routes.Application.resultsWS(roomName).webSocketURL()");

  var receiveEvent = function(event) {
    var data = JSON.parse(event.data);
    if(data.error) {
      socket.close();
    } else {
      window.game.receiveGameLoopNotif(data);
    }
  };

  socket.onmessage = receiveEvent;
})();
