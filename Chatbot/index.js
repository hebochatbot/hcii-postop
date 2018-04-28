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
    let response, speech; // the response we'll return
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
        speech = "Here are instructions on how to apply pressure."
        response = "(APPLY_PRESSURE_RESPONSE)";
        break;

      // DIALOG:
      // Can I change my dressing?
      // Is it still bleeding? Yes.
      // You should apply pressure instead. How?
      case "can-i-change-dressing - yes - how":
        speech = "Here are instructions on how to apply pressure."
        response = "(APPLY_PRESSURE_RESPONSE)";
        break;

      // DIALOG:
      // Can I change my dressing?
      // Is it still bleeding? No.
      // You should change only if after 48 hours...
      case "can-i-change-dressing - no":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = speech = "You can change your dressing now. You should be changing your dressing daily or when it gets wet.";
        } else {
          response = speech = "I recommend you wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      // DIALOG:
      // Can I shower?
      case "can-i-shower":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = speech = "You can let water run over your " + surgeryArea + ", but you should avoid putting it" +
                       " directly in a stream of water. I always recommend a bath over a shower so that you can avoid getting your " + surgeryArea + " wet.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = speech = "Please avoid getting the wound and dressing wet for now. Wait until 48 hours after your surgery, " +
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + ". When that time comes, let me know if you need any help!";
        }
        break;

      case "do-i-do-the-same-dressing-as-my-doctor":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = speech = "When you change your dressing at home, it will look smaller than the pressure dressing that your doctor used.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = speech = "When you change your dressing at home, it will look smaller than the pressure dressing that your doctor used. Be sure to wait until " +
                        timeOfSurgery.toDateString() + " at " + timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      // DIALOG:
      // Do i do the same dressing that doc did?
      // No do normal dressing. How?
      case "do-i-do-the-same-dressing-as-my-doctor - how - bandage":
        speech = "Here is how you should change your dressing with a bandage."
        response = "(CHANGE_BANDAGE_RESPONSE)";
        break;

        case "do-i-do-the-same-dressing-as-my-doctor - how - gauze":
        speech = "Here is how you should change your dressing with gauze. You will also need non-stick dressing, such as Telfa"
        response = "(CHANGE_GAUZE_RESPONSE)";
        break;


      case "dry-blood-stuck-to-dressing":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
            response = speech = "If your bandage is stuck to your wound, try applying a damp cloth to the area to help loosen the bandage. It's often better to have a bandage that's stuck than to pull too hard and potentially cause bleeding.";
        } else {
            timeOfSurgery.setDate(timeOfSurgery.getDate() + FORTY_EIGHT_HOURS_LATER);
            response = speech = "I recommend you wait until " + timeOfSurgery.toDateString() + " at " +
                       timeOfSurgery.toLocaleTimeString() + " to change your dressing. Once 48 hours has passed, you can use a damp cloth to help loosen your bandage, if it's still stuck.";
        }
        break;

      case "help":
        if (upcomingSurgery(timeOfSurgery, currentTime)) {
          response = speech = "Hi there! I can help you prepare for your surgery! I can answer questions about your surgery that relate to wound care, bleeding, or swelling. You can ask me a question like 'Will I be able to shower?'";
        } else {
          var exampleQuestions = ["When can I change my dressing?", "How do I change my dressing?", "Can I shower?", "How do I clean my wound?", "How can I avoid scarring?", "What if my dressing gets wet?"];
          response = speech = "Hi there! Hope your recovery is going well! I can answer questions about your surgery that relate to wound care, bleeding, or swelling. You can ask me a question like '" + exampleQuestions[Math.floor(Math.random() * exampleQuestions.length)] + "'";
        }
        break;

      case "how-can-i-stop-my-bleeding - how": //TODO: Timing
        // join this together with i am still bleeding and have a variable to track the last time the user said that to see if multiple times in a few hours
        speech = "Here is how you can stop the bleeding."
        response = "(APPLY_PRESSURE_RESPONSE)";
        break;

      case "how-do-i-change-my-dressing - bandage":
        speech = "Here's how to change your dressing with a bandage.";
        response = "(CHANGE_BANDAGE_RESPONSE)";
        break;

        case "how-do-i-change-my-dressing - gauze":
        speech = "Here's how to change your dressing with gauze. You will also need Telfa or any non-stick dressing.";
        response = "(CHANGE_GAUZE_RESPONSE)";
        break;

      // DIALOG:
      // You should put a lot of vaseline. Would you like me to show you how? Yes
      case "how-do-i-keep-wound-from-drying-out - yes":
        speech = "Here's how much vaseline you should apply."
        response = "(VASELINE_RESPONSE)";
        break;

      // DIALOG:
      // You should put a lot of vaseline. Would you like me to show you how? Yes
      case "how-much-vaseline - yes":
        speech = "Here's how much vaseline you should apply. The more the merrier!"
        response = "(VASELINE_RESPONSE)";
        break;

      case "how-should-i-apply-pressure":
        speech = "Here are instructions on how to apply pressure."
        response = "(APPLY_PRESSURE_RESPONSE)";
        break;

      // DIALOG:
      // You should put a lot of vaseline to minimize scarring. Would you like me to show you how? Yes
      case "how-to-reduce-scarring - yes":
        speech = "This is how much vaseline you should apply."
        response = "(VASELINE_RESPONSE)";
        break;

      case "is-it-ok-to-bleed-through-dressing":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = speech = "It is normal to bleed within the first two weeks. If it has been after two weeks, you should apply pressure to the wound and call your doctor if the bleeding hasn't stopped after 30 minutes."
        } else {
          response = speech = "Bleeding is common in the first 48 hours. You should use a dark towel to apply pressure for 30 minutes. This will help stop the bleeding.";
        }
        break;

      // DIALOG:
      // What if...?
      // Is it wet from blood? yes.
      // has the bleeding stopped? yes.
      case "What-if-my-dressing-gets-wet? - yes - yes":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = speech = "You should change your dressing and clean the wound area.";
        } else {
          response = speech = "Great! It's best to leave your dressing alone. Wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      // DIALOG:
      // What if...?
      // Is it wet from blood? no.
      case "What-if-my-dressing-gets-wet? - no":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = speech = "You should replace your dressing whenever it gets wet.";
        } else {
          response = speech = "It's best to leave your dressing alone. For now you can try to pat it dry. Wait until " + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + " to change your dressing.";
        }
        break;

      case "what-should-i-do-if-the-dressing-is-falling-off":
        if (surgeryArea === "forehead" || surgeryArea === "scalp" || surgeryArea === "back of neck") {
            response = speech = "I see that your surgery was on your " + surgeryArea + ". If you have hair, it might be getting in the way " +
                        "of the bandage. Try to secure the tape on the skin under your hair. You can also try wearing a hat " +
                        "or a scarf to keep the dressing in place.";
        } else {
            response = speech = "You can try adding more tape if your dressing is coming loose.";
        }
        break;

      case "when-can-i-change-dressing":
        if (isAfterFortyEightHours(timeOfSurgery, currentTime)) {
          response = speech = "You can change your dressing now. You should be changing your dressing daily or when it gets wet.";
        } else {
          response = speech = "You can change your dressing on" + timeOfSurgery.toDateString() + " at " +
                     timeOfSurgery.toLocaleTimeString() + ".";
        }
        break;

      default:
        response = speech = "I'm sorry, I don't think I know the answer to that question. I can only answer questions on bleeding, swelling, and wound care."
    }


    res.setHeader('Content-Type', 'application/json'); //Requires application/json MIME type
    res.send(JSON.stringify({ "speech": speech, "displayText": response
    //"speech" is the spoken version of the response, "displayText" is the visual version
    }));
};
