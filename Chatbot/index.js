const FORTY_EIGHT_HOURS_LATER = 2; // days
const FORTY_EIGHT_HOURS_IN_MS = 172800000;


const isAfterFortyEightHours = (timeOfSurgery, timeOfNow) => {
    const timeElapsed = timeOfNow - timeOfSurgery;
    return timeElapsed >= FORTY_EIGHT_HOURS_IN_MS;
}

const upcomingSurgery = (timeOfSurgery, timeOfNow) => {
    const timeElapsed = timeOfNow - timeOfSurgery;
    return timeElapsed < 0;
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

      // DIALOG
      // Can I change my dressing?
      // Is it still bleeding? I'm not sure.
      // Let's check - is it? Yes.
      // You should apply pressure instead. How?
      case "can-i-change-dressing - unsure - yes - how":
        console.log("visual answer for how to apply pressure");
        break;

      // DIALOG:
      // Can I change my dressing?
      // Is it still bleeding? Yes.
      // You should apply pressure instead. How?
      case "can-i-change-dressing - yes - how":
        console.log("visual answer for how to apply pressure");
        break;

      // DIALOG:
      // Can I change my dressing?
      // Is it still bleeding? No.
      // You should change only if after 48 hours...
      case "can-i-change-dressing - no":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = "You can change your dressing now. You should be changing your dressing daily or when it gets wet.";
        } else {
          response = "I recommend you wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      // DIALOG:
      // Can I shower?
      case "can-i-shower":
        response = "You can bathe; the important thing is to not get the dressing wet. I always recommend a bath over a shower so that you can avoid getting your " + surgeryArea + " wet.";
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = "You can let water run over your " + surgeryArea + ", but you should avoid putting it" +
                       " directly in a stream of water.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "Please avoid getting the wound and dressing wet for now. Wait until 48 hours after your surgery, " +
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ". When that time comes, let me know if you need any help!";
        }
        break;

      case "do-i-do-the-same-dressing-as-my-doctor":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = "When you change your dressing at home, it will look smaller than the pressure dressing that your doctor used.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "When you change your dressing at home, it will look smaller than the pressure dressing that your doctor used. Be sure to wait until " +
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      // DIALOG:
      // Do i do the same dressing that doc did?
      // No do normal dressing. How?
      case "do-i-do-the-same-dressing-as-my-doctor - how":
        console.log("Show the visual answer of how to change your dressing.");
        break;


      case "dry-blood-stuck-to-dressing":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = "If your bandage is stuck to your wound, try applying a damp cloth to the area to help loosen the bandage. It's often better to have a bandage that's stuck than to pull too hard and potentially cause bleeding.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "I recommend you wait until " + timeOfSurgery.toDateString() + " at " +
                       timeOfSurgery.toLocaleTimeString() + " to change your dressing. Once 48 hours has passed, you can use a damp cloth to help loosen your bandage, if it's still stuck.";
        }
        break;

      case "help":
        if (upcomingSurgery(timeOfSurgery, currentTime)) {
          response = "Hi there! I can help you prepare for your surgery! I can answer questions about your surgery that relate to wound care, bleeding, or swelling. You can ask me a question like 'Will I be able to shower?'";
        } else {
          var exampleQuestions = ["When can I change my dressing?", "How do I change my dressing?", "Can I shower?", "How do I clean my wound?", "How can I avoid scarring?", "What if my dressing gets wet?"];
          response = "Hi there! Hope your recovery is going well! I can answer questions about your surgery that relate to wound care, bleeding, or swelling. You can ask me a question like '" + exampleQuestions[Math.floor(Math.random() * exampleQuestions.length)] + "'";
        }
        break;
      case "how-can-i-shower":
        break;
      case "how-can-i-stop-my-bleeding":
        // join this together with i am still bleeding and have a variable to track the last time the user said that to see if multiple times in a few hours
        break;
      case "how-do-i-change-my-dressing":
        break;
      case "how-do-i-keep-wound-from-drying-out":
        break;
      case "how-do-i-put-dressing-on-unseen-area":
        break;
      case "how-much-vaseline":
        break;
      case "how-often-can-i-change-my-dressing":
        break;
      case "how-to-clean-my-wound":
        break;
      case "how-to-reduce-scarring":
        break;
      case "is-bleeding-normal-right-now":
        break;
      case "is-it-ok-to-bleed-through-dressing":
        break;
      case "is-scabbing-normal":
        break;
      case "my-wound-is-itchy":
        break;
      case "ran-out-of-gauze":
        break;
      case "what-if-dressing-gets-wet":
        break;
      case "what-if-wound-hurts":
        break;
      case "what-level-of-bleeding-is-normal":
        break;
      case "what-should-i-do-if-the-dressing-is-falling-off":
        break;
      case "when-can-i-change-dressing":
        break;
      case "why-cant-i-get-the-wound-wet":
        break;
      case "wound-swelling":
        break;
      case "wound-swelling-rapidly":
        break;



        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = "You can let water run over your " + surgeryArea + ", but you should avoid putting it" +
                       " directly in a stream of water.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = "Please avoid getting the wound and dressing wet for now. Wait until 48 hours after your surgery, " +
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ". You can ask me again then for help.";
        };
        break;




      case "can-i-change-my-dressing":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = "You should be changing your dressing daily or when it gets wet.";
        } else {
          response = "I recommend you wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;
      case "Can-I-change-my-wound-dressing-if-it-is-before-48 hours-but-is-it-bleeding-through-my-dressing?":
        // TO DO
        console.log("This is where we show the visual picture for pressure right? halp.")
        break;
      case "can-i-shower":
        response = "You can bathe; the important thing is to not get the dressing wet. We recommend a bath so that you can avoid getting your " + surgeryArea + " wet.";
        break;
      case "Do-I need-to-change-my dressing-if-it-is-bleeding-through-it? - no":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = "You can go ahead and change your dressing. If your wound is sticking to your dressing, you can try to use a damp cloth to help loosen it.";
        } else {
          response = "Don't worry about changing your dressing now! You should wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to replace your pressure dressing.";
        }
        break;
      default:
        response = "I'm sorry, I don't think I know the answer to that question. I can only answer questions on bleeding, swelling, and wound care."







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
