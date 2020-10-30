
var products=[]
var myMap=new Map()


function httpGet(theUrl) {

  var xmlHttp = new XMLHttpRequest();
 
  xmlHttp.open("GET", theUrl, false); // false for synchronous request
  xmlHttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
  xmlHttp.setRequestHeader('Access-Control-Allow-Origin', '*');
  xmlHttp.send(null);
  console.log(xmlHttp.responseText);
  return xmlHttp.responseText;


}

function httpPost(url2){
  var http = new XMLHttpRequest();

var url = 'http://localhost:8080/productUrl';
var params = url2;
http.open('POST', url, false);

//Send the proper header information along with the request

http.send(params);

http.onreadystatechange = function() {//Call a function when the state changes.
    if(http.readyState == 4 && http.status == 200) {
        alert(http.responseText);
        
    }
}
return http.responseText;

}


function parseAll() {

  var abc = httpGet("http://localhost:8080/productList");
  console.log("get request")
  var obj = JSON.parse(abc);
 
  for (product in obj.products) {
    var p = obj.products[product];
    products.push(p.title);
    myMap.set(p.title.toLowerCase(), p.url);
  }
  
  console.log(products,myMap)
}




function getProducts(){
  parseAll()
  console.log(products)

   ss()

}

function ss(){

  var substringMatcher = function (strs) {
    console.log(strs)
    return function findMatches(q, cb) {
      
      var matches, substringRegex;
  
      // an array that will be populated with substring matches
      matches = [];
  
      // regex used to determine if a string contains the substring `q`
      substrRegex = new RegExp(q, "i");
  
      // iterate through the pool of strings and for any string that
      // contains the substring `q`, add it to the `matches` array
      $.each(strs, function (i, str) {
        if (substrRegex.test(str)) {
          matches.push(str);
        }
      });
  
      cb(matches);
    };
  };
  $("#the-basics .typeahead").typeahead(
    {
      hint: true,
      highlight: true,
      minLength: 1,
    },
    {
      name: "products",
      source: substringMatcher(products),
    }
  );
}





function findUrl() {
  let title = document.getElementById("inp").value.toLowerCase();
  console.log(title)
  if (myMap[title] === undefined)
    var url2 = myMap.get(title);
    var k=httpPost(url2)
    console.log(k)
    var c = JSON.parse(k);
    addgrp(datePrice(c));
  }

//Data is represented as an array of {x,y} pairs.
function datePrice(data) {
  var list = [];
  for (var i in data.merchants) {
    var priceList = [];
    var obj = { key: data.merchants[i].merchantUrl };
    for (var j in data.merchants[i].prices) {
      priceList.push({
        x: Date.parse(data.merchants[i].prices[j].date),
        y: data.merchants[i].prices[j].price,
      });
    }
    obj.values = priceList;
    list.push(obj);
  }
  return list;
}

function addgrp(lst) {
  console.log(lst)
  
  nv.addGraph(function () {
    var chart = nv.models
      .lineChart()
      .margin({ left: 100 }) //Adjust chart margins to give the x-axis some breathing room.
      .useInteractiveGuideline(true) //We want nice looking tooltips and a guideline!
      
      .showLegend(true) //Show the legend, allowing users to turn on/off line series.
      .showYAxis(true) //Show the y-axis
      .showXAxis(true) //Show the x-axis

      chart.options({height: 500});
    chart.xAxis //Chart x-axis settings
      .axisLabel("Date")
      .tickFormat(function (d) {
        return d3.time.format("%b %d %H:%M:%S")(new Date(d));
      });

    chart.yAxis //Chart y-axis settings
      .axisLabel("Price (p)")
      .tickFormat(d3.format(".02f"));

    /* Done setting the chart up? Time to render it!*/
    var myData = lst; //You need data...

    d3.select("#chart svg") //Select the <svg> element you want to render the chart in.
      .datum(myData) //Populate the <svg> element with chart data...
      .call(chart); //Finally, render the chart!

    //Update the chart when window resizes.
    nv.utils.windowResize(function () {
      chart.update();
    });
    return chart;
  });
}
