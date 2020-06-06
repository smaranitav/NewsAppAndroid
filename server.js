var express = require('express');
var app = express();
var cors = require('cors')
const googleTrends = require('google-trends-api');

function getNYArticles(data, get_all_articles, section) {
  var jsonDataObj={"nyArticles":[]}
  full_json = []
  all_data = JSON.parse(data).results
  console.log(all_data)

  var allSections;
  //console.log(JSON.parse(data).response)

  count = 0

  if (get_all_articles == "yes") {
    total_count = all_data.length
    //allSections = ["world", "business", "sports", "politics", "technology", "us"]
  }
  else {
    //get section articles.i.e limit to 10
    console.log("hell")
    if (section == "politics") {
      allSections = ["us"]
    }
    else {
      allSections = [section]
    }
    total_count = 10
  }
  if(get_all_articles=="yes"){
    for (i = 0; i < all_data.length; i++){
    if (all_data[i].title && all_data[i].title.length != 0 &&
      all_data[i].url && all_data[i].url.length != 0 &&
      all_data[i].abstract && all_data[i].abstract.length != 0 &&
      all_data[i].published_date && all_data[i].published_date.length != 0 &&
      all_data[i].section && all_data[i].section.length != 0 &&
      all_data[i].multimedia

    ) {
      sample_json = {}
      sample_json['newsSrc'] = "NYTimes"
      sample_json['title'] = all_data[i].title
      sample_json['url'] = all_data[i].url
      sample_json['abstract'] = all_data[i].abstract
      sample_json['published_date'] = all_data[i].published_date
      
        sample_json['section'] = all_data[i].section
      sample_json['multimedia'] = all_data[i].multimedia
      jsonDataObj["nyArticles"].push(sample_json);
      //full_json[count] = sample_json
      count = count + 1
    }
  }
      
  }
  else{
  for (i = 0; i < all_data.length; i++) {
    if (count == total_count) {
      break;
    }
    if (allSections.indexOf(all_data[i].section) != -1) {
      //has to be one of those defined sections only
      if (all_data[i].title && all_data[i].title.length != 0 &&
        all_data[i].url && all_data[i].url.length != 0 &&
        all_data[i].abstract && all_data[i].abstract.length != 0 &&
        all_data[i].published_date && all_data[i].published_date.length != 0 &&
        all_data[i].section && all_data[i].section.length != 0 &&
        all_data[i].multimedia

      ) {
        sample_json = {}
        sample_json['newsSrc'] = "NYTimes"
        sample_json['title'] = all_data[i].title
        sample_json['url'] = all_data[i].url
        sample_json['abstract'] = all_data[i].abstract
        sample_json['published_date'] = all_data[i].published_date
        // if (all_data[i].section == "us") {
        //   if (all_data[i].subsection.length != 0) {
        //     sample_json['section'] = all_data[i].subsection
        //   }
        //   else {
        //     sample_json['section'] = "Politics"
        //   }

        // }
        // else {
          sample_json['section'] = all_data[i].section
        //}

        sample_json['multimedia'] = all_data[i].multimedia
        //console.log(all_data[i].multimedia)

        jsonDataObj["nyArticles"].push(sample_json)

        //full_json[count] = sample_json
        count = count + 1

      }
    }
  }
}
  return jsonDataObj;
}
function getSearchNYArticles(data) {
  var jsonDataObj={"nySearch":[]}
  full_json = []
  all_data = JSON.parse(data).response.docs
  //console.log(all_data)
  var i;
  count = 0;

  for (i = 0; i < all_data.length; i++) {
    //has to be one of those defined sections only

    if (all_data[i].headline.main && all_data[i].headline.main.length != 0 &&
      all_data[i].web_url && all_data[i].web_url.length != 0 &&
      all_data[i].abstract && all_data[i].abstract.length != 0 &&
      all_data[i].pub_date && all_data[i].pub_date.length != 0 &&
      all_data[i].section_name && all_data[i].section_name.length != 0 &&
      all_data[i].multimedia

    ) {
      sample_json = {}
      sample_json['newsSrc'] = "NYTimes"
      sample_json['title'] = all_data[i].headline.main
      sample_json['articleData'] = all_data[i].web_url //holds the url
      sample_json['bodySummary'] = all_data[i].abstract
      sample_json['published_date'] = all_data[i].pub_date
      sample_json['section'] = all_data[i].section_name
      sample_json['multimedia'] = all_data[i].multimedia
      jsonDataObj["nySearch"].push(sample_json)
      //console.log(all_data[i].multimedia)

      //full_json[count] = sample_json
      count = count + 1

    }

  }
  return jsonDataObj;


}
function getSearchGuardianArticles(data) {
  var jsonDataObj={"guardianSearch":[]}
  full_json = []
  all_data = JSON.parse(data).response.results
  count = 0

  for (i = 0; i < all_data.length; i++) {
    if (all_data[i].webTitle && all_data[i].webTitle.length != 0 &&
      all_data[i].webUrl && all_data[i].webUrl.length != 0 &&
      all_data[i].blocks &&
      all_data[i].blocks.body &&
      all_data[i].blocks.body[0].bodyTextSummary && all_data[i].blocks.body[0].bodyTextSummary.length != 0 &&
      all_data[i].webPublicationDate && all_data[i].webPublicationDate.length != 0 &&
      all_data[i].sectionId && all_data[i].sectionId.length != 0 &&
      all_data[i].blocks.main &&
      all_data[i].blocks.main.elements[0].assets &&
      all_data[i].id
    ) {
      sample_json = {}
      sample_json['newsSrc'] = "Guardian"
      sample_json['title'] = all_data[i].webTitle
      sample_json['articleData'] = all_data[i].id
      sample_json['webUrl'] = all_data[i].webUrl
      sample_json['bodySummary'] = all_data[i].blocks.body[0].bodyTextSummary
      sample_json['published_date'] = all_data[i].webPublicationDate
      sample_json['section'] = all_data[i].sectionId
      
      if (all_data[i].blocks.main.elements[0].assets.length != 0 &&
        all_data[i].blocks.main.elements[0].assets[all_data[i].blocks.main.elements[0].assets.length - 1].file) {
        sample_json['imgSrc'] = all_data[i].blocks.main.elements[0].assets[all_data[i].blocks.main.elements[0].assets.length - 1].file

      }
      else {
        sample_json['imgSrc'] = 'https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png'
      }
      //full_json[count] = sample_json
      jsonDataObj["guardianSearch"].push(sample_json)
      count = count + 1

    }
  }
  return jsonDataObj;


}

function getGuardianArticles(data, get_all_articles) {
  var jsonDataObj={"guardianArticles":[]}
  full_json = []
  all_data = JSON.parse(data).response.results
  count = 0

  //CHANGED SINCE SUBMIT
  var allSections = ["world", "business", "sport", "politics", "technology","science"]
  if (get_all_articles == "yes") {
    total_count = all_data.length
  }
  else {
    //get section articles.i.e limit to 10
    total_count = 10
  }

  for (i = 0; i < all_data.length; i++) {
    if (count == total_count) {
      break;
    }
    if (allSections.indexOf(all_data[i].sectionId) != -1) {
      //has to be one of those defined sections only
      if (all_data[i].webTitle && all_data[i].webTitle.length != 0 &&
        all_data[i].webUrl && all_data[i].webUrl.length != 0 &&
        all_data[i].blocks &&
        all_data[i].blocks.body &&
        all_data[i].blocks.body[0].bodyTextSummary && all_data[i].blocks.body[0].bodyTextSummary.length != 0 &&
        all_data[i].webPublicationDate && all_data[i].webPublicationDate.length != 0 &&
        all_data[i].sectionId && all_data[i].sectionId.length != 0 &&
        all_data[i].blocks.main &&
        all_data[i].blocks.main.elements[0].assets &&
        all_data[i].id
      ) {
        sample_json = {}
        sample_json['webTitle'] = all_data[i].webTitle
        sample_json['webUrl'] = all_data[i].webUrl
        sample_json['bodyTextSummary'] = all_data[i].blocks.body[0].bodyTextSummary
        sample_json['webPublicationDate'] = all_data[i].webPublicationDate
        sample_json['sectionId'] = all_data[i].sectionId
        sample_json['id'] = all_data[i].id
        sample_json['newsSrc'] = "Guardian"

        if (all_data[i].blocks.main.elements[0].assets.length != 0 &&
          all_data[i].blocks.main.elements[0].assets[all_data[i].blocks.main.elements[0].assets.length - 1].file) {
          sample_json['imgSrc'] = all_data[i].blocks.main.elements[0].assets[all_data[i].blocks.main.elements[0].assets.length - 1].file

        }
        else {
          sample_json['imgSrc'] = 'https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png'
        }
        // full_json[count] = sample_json
        // count = count + 1
        jsonDataObj["guardianArticles"].push(sample_json);
          
        count=count+1

      }
    }
  }
  console.log(full_json)
  console.log(jsonDataObj);

  return jsonDataObj;
}

app.use(cors()) //gives a CORS error if this isnt used

app.get('/nytimes', function (req, res) {
  console.log("Got a GET request for the NY Times");
  url = 'https://api.nytimes.com/svc/topstories/v2/home.json?api-key=wRaNGxN6A3HyS7YhDLXdvpQA7dzUJWA0'
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      //console.log(data)
      //console.log(data.json().length);
      console.log(JSON.parse(data).results)
      full_json = getNYArticles(data, "yes", "all")
      res.send(full_json)
      //res.send(data)
    })
})

app.get('/nytimesSection', function (req, res) {
  console.log("Got a GET request for the NY Times section news");
  url = "https://api.nytimes.com/svc/topstories/v2/" + req.query.sectionName + ".json?api-key=wRaNGxN6A3HyS7YhDLXdvpQA7dzUJWA0"
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      full_json = getNYArticles(data, "no", req.query.sectionName)
      console.log(full_json)
      res.send(full_json)
      // console.log(data)
      // res.send(data)
    })
})

app.get('/guardian', function (req, res) {
  console.log("Got a GET request for the Guardian");
  url = 'https://content.guardianapis.com/search?api-key=282b3586-7838-4aac-94ea-ec6accdf5fb9&section=(sport|business|technology|politics)&show-blocks=all'
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      full_json = getGuardianArticles(data, "yes")
      console.log(full_json)
      res.send(full_json)
    })
})
app.get('/guardianSection', function (req, res) {
  url = "https://content.guardianapis.com/" + req.query.sectionName + "?api-key=282b3586-7838-4aac-94ea-ec6accdf5fb9&show-blocks=all"
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      //console.log(JSON.parse(data).response.results);
      full_json = getGuardianArticles(data, "no")
      console.log(full_json)
      res.send(full_json)
    })
})

app.get('/GuardianDetailedArticle', function (req, res) {
  console.log("inside detailed article of guardian")
  url = "https://content.guardianapis.com/" + req.query.articleId + "?api-key=282b3586-7838-4aac-94ea-ec6accdf5fb9&show-blocks=all"
  //console.log(req.query.articleId)
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.json())
    .then(data => {
      console.log("Came here")
      res.send(data)
    })
})

app.get('/NYDetailedArticle', function (req, res) {
  console.log("inside detailed article of NY")
  console.log(req.query.articleUrl)
  url = "https://api.nytimes.com/svc/search/v2/articlesearch.json?fq=web_url:(" + "\"" + req.query.articleUrl + "\")&api-key=wRaNGxN6A3HyS7YhDLXdvpQA7dzUJWA0"
  //console.log(req.query.articleId)
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.json())
    .then(data => {
      console.log("Came here")
      res.send(data)
    })
})

app.get('/searchGuardian', function (req, res) {
  console.log("inside search article of Guardian")
  url = "https://content.guardianapis.com/search?q=" + req.query.queryKeyword + "&api-key=282b3586-7838-4aac-94ea-ec6accdf5fb9&show-blocks=all"
  //url=""+req.query.queryKeyword
  //console.log(req.query.articleId)
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      //console.log(data);
      full_json = getSearchGuardianArticles(data)
      //console.log(full_json)
      res.send(full_json)
      //console.log("Came here")
      //res.send(data)
    })
})

app.get('/searchNY', function (req, res) {
  console.log("inside search article of NY times")
  url = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=" + req.query.queryKeyword + "&api-key=wRaNGxN6A3HyS7YhDLXdvpQA7dzUJWA0"
  // url=""+req.query.queryKeyword
  //console.log(req.query.articleId)
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.text())
    .then(data => {
      //console.log(data);
      full_json = getSearchNYArticles(data)
      //console.log(full_json)
      res.send(full_json)
      // console.log("Came here")
      // res.send(data)
    })
})

app.get('/showLatestGuardian', function (req, res) {
  console.log("inside search article of Guardian")
  //url = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=" + req.query.queryKeyword + "&api-key=wRaNGxN6A3HyS7YhDLXdvpQA7dzUJWA0"
  // url=""+req.query.queryKeyword
  //console.log(req.query.articleId)
  url="http://content.guardianapis.com/search?order-by=newest&show-fields=starRating,headline,thumbnail,short-url&api-key=7069af5b-656c-4300-8def-6eda8dbbf700"
  const fetch = require('node-fetch');
  fetch(url)
    .then(response => response.json())
    .then(data => {
      //full_json=showLatestGuardianNews(data)
      console.log(data)
      res.send(data)
    })
})

//var date=new Date(2019,5,1)  //5 because date starts month from 0 to 11 and format for date object is Date(year,month,day)
app.get('/showTrends', function (req, res) {

  console.log(req.query.searchKeyword)
  googleTrends.interestOverTime({keyword: req.query.searchKeyword,startTime:new Date('2019-06-01')})
  .then(function(results){
    console.log(results);
    res.send(results)
  })
  .catch(function(err){
    console.error(err);
  });

})

const port= process.env.PORT || 9000;
var server = app.listen(port, function () {
  // var host = server.address().address
  // var port = server.address().port
  // console.log(host)
  console.log("Example app listening");
})  
