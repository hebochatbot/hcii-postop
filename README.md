# Hebo


Directories:
- Chatbot - contains index.js file that is deployed as the webhook (hosted on Google Cloud)
- Hebo - contains code for Hebo (Android mobile application)


If you want to work on Hebo's chatbot logic, you can edit intents (training phrases and responses) in Dialogflow. If the response depends on user's profile (e.g. site of surgery, time of surgery), you should use the webhook (remember to turn the fulfillment tab on in Dialogflow and case on the intent name)

If you want to work on the mobile application (e.g. onboarding screens, timer, etc.), visual answer content, and most other things, you probably want to work on files in the Hebo folder.
