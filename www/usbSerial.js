/*global cordova*/
module.exports = {

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "USBSerial", "connect", [macAddress]);
    },

    // Android only - see http://goo.gl/1mFjZY
    connectInsecure: function (macAddress, success, failure) {
        cordova.exec(success, failure, "USBSerial", "connectInsecure", [macAddress]);
    },

    disconnect: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "disconnect", []);
    },

    // list bound devices
    list: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "list", []);
    },

    isEnabled: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "isEnabled", []);
    },

    isConnected: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "isConnected", []);
    },

    // the number of bytes of data available to read is passed to the success function
    available: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "available", []);
    },

    // read all the data in the buffer
    read: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "read", []);
    },

    // reads the data in the buffer up to and including the delimiter
    readUntil: function (delimiter, success, failure) {
        cordova.exec(success, failure, "USBSerial", "readUntil", [delimiter]);
    },

    // writes data to the bluetooth serial port - data must be a string
    write: function (data, success, failure) {
        cordova.exec(success, failure, "USBSerial", "write", [data]);
    },

    // calls the success callback when new data is available
    subscribe: function (delimiter, success, failure) {
        cordova.exec(success, failure, "USBSerial", "subscribe", [delimiter]);
    },

    // removes data subscription
    unsubscribe: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "unsubscribe", []);
    },

    // clears the data buffer
    clear: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "clear", []);
    },

    // reads the RSSI of the *connected* peripherial
    readRSSI: function (success, failure) {
        cordova.exec(success, failure, "USBSerial", "readRSSI", []);
    }

};
