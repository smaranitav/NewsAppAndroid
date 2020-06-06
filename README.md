# NewsAppAndroid
Worked on developing an Android application for displaying and sharing Guardian news on social media, displaying trending news and search functionality to look up for keywords in the news. All HTTP requests are made from the Android application to the backend Node.js code, deployed on AWS.

# Features
1. Displays the latest news from Guardian News API and also displays the top headlines according to categories such as :
 a. Business
 b. Technology
 c. World
 d. Politics
 e. Science
 f. Sports 
2. Search functionality is implemented using Bing AutoSuggest API where the user is provided with auto-suggestions of keywords and when the user selects a keyword, it displays the news results pertaining/relevant to the search keyword. 
3. Google Trends feature for trending news is implemented in a graph structure which shows the trending graph of the keyword in the news in the past few days.
4. Use of OpenWeatherMap API for fetching the current weather condition, temperature based on a user's location in real-time.
5. News can be shared via Twitter in real-time from the application.
6. The news articles can be bookmarked and accessed for future references , as well as removed from bookmarks if not required. 

# Client server architecture
1. Client - Android application
2. Server - Node.js code named server.js deployed on AWS

Client makes HTTP requests asynchronously using Volley to the server and the server makes the API calls and returns response back to the client. 


