var show_alert = function(selector, message)
{
  $(".alert-details", selector).html(message || "")
  selector.show();
  setTimeout(function(){
    selector.hide();
    $("#report").show()
  }, message ? 4000 : 1000)
}

var zero_pad = function(num) {
  return ("000"+num).slice(-3);
}

var extract_glyphs = function(page_id)
{
  // returns array of glyph objects
  // each glyph object contains sura_id, aya_id & glyphs array
  var ret = []
  var quarter = quarter_map[page_id]
  $.each(glyph_map[page_id], function(sura_aya, glyphs){
    var arr = sura_aya.split("-")
    var sura_id = arr[0]
    var aya_id = arr[1]
    var glyph_text = $("<div/>").html(glyphs).text()
    glyphs = glyph_text.split("")
    // remove last glyph which is the aya separator
    glyphs.pop()
    // conditionaly remove first glyph if it is quarter mark
    if (quarter && quarter.sura_id == sura_id 
    && quarter.aya_id == aya_id
    && aya_id != 1)
      glyphs.shift()
    ret.push({
      sura_id: sura_id,
      aya_id: aya_id,
      glyphs: glyphs
    })
  })
  return ret;
}

var qaree_audio_is_secure = function(qaree_id)
{
  return parseInt(qaree_id) <= 100  
}

function load_js_data(url) {
  // loads a Javascript file from url
  $.ajax({
    url: url,
    crossDomain: true,
    dataType: 'jsonp',
    dataType: "script",
    error: function(){
      alert("لا توجد بيانات لهذا القارئ بعد")
    }
  });
}
