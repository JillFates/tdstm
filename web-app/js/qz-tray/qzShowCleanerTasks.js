QZ = function(opts){
  var NOP = function(){};

  var onSuccess = opts.onSuccess || NOP;
  var onFail = opts.onFail || NOP;
  var onConnectionError = opts.onConnectionError || NOP;
  var codebase = opts.codebase || ".";
  var signaturePath = opts.signaturePath || ".";

  /// Authentication setup ///
  qz.security.setCertificatePromise(function(resolve, reject) {
    $.ajax(codebase + "/ws/qzCertificate").then(resolve, reject);
  });

  /// Message signature ///
  qz.security.setSignaturePromise(function(toSign) {
    return function(resolve, reject) {
      $.ajax(signaturePath + "?request=" + toSign).then(
        function(data){
          if(data.status == "success") {
            resolve(data.data.signed_message);
          }else{
            logSignError(data, reject);
          }
        },
        function(data){
          logSignError(data, reject);
        }
      );
    };
  });

  function logSignError(data, next){
    var statusText = data.statusText || "";
    next = next || NOP;

    console.error(data);
    onFail("Error while trying to sign message for QZTray");
    next(data)
  }

  /// Connection ///
  function launchQZ() {
    if (!qz.websocket.isActive()) {
      window.location.assign("qz:launch");
      //Retry 5 times, pausing 1 second between each attempt
      startConnection({ retries: 5, delay: 1 });
    }
  }

  function startConnection(config) {
    if (!qz.websocket.isActive()) {
      qz.websocket.connect(config).then(function() {
        //findVersion();
        console.log("QZ-tray Connection started");
        onSuccess();
      }).catch(handleConnectionError);
    }else { 
      console.log("An active connection with QZ already exists");
      onSuccess();
    }
  }

  function endConnection() {
    if (qz.websocket.isActive()) {
      qz.websocket.disconnect().then(function() {
      }).catch(handleConnectionError);
    }
  }

  function listNetworkInfo() {
    qz.websocket.getNetworkInfo().then(function(data) {
      if (data.macAddress == null) { data.macAddress = 'UNKNOWN'; }
      if (data.ipAddress == null) { data.ipAddress = "UNKNOWN"; }

      var macFormatted = '';
      for(var i = 0; i < data.macAddress.length; i++) {
        macFormatted += data.macAddress[i];
        if (i % 2 == 1 && i < data.macAddress.length - 1) {
          macFormatted += ":";
        }
      }

      displayMessage("<strong>IP:</strong> " + data.ipAddress + "<br/><strong>Physical Address:</strong> " + macFormatted);
    }).catch(displayError);
  }

  function isActive(){
    return qz.websocket.isActive();
  }

  function findPrinters(callback) {
    console.log("le find Printers..");
    qz.printers.find().then(function(printers) {
      console.log("Printers found:", printers);
      callback(printers);
    }).catch(displayError);
  }

  function printEPL(opts) {
    var opts = opts || {data:{}};
    var onDone = opts.onDone || NOP;
    var onSuccess = opts.onSuccess || NOP;
    var onFail = opts.onFail || NOP;

    var config = getUpdatedConfig();
    var printer = opts.printerName;
    config.setPrinter(printer);

    // Hint:  Carriage Return = \r, New Line = \n, Escape Double Quotes= \"
    // Change these to adjust the offset of the text if misaligned with the labels
    var offset1 = 180;
    var offset2 = 500;
    var data = opts.data;

    var printData = [
      '\nN\n',
      '\nR0,0\n',  // Reset coordinates
      '\nQ254\n',  // Set label height 1.25"
      '\nD7\n',    // Density 
      'A'+offset1+',0,0,4,1,1,N,"' + data.assetName + '"\n',
      'A'+offset1+',27,0,3,1,1,N,"' + data.modelName + '"\n',
      'A'+offset1+',54,0,3,1,1,N,"Cart: ' + data.cart + '"\n',
      'A'+offset2+',54,0,3,1,1,N,"Shelf: ' + data.shelf + '"\n',
      'A'+offset1+',81,0,3,1,1,N,"Room: ' + data.roomTarget + '"\n',
      'A'+offset1+',108,0,3,1,1,N,"Rack: ' + data.rackTarget + '"\n',
      'A'+offset2+',108,0,3,1,1,N,"UPos: ' + data.targetRackPosition + '"\n',
      'B'+offset1+',130,0,1,3,5,60,B,"' + data.assetTag + '"\n',
      'P' + data.printTimes + ',1\n'
    ];

    qz.print(config, printData).then(function(){
      //return the printer used
      onSuccess({
        getPrinter:function(){
          return printer;
        }
      });
    }).catch(function(err){
      displayError(err);
      onFail(err)
    });
  }

  /// Helpers ///
  function handleConnectionError(err) {
    if (err.target != undefined) {
      if (err.target.readyState >= 2) { //if CLOSING or CLOSED
        displayError("Connection to QZ Tray was closed");
      } else {
        displayError("A connection error occurred, check log for details");
      }
    } else {
      displayError(err);
    }
    onConnectionError(err);
  }

  function displayError(err) {
    console.error(err);
  }

  qz.websocket.setClosedCallbacks(function(evt) {
    console.log(evt);
  });

  qz.websocket.setErrorCallbacks(handleConnectionError);

  /// QZ Config ///
  var cfg = null;
  function getUpdatedConfig() {
    if (cfg == null) {
      cfg = qz.configs.create(null);
    }

    return cfg
  }

//Public API
  window.QZObj = {
    print     : printEPL,
    isActive  : isActive,
    findPrinters : findPrinters
  }

  startConnection();

  return window.QZObj;
}