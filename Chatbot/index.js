const FORTY_EIGHT_HOURS_LATER = 2; // days

/*
* HTTP Cloud Function.
*
* @param {Object} req Cloud Function request context.
* @param {Object} res Cloud Function response context.
*/
exports.heboHttp = function heboHttp (req, res) {
    console.log(req.body);
    let response; // the response we'll return
    let timeOfSurgery;

    const currentTime = req.body.timestamp;
    const intentName = req.body.result.metadata.intentName;
    const contexts = req.body.result.contexts;
    let params = req.body.result.parameters;

    switch (intentName) {
        case "how-should-i-secure-the-dressing - yes":
            params = contexts[0].parameters;
            timeOfSurgery = new Date(params["date-time"]);
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "This is the example intent. Don't shower until 48 hours after you surgery, which would be " + 
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString();
            break;
        case "other-example-intent":
            timeOfSurgery = new Date(params["date-time"]);
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "This is the other example intent. Don't shower until 48 hours after you surgery, which would be " + 
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString();
            break;
        default:
            response = "I'm sorry, I don't think I know the answer to that question. I can only answer questions on bleeding, swelling, and wound care."
    }

  
    res.setHeader('Content-Type', 'application/json'); //Requires application/json MIME type
    res.send(JSON.stringify({ "speech": response, "displayText": response 
    //"speech" is the spoken version of the response, "displayText" is the visual version
    }));
};