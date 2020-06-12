
'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


// ---- MPESA VARIABLES
var access_token;
var base64data;
var dateFormat = require('dateformat');
var day=dateFormat(new Date(), "YYYYMMDDHHmmss");
var data1 = '174379';
var data2 = 'bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919';
var data3 = day;
var buff = new Buffer(data1+data2+data3);
base64data = buff.toString('base64');
var clientID;
var clinet_phoneNumber;
var currtime = Date.now();
var currtimeText=dateFormat(Date.now(), "YYYYMMDDHHmmss");

 // ---------- MPESA  STK REQUEST ------------------
 exports.cashLNM = functions.https.onRequest(async (requestf, _response) => {

  clinet_phoneNumber = requestf.body.client_number;
  clientID = requestf.body.client_id;
 
  var request = require('request'),
  consumer_key = "72YsA1XXN7cJbJxDTTDOFg0R9ShGGIm6",
  consumer_secret = "WNvBe2cexd6jAVo0",
  url = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"
  var auth = "Basic " + new Buffer(consumer_key + ":" + consumer_secret).toString("base64");


  request(
    {
      url : url,
      headers : {
        "Authorization" : auth
      }
    },
    function (error, response, body) {
      // TODO: Use the body object to extract OAuth access token
      // TODO: Use the body object to extract the response

      // ---------- GET TOKEN --------------
      var obj = JSON.parse(body);
      access_token = obj.access_token;
      console.log("Success Token: ",access_token);
      //_response.send(body);

      // --------- SEND STK ----------------

      var request = require('request'),
      oauth_token = access_token,
      url = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
      var auth = "Bearer " + oauth_token;
      
      
      
      //console.log("Time Format: ",day);
             
  
    
      request(
        {
          method: 'POST',
          url : url,
          headers : {
            "Authorization" : auth
          },
        json : {
          "BusinessShortCode": "174379",
          "Password": base64data,
          "Timestamp": day,
          "TransactionType": "CustomerPayBillOnline",
          "Amount": "5",
          "PartyA": clinet_phoneNumber,
          "PartyB": "174379",
          "PhoneNumber": clinet_phoneNumber,
          "CallBackURL": "https://us-central1-buuk-rides.cloudfunctions.net/cashLNMcallback",
          "AccountReference": "UpMeet",
          "TransactionDesc": "FACILITY PAYMENT"
        }
      },
        function (error, response_stk, body) {
          // TODO: Use the body object to extract the response
      
          //console.log("-------------------"); 
          //console.log("STK Body Parameters: ",body);
          //console.log("-------------------"); 
           _response.send(body);
           var prettyjson = require('prettyjson');
            var options = {
              noColor: true
            };

          // C2B ConfirmationURL - /api/v1/c2b/confirmation
          console.log('-----------C2B BODY RESULT------------');
          console.log(prettyjson.render(response_stk.body, options));
          console.log('USERID - ' , clientID);
          console.log('DATE - ' , currtimeText);
          console.log('TIME - ' , currtime);
          console.log('-----------------------');
        
          // ------- STK ERROR ----------------
          if(error){
            console.error("An STK error Occured: ",error)
            _response.send(error);
          }

          // -------- END OF STK ERROR ------------
        }
      )


      // --------- END OF SEND STK ----------

     

      // -------- TOKEN ERROR --------
      if(error){
        console.log("A Token error Occured : ",error)
        _response.send(error);
      }

     
     
  

      // ----- END OF TOKEN ERROR ------------
    }
  )



});
// ----------  MPESA CALLBACK URL ------------------
exports.cashLNMcallback = functions.https.onRequest(async (requestf, _response) => {

  var prettyjson = require('prettyjson');
  var options = {
    noColor: true
  };

  // C2B ConfirmationURL - /api/v1/c2b/confirmation
  console.log('-----------C2B CALLBACK RESULTS------------');
  console.log(prettyjson.render(requestf.body, options));
  console.log('DATE - ' , currtimeText);
  console.log('TIME - ' , currtime);
  console.log('-----------------------------------------------');


  var message = {
    "ResultCode": 0,
    "ResultDesc": "Success"
  };

  _response.json(message);

  var ref = db.ref('MpesaTransactions/' + 'LipaNaMpesa/' + requestf.body.Body.stkCallback.CheckoutRequestID);
  //var refVibarua = db.ref('VibaruaEarnings');
  
    ref.set({
      Time: currtime,
      Date: currtimeText,
      PaymentDesc: "DeliveryPayment",
      ResultCode: requestf.body.Body.stkCallback.ResultCode,
      ResultDesc: requestf.body.Body.stkCallback.ResultDesc,
      MerchantRequestID: requestf.body.Body.stkCallback.MerchantRequestID,
      CheckoutRequestID: requestf.body.Body.stkCallback.CheckoutRequestID,
      TransactionBody: requestf.body.Body.stkCallback,  
  
    });
    
  
          // Attach an asynchronous callback to read the data at our posts reference
    /*refVibarua.on("value", function(snapshot) {

      if(snapshot.child("TotalMpesaEarned").exists && snapshot.child("TotalDueToRiders").exists){
        console.log("Exists");

      var totalMpesaEarned = snapshot.child("TotalMpesaEarned").val();
      var totalDueToRiders = snapshot.child("TotalDueToRiders").val();

      }else{

        //refVibarua.child('TotalMpesaEarned').set();
        //refVibarua.child('TotalDueToRiders').set();
        console.log("Empty");
      }

      //console.log(snapshot.child("TotalMpesaEarned").val());
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });*/
  

  //console.log(_response);

});

/**
 * Triggers when a user gets a new follower and sends a notification.
 *
 * Followers add a flag to `/followers/{OwnerUID}/{CustomerUID}`.
 * Users save their device notification tokens to `/users/{OwnerUID}/notificationTokens/{notificationToken}`.
 */
exports.sendFollowerNotification = functions.database.ref('/Notifications/{OwnerUID}/{CustomerUID}')
    .onWrite(async (change, context) => {
      const CustomerUID = context.params.CustomerUID;
      const OwnerUID = context.params.OwnerUID;
      // If un-follow we exit the function.
      if (!change.after.val()) {
        return console.log('User ', CustomerUID, 'un-followed user', OwnerUID);
      }
      console.log('We have a new notification from Customer UID:', CustomerUID, 'for Venue Owner:', OwnerUID);

      // Get the list of device notification tokens.
      const getDeviceTokensPromise = admin.database()
          .ref(`/Users/${OwnerUID}/notificationTokens`).once('value');

      const getUserName = admin.database()
      .ref(`/Users/${CustomerUID}/username`).once('value');

      // Get the follower profile.
      const getFollowerProfilePromise = admin.auth().getUser(CustomerUID);

      // The snapshot to the user's tokens.
      let tokensSnapshot;

      // The array containing all the user's tokens.
      let tokens;

      const results = await Promise.all([getDeviceTokensPromise, getFollowerProfilePromise, getUserName]);
      tokensSnapshot = results[0];
      const follower = results[1];
      const name = results[2];

      // Check if there are any device tokens.
      if (!tokensSnapshot.hasChildren()) {
        return console.log('There are no notification tokens to send to.');
      }
      
      console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
      console.log('Fetched Customer profile', follower);
      console.log('Customer Name', name);

      // Notification details.
      const payload = {
        notification: {
          title: 'You have a new Facilty Request!',
          body: `${CustomerUID} made a request.`,
          // icon: follower.photoURL
          // body: ` made a request.`,
        }
      };

      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshot.val());
      // Send notifications to all tokens.
      console.log('Token is', tokens);
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });