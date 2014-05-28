var algorithms = [
  {name: "General Parameters", params: [
    {name: 'cosineSimDiffStoppingCriteria', value: 0.001, dataType: 'double', min: 0, max: 1.0},
    {name: 'startingTrust', value: 0.8, dataType: 'double' , min: 0, max: 1.0},
    {name: 'startingErrorFactor', value: 0.4, dataType: 'double' , min: 0, max: 1.0},
    {name: 'startingConfidence', value: 1, dataType: 'double', min: 0, max: 1.0},
  ]},
  {name: "Cosine", params: [
    {name: 'ampeningFactorCosine', value: 0.2, dataType: 'double', min: 0, max: 1},
  ]},
  {name: "2-Estimates", params: [
    {name: 'normalizationWeight', value: 0.5, dataType: 'double', min: 0, max: 1},
  ]},
  {name: "3-Estimates", params: [
    {name: 'normalizationWeight', value: 0.5, dataType: 'double', min: 0, max: 1},
  ]},
  {name: "Depen", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1},
    {name: 'n', value: 100, dataType: 'int', min: 1},
  ]},
  {name: "Accu", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1},
    {name: 'n', value: 100, dataType: 'int', min: 1},
  ]},
  {name: "AccuSim", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1},
    {name: 'n', value: 100, dataType: 'int', min: 1},
  ]},
  {name: "AccuNoDep", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1},
    {name: 'n', value: 100, dataType: 'int', min: 1},
  ]},
  {name: "TruthFinder", params: [
    {name: 'similarityConstant', value: 0.5, dataType: 'double', min: 0, max: 1},
  ]},
  {name: "Simple LCA and Guess LCA", params: [
    {name: 'Bita1', value: 0.5, dataType: 'double', min: 0, max: 1},
  ]},
  {name: "MLE", params: [
    {name: 'Bita1', value: 0.5, dataType: 'double', min: 0, max: 1},
    {name: 'r', value: 0.5, dataType: 'double', min: 0, max: 1},
  ]},
]

$(function() {
  // create accordion algorithm panes
  var container = $("#input_claims_pane")
  var html = ""
  $.each(algorithms, function(index, algorithm) {
    

    var params =""
    
    $.each(algorithm.params, function(pindex, param){
      

      params += "<tr><td>" + param.name + " </td><td><input class='spinner' min='"+param.min+"' max='"+param.max+"' value='"+param.value+"'></input></td></tr>" 
      
    });
    html += "<h3>"+algorithm.name+"</h3><div><table cellpadding=0 cellspacing=0 width='100%' border=0 style ='border-collapse:collapse;'>" + params+ "</table></div>"
  });
  
  container.after(html)
  $( ".spinner" ).spinner({
      step: 0.1,
      numberFormat: "n",
    }).each(function(){
      $(this).spinner('option', 'min', parseFloat($(this).attr("min")));
      $(this).spinner('option', 'max', parseFloat($(this).attr("max")));
    });






  // create the accordion
    $( "#accordion" ).accordion({
      collapsible: true,
      active:false,
      
    });
 }); 



$(function () {
    // 6 create an instance when the DOM is ready
    $('#jstree')
  // listen for event
  .on('changed.jstree', function (e, data) {
    var i, j, r = [];
    for(i = 0, j = data.selected.length; i < j; i++) {
      r.push(data.instance.get_node(data.selected[i]).text);
    }
    $('#event_result').html('Selected: ' + r.join(', '));
  })
  // create the instance
 
  .jstree(); 
		
  });

$(function () {
    // 6 create an instance when the DOM is ready
    $('#jstree_1')
  // listen for event
  .on('changed.jstree_1', function (e, data) {
    var i, j, r = [];
    for(i = 0, j = data.selected.length; i < j; i++) {
      r.push(data.instance.get_node(data.selected[i]).text);
    }
    $('#event_result').html('Selected: ' + r.join(', '));
  })
  // create the instance
  .jstree();
		
  });


$(function(){
   $('body').layout({
    applyDefaultStyles: true,
    west__size: "40%",
    west__minSize: "10%",
    west__maxSize: "50%",
    west__onresize: function() {
      datatable.columns.adjust()
    },
    west__onclose: function() {
      datatable.columns.adjust()
    }, 
    });
   
});



$(function(){
$('.inner').layout({
     applyDefaultStyles: true,
     north__size: "50%",
    north__minSize: "25%",
    north__maxSize: "75%",
    center__onresize: function() {
      setup_datatable()
    },
});
});