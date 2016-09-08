/**
 * Created by octavio on 7/18/16.
 */

//// Set Global Object for Component Reference
QZ = function(opts){
  var NOP = function(){};
  var onSuccess = opts.onSuccess || NOP;
  var onFail = opts.onFail || NOP;
  var codebase = opts.codebase || ".";

  /**
   * check if the applet is loaded
   * @returns true if the applet is loaded, false otherwise
   */
  var isActive = function(){
    if(window.qz){
      return true;
    }else{
      return false;
    }
  };


  function useDefaultPrinter() {
      // Searches for default printer
      qz.findPrinter();

      // Automatically gets called when "qz.findPrinter()" is finished.
      window['qzDoneFinding'] = function() {
        // Alert the printer name to user
        var printer = qz.getPrinter();

        console.log(printer !== null ? 'Default printer found: "' + printer + '"':
                'Default printer ' + 'not found');

        // Remove reference to this function
        window['qzDoneFinding'] = null;
      };
  }

  /**
   * event function that calls a function and pass the list of printers found
   * @param callback function that receives an array of printer names found or a list empty if no print is found
   */
  var findPrinters = function(callback) {
    //if(isActive()) {
      // Automatically gets called when "qz.findPrinter()" is finished.
      window['qzDoneFinding'] = function () {
        // Get the CSV listing of attached printers
        var strPrinters = qz.getPrinters();
        var printers = strPrinters.split(',');
        // Remove reference to this function
        window['qzDoneFinding'] = null;
        callback(printers);
      }

      qz.findPrinter('\\{bogus_printer\\}');
    //}else{
    //  callback([]);
    //}
  };

  function usePrinter(opts) {
    opts = opts || { onSuccess: NOP, onFail:NOP};
    var printerName = opts.printerName;

    if (isActive()) {
      // Automatically gets called when "qz.findPrinter()" is finished.
      window['qzDoneFinding'] = function() {
        // Remove reference to this function
        window['qzDoneFinding'] = null;

        // Alert the printer name to user
        var printer = qz.getPrinter();
        if(printer){
          opts.onSuccess(printer, qz);
        }else{
          opts.onFail('Printer "'+name+'" not Found', qz);
        }
      };

      // Searches for default printer
      qz.findPrinter(printerName);
    }
  }


  /***************************************************************************
   * Prototype function for printing raw EPL commands
   * parameters: JSON object with the folowing info
   * {
   *    printerName: <printer Name to Use>,
   *    data:{
   *      assetName : <assetName>,
   *      assetTag  : <assetTag>,
   *      modelName : <modelName>,
   *      cart      : <cart>,
   *      shelf     : <shelf>,
   *      roomTarget: <roomTarget>,
   *      rackTarget: <rackTarget>,
   *      targetRackPosition: <targetRackPosition>,
   *      printTimes: <printTimes>
   *    },
   *    onSuccess: <function that is call if the print action is successful queued>,
   *    onFail: <function that is called in case of a problem queuing the print job>,
   *    onDone: <function called always regardless of the result of the job>
   * }
   ***************************************************************************/
  var printEPL = function(opts){
    var opts = opts || {data:{}};
    var onDone = opts.onDone || NOP;
    var onSuccess = opts.onSuccess || NOP;
    var onFail = opts.onFail || NOP;

    //If is not active or already waiting for a job
    if(!isActive() || window['qzDonePrinting']) {
      if(typeof qz !== 'undefined') {
        onFail("Not Ready", qz);
      } else  {
        onFail("Barcode applet was not properly loaded.")
      }
      onDone(qz);
      return;
    }

    usePrinter({
      printerName: opts.printerName,
      onFail: onFail,
      onSuccess: function(printer, qz){
        //Add Handler to control printing
        window['qzDonePrinting'] = function() {
          window['qzDonePrinting'] = null;

          // Alert error, if any
          if (qz.getException()) {
            onFail(qz.getException(), qz);
          }else {
            onSuccess(qz);
          }

          // done and remove handler
          onDone(qz);
        }
        // Send characters/raw commands to qz using "append"
        // This example is for EPL.  Please adapt to your printer language
        // Hint:  Carriage Return = \r, New Line = \n, Escape Double Quotes= \"

        // Change these to adjust the offset of the text if misaligned with the labels
        var offset1 = 180;
        var offset2 = 500;

        var data = opts.data;
        qz.append('\nN\n');
        qz.append('\nR0,0\n');  // Reset coordinates
        qz.append('\nQ254\n');  // Set label height 1.25"
        qz.append('\nD7\n');    // Density 
        qz.append('A'+offset1+',0,0,4,1,1,N,"' + data.assetName + '"\n');
        qz.append('A'+offset1+',27,0,3,1,1,N,"' + data.modelName + '"\n');
        qz.append('A'+offset1+',54,0,3,1,1,N,"Cart: ' + data.cart + '"\n');
        qz.append('A'+offset2+',54,0,3,1,1,N,"Shelf: ' + data.shelf + '"\n');
        qz.append('A'+offset1+',81,0,3,1,1,N,"Room: ' + data.roomTarget + '"\n');
        qz.append('A'+offset1+',108,0,3,1,1,N,"Rack: ' + data.rackTarget + '"\n');
        qz.append('A'+offset2+',108,0,3,1,1,N,"UPos: ' + data.targetRackPosition + '"\n');
        qz.append('B'+offset1+',130,0,1,3,5,60,B,"' + data.assetTag + '"\n');
        qz.append('P' + data.printTimes + ',1\n');

        qz.print();
      }
    });
  };

  /**
   * Called after loading the Application Applet
   */

  function qzReady() {
    // Setup our global qz object

    window["qz"] = document.getElementById('qz');
    if (qz) {
      try {
        console.log("I'm ready..");
        onSuccess();
      } catch(err) { // LiveConnect error, display a detailed meesage
        console.log("Exception:", err);
        onFail(
        "ERROR:  \nThe applet did not load correctly.  Communication to the " +
        "applet has failed, likely caused by Java Security Settings.  \n\n" +
        "CAUSE:  \nJava 7 update 25 and higher block LiveConnect calls " +
        "once Oracle has marked that version as outdated, which " +
        "is likely the cause.  \n\nSOLUTION:  \n  1. Update Java to the latest " +
        "Java version \n          (or)\n  2. Lower the security " +
        "settings from the Java Control Panel."
        );
      }
    }
  }

  //Document Wrapper to add the applet after rendering the page and improve UX
  function docWriteWrapper(func) {
    var writeTo = document.createElement('del'),
    oldwrite = document.write,
    content = '';
    writeTo.id = "me";
    document.write = function(text) {
      content += text;
    }
    func();
    writeTo.innerHTML += content;
    document.write = oldwrite;
    document.body.appendChild(writeTo);
  };

  /**
   * Deploys different versions of the applet depending on Java version.
   * Useful for removing warning dialogs for Java 6.  This function is optional
   * however, if used, should replace the <applet> method.  Needed to address
   * MANIFEST.MF TrustedLibrary=true discrepency between JRE6 and JRE7.
   */
  function deployQZ() {
    var pathApplet = codebase + '/qz-print.jar';
    var pathJnlp = codebase + '/qz-print_jnlp.jnlp';

    var attributes = {id: "qz", code:'qz.PrintApplet.class',
      archive: pathApplet, width:1, height:1};
    var parameters = {jnlp_href: pathJnlp,
      cache_option:'plugin', disable_logging:'false',
      initial_focus:'false'};

    if (deployJava.versionCheck("1.7+") == true) {}
    else if (deployJava.versionCheck("1.6+") == true) {
      attributes['archive'] = 'jre6/qz-print.jar';
      parameters['jnlp_href'] = 'jre6/qz-print_jnlp.jnlp';
    }


    docWriteWrapper(function () {
      deployJava.runApplet(attributes, parameters, '1.7');
    });
  };

  //Global declared
  window.qzReady = qzReady;

  //Public API
  window.QZObj = {
    print     : printEPL,
    isActive  : isActive,
    findPrinters : findPrinters
  }

  //Deploy the Applet
  deployQZ();

  return window.QZObj;
};
