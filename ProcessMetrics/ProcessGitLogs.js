var fs = require('fs');
const request = require('request');
const cheerio = require('cheerio');
const readline = require('readline');
const opn = require('opn');

require.extensions['.txt'] = function (module, filename) {
    module.exports = fs.readFileSync(filename, 'utf8');
};

var defects = [];

var threeCommits = require("./logs.txt").toString();
var output = threeCommits.split(/commit([\s\S]+?)commit/)
output.splice(0, 1);

for (var i = 0; i < output.length; i++){
  var lines;
  var filenames = [];
  var defect = [];

  lines = output[i].split('\n');
  lines.shift();
  for(var j = 0;j < lines.length;j++){
    
    if (lines[j] == '') {
      lines.splice(i, 1);
    }
    
    if (/^\s|\s$/.test(lines[j])) {
    lines[j]= lines[j].replace(/^\s+|\s+$/g,'')
    }

  if (/Author/.test(lines[j])) {
          var obj= /<(.*)>/.exec(lines[j]);
            if(obj != null ){
          var author = obj[1].toString();

          defect.push(author);
        }
      }
      if (/Date:\s+(.*)/i.test(lines[j])) {
    obj = /Date:\s+(.*)/i.exec(lines[j]);
    if (obj != null) {
      var date = obj[0];
      defect.push(date);
    }
  }

    if (/\w*-\w*\./.test(lines[j])) {
      var obj = lines[j].match(/([A-Z]+-\d+)/);
      if(obj !==null ){
      var commitId = obj[0];
      defect.push(commitId);
      }
    }

    if (/([^\/]+)\.java/.test(lines[j])) {
      var obj = /([^\/]+)\.java/.exec(lines[j]);
      if(obj !=null ){
      var filename = obj[0];
      filenames.push(filename);
      }
    }
  }
  defect.push(filenames);
  defects.push(defect)
}

var urlAndAssociatedFiles = [];


for (var i = 0; i < defects.length; i++) {
  var defectIdarry = defects[i];
  if (defectIdarry.length < 4) {
  }
  var defectId = defectIdarry[2];
  var associateFiles = defectIdarry[3];
  if (  typeof defectId === 'string' && associateFiles.length != 0) {
    var defectUrl = 'https://issues.apache.org/jira/si/jira.issueviews:issue-xml/' + 
      defectId + '/' + defectId +'.xml';
    var temp = [];
    temp.push(defectUrl);
    temp.push(associateFiles);
    urlAndAssociatedFiles.push(temp);
  }
  
}

var filesWithBug =[];
processArray(urlAndAssociatedFiles);

async function processArray(array) {
  let promises = array.map(item => {
    return new Promise((resolve, reject) => {
      request(item[0], (error, response, html) => {
        if (error) {
          reject(error);
        } else if (response.statusCode !== 200) {
          reject(new Error(`Invalid status code: ${response.statusCode}`));
        } else {
          const $ = cheerio.load(html);
          $title = $('title').text();
          $type = $('type').text();
          console.log("Type is ",$type);
          if ($type == 'Bug' ) {
            $resolution = $('resolution').text();
            if ($resolution == 'Fixed') {
              $resolved = $('resolved').text();
              var mydate = new Date($resolved);
              var releaseDate = new Date('Jan 01 2017');
              if (mydate < releaseDate) {
                resolve(item[1]);
                console.log("Bug Found!!");
              } else {
                resolve(null);
              }
            } else {
              resolve(null);
            }
          } else {
            resolve(null);
          }
        }
      });
    });
  });

  let results = await Promise.all(promises);
  let filesWithBug = results.filter(file => file !== null);

  console.log("files: " + filesWithBug.length);
  fs.writeFile("PreReleaseDefects.txt", filesWithBug, function(err) {
    if(err) {
      return console.log(err);
    }
    console.log("The file was saved!");
  });
}