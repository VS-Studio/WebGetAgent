var page = require('webpage').create();


var web = phantom.args[0];
page.open(web, function() {
  //page.render(web + '.png');
  console.log(page.content);
  phantom.exit();
});
