$(function() {
  var link = "https://api.github.com/repos/MathewSachin/Fate-Grand-Automata/releases"
  $.get(link, function(data) {
    var releases = data
    // We include pre-releases here
    var latest = releases[0]

    var asset = latest.assets[0]
    var downloadLink = asset.browser_download_url

    $("#download-btn").attr('href', downloadLink)
    $('#version-txt').text(latest.tag_name)
  })
})