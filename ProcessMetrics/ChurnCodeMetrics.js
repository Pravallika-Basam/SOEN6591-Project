var fs = require('fs');
const request = require('request');
const cheerio = require('cheerio');
const readline = require('readline');
const opn = require('opn');

const fileContents = fs.readFileSync('./codechurn.txt', 'utf-8');
const fileLines = fileContents.split(/\r?\n/);
const churnesOutCome = [];

for (let i = 0; i < fileLines.length; i++) {
  const line = fileLines[i].trim();
  if (line.match(/\.java/)) {
    const parts = line.split(/\s+/);
    const fileName = parts.pop();
    const churnValue = parts.pop();
    churnesOutCome.push([fileName, churnValue]);
  }
}

let result = '';
for (let i = 0; i < churnesOutCome.length; i++) {
  result += `${churnesOutCome[i][0]},${churnesOutCome[i][1]}\n`;
}

fs.writeFile('churnMetrics.csv', result, function(err) {
  if (err) {
    console.log(err);
  } else {
    console.log('The file was saved!');
  }
});
