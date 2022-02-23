// The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

const { symbol } = require("./symbol.js");
var yf = require('yahoo-finance');

async function get_latest_quote(s) {
    let data = await yf.quote({
        symbol: s,
        modules: ["price"],
    });

    return data.price.regularMarketPrice;
}

exports.update_coins = functions.pubsub.schedule('every 5 minutes').onRun((context) => {
    const fs = admin.firestore();
    let batch = fs.batch();
    return (async () => {
        // get all documents and read them
        await fs.collection("data").listDocuments().then(docs => {
        return fs.getAll(...docs);
    }).then(async snapshots => {
        for(let ds of snapshots) { 
            // for each document snapshot
            if(ds.exists) { // safe check; this should never enter
                // get the data from the snapshot and process it
                let data = ds.data();
                functions.logger.info(`Got data of ${ds.id}: ${JSON.stringify(data)}`);
                data.historical.push(data.latest);
                functions.logger.info(`Symbol: ${symbol.get(ds.id)}`);
                let quote = await get_latest_quote(symbol.get(ds.id)).then(result => {
                    functions.logger.info(`Finished resolve of yf for ${ds.id}: ${result}`)
                    data.latest = result;
                    functions.logger.info(`Setting data of ${ds.id}: ${JSON.stringify(data)}`);
                    batch.update(ds.ref, data);
                });
            } else {
                functions.logger.error(`Missing document: '${ds.id}'. This should never enter.`);
            }
        }

        return null;
    });
        return batch.commit();
   })(); 
});

exports.man_update_coins = functions.https.onRequest(async (req, res) => {
    const fs = admin.firestore();
    let batch = fs.batch();
    return (async () => {
        await fs.collection("data").listDocuments().then(docs => {
        return fs.getAll(...docs);
    }).then(async snapshots => {
        for(let ds of snapshots) {
            if(ds.exists) {
                let data = ds.data();
                functions.logger.info(`Got data of ${ds.id}: ${JSON.stringify(data)}`);
                data.historical.push(data.latest);
                functions.logger.info(`Symbol: ${symbol.get(ds.id)}`);
                let quote = await get_latest_quote(symbol.get(ds.id)).then(result => {
                    functions.logger.info(`finished resolve of yf: ${result}`)
                    data.latest = result;
                    functions.logger.info(`Setting data of ${ds.id}: ${JSON.stringify(data)}`);
                    batch.update(ds.ref, data);
                });
            } else {
                functions.logger.error(`Missing document: '${ds.id}'. This should never enter.`);
            }
        }

        return null;
    });
        return batch.commit();
}); });
