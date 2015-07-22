/*global cordova, module*/

module.exports = {
    runFFMPEG: function (command, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "FFMPEGCommandline", "run", command || []);
    }
};
