const FORTY_EIGHT_HOURS_LATER = 2; // days
const FORTY_EIGHT_HOURS_IN_MS = 172800000;


const isAfterFortyEightHours = (timeOfSurgery, timeOfNow) => {
    const timeElapsed = timeOfNow - timeOfSurgery;
    return timeElapsed >= FORTY_EIGHT_HOURS_IN_MS;
}

/*
* HTTP Cloud Function.
*
* @param {Object} req Cloud Function request context.
* @param {Object} res Cloud Function response context.
*/
exports.heboHttp = function heboHttp (req, res) {
    console.log(req.body);
    let response; // the response we'll return
    let params = req.body.result.parameters;
    const currentTime = new Date(req.body.timestamp);
    const intentName = req.body.result.metadata.intentName;
    const contexts = req.body.result.contexts;
    const timeOfSurgery = new Date(params["date-time"]);;
    const surgeryArea = params["body-parts"];

    switch (intentName) {
        case "how-should-i-secure-the-dressing - yes":
            params = contexts[0].parameters;
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "This is the example intent. Don't shower until 48 hours after you surgery, which would be " + 
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString();
            break;
        case "other-example-intent":
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "This is the other example intent. Don't shower until 48 hours after you surgery, which would be " + 
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString();
            break;
        case "how-often-can-i-change-my-dressing":
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "You can change your dressing as often as you'd like. We recommend changing it at least once a day.";
            } else {
                timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
                response = "You should wait 48 hours after your surgery before changing your dressing, which would be " +
                            timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ".";
            };
            break;
        case "can-i-change-my-dressing":
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "Yes, you should change your dressing daily or when it gets wet.";
            } else {
                timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
                response = "No, you should wait until " + timeOfSurgery.toDateString() + " at " +
                           timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
            };
            break;
        case "when-can-i-change-my-dressing":
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "You should change your dressing once a day or whenever it gets wet.";
            } else {
                timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
                response = "You can change your dressing 48 hours after your surgery. In your case, you can change after " +
                            timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ".";
            };
            break;
        case ("how-should-i-secure-the-dressing"):
        case ("dressing-keeps-falling-off"):
            if (surgeryArea === "forehead" || surgeryArea === "scalp" || surgeryArea === "back of neck") {
                response = "I see that your surgery was on your " + surgeryArea + ". If you have hair, it might be getting in the way " +
                            "of the bandage. Try to secure the tape on the skin under your hair. You can also try wearing a hate " +
                            "or a scarf to keep the dressing in place.";
            } else {
                response = "You can try adding more tape if your dressing is loose.";
            }
            break;
        case ("can-i-wash-body"):
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "Yes, but actually since it has been 48 hours after your surgery, you can get your dressing wet if you change it after.";
            } else {
                response = "Yes, the important thing is to not get the dressing wet. We recommend a bath so that you can avoid getting your " +
                surgeryArea + " wet.";
            };
            break;
        case ("how-can-i-shower"):
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "You can let water run over your " + surgeryArea + ", but you should avoid putting it" + 
                           " directly in a stream of water.";
            } else {
                timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
                response = "Please avoid getting the wound and dressing wet for now. Wait until 48 hours after your surgery, " + 
                            timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ". You can ask me again then for help.";
            };
            break;
        case ("what-should-i-do-if-blood-sticks-to-dressing"):
            if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
                response = "Let the dressing soak in the shower before removing it. When you redress the bandage, you should " +
                           "increase the amount of vaseline applied and use non-stick pads.";
            } else {
                timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
                response = "Please don't try to change the dressing. You should wait until " + 
                            timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + " to change your dressing. " +
                            "Please ask me then if the blood is still sticking to your dressing.";
            };
            break;
        default:
            response = "I'm sorry, I don't think I know the answer to that question. I can only answer questions on bleeding, swelling, and wound care."
    }

  
    res.setHeader('Content-Type', 'application/json'); //Requires application/json MIME type
    res.send(JSON.stringify({ "speech": response, "displayText": response 
    //"speech" is the spoken version of the response, "displayText" is the visual version
    }));
};