# general
Java based Reddit bot that recognizes mentions in comments and is able to answer.  
In this use-case image and video dimensions get analysed and the used pixels calculated.  

## keyword recognition
The bot recognizes keywords and picks random, hard coded responses.  
If more than one of such responses appear, Gemma 3 is used to combine them.  
This is not always perfect but improves readability.  

## running
Replace Username and Password in `Credentials.java` with your own and create Client ID/SECRET on https://www.reddit.com/prefs/apps.  
Create a [Gemini API key](https://aistudio.google.com/apikey) to use Gemini answer refinment. Currently this is always enabled.  

## mentions 
This project builds upon [Reddit4J](https://github.com/masecla22/Reddit4J) and uses (Google Gen AI Java SDK)[https://github.com/googleapis/java-genai] to access Gemma.
