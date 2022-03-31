// The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
const yf2 = require('yahoo-finance2').default; // NOTE the .default
const { symbol } = require("./symbol.js");

let to_yf_symbol = ((s) => s.toUpperCase() + "-USD");


exports.update_coins = functions.pubsub.schedule('every 5 minutes').onRun((context) => {
    const fs = admin.firestore();
    let batch = fs.batch();
    return (async () => {
        await fs.collection("data").listDocuments().then(docs => {
        return fs.getAll(...docs);
    }).then(async snapshots => {
        let proms = [];
        for(let ds of snapshots) {
            if(ds.exists) {
                let data = ds.data();
                proms.push(yf2.quote(to_yf_symbol(ds.id)).then((latest_quote) => {
                    let lq = latest_quote.regularMarketPrice;
                    functions.logger.info(`For coin ${data.name}: ${data.latest} ->  ${lq}`);
                    let old_quote = data.latest;
                    proms.push(fs.getAll(data.historical).then(res => {
                        let hds = res[0];
                        functions.logger.info(`Got historical of ${hds.id}!`);
                        let hdata = hds.data();
                        hdata.historical.push(old_quote);
                        batch.update(hds.ref, hdata);
                    }));
                    data.latest = lq;
                    batch.update(ds.ref, data);
                }));
            } else {
                functions.logger.error(`Missing document: '${ds.id}'. This should never enter.`);
            }
        }
        return Promise.all(proms).then(r => batch.commit());
    });
   })(); 
});
