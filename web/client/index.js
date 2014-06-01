//Json coding for creating algorithm and their parameters

var algorithms = [
  {id:1, name: "General Parameters", params: [
    {name: 'cosineSimDiffStoppingCriteria', value: 0.001, dataType: 'double', min: 0, max: 1.0, desc: 'put description here'},
    {name: 'startingTrust', value: 0.8, dataType: 'double' , min: 0, max: 1.0, desc: 'put description here'},
    {name: 'startingErrorFactor', value: 0.4, dataType: 'double' , min: 0, max: 1.0, desc: 'put description here'},
    {name: 'startingConfidence', value: 1, dataType: 'double', min: 0, max: 1.0, desc: 'put description here'},
  ]},
  {id:2, name: "Cosine", params: [
    {name: 'ampeningFactorCosine', value: 0.2, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
  {id:3, name: "2-Estimates", params: [
    {name: 'normalizationWeight', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
  {id:4, name: "3-Estimates", params: [
    {name: 'normalizationWeight', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
  {id:5, name: "Depen", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'put description here'},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
    {name: 'n', value: 100, dataType: 'int', min: 1, desc: 'put description here'},
  ]},
  {id:6, name: "Accu", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'put description here'},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
    {name: 'n', value: 100, dataType: 'int', min: 1, desc: 'put description here'},
  ]},
  {id:7, name: "AccuSim", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'put description here'},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
    {name: 'n', value: 100, dataType: 'int', min: 1, desc: 'put description here'},
  ]},
  {id:8, name: "AccuNoDep", params: [
    {name: 'alpha', value: 0.2, dataType: 'double', min: 0, max: 0.5, desc: 'put description here'},
    {name: 'c', value: 0, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
    {name: 'n', value: 100, dataType: 'int', min: 1, desc: 'put description here'},
  ]},
  {id:9, name: "TruthFinder", params: [
    {name: 'similarityConstant', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
  {id:10, name: "Simple LCA and Guess LCA", params: [
    {name: 'Bita1', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
  {id:11, name: "MLE", params: [
    {name: 'Bita1', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
    {name: 'r', value: 0.5, dataType: 'double', min: 0, max: 1, desc: 'put description here'},
  ]},
]

$(function() {
  // create accordion algorithm panes
  var container = $("#tab_pane")
  var html = ""
  $.each(algorithms, function(index, algorithm) {
    

    var params =""
    
    $.each(algorithm.params, function(pindex, param){
      

      params += "<tr><td width='100%'><span>" + param.name + "</span><span class='ui-icon ui-icon-info' title='"+param.desc+"'></span> </td><td><input class='spinner' min='"+param.min+"' max='"+param.max+"' value='"+param.value+"'></input></td></tr>" 
      
    });
    html += "<h3>"+algorithm.name+"</h3><div><table cellpadding=0 cellspacing=0 width='100%' border=0 style ='border-collapse:collapse;'>" + params+ "</table></div>"
  });
  
  container.append(html)
  $( ".spinner" ).spinner({
      step: 0.1,
      numberFormat: "n",
    }).each(function(){
      $(this).spinner('option', 'min', parseFloat($(this).attr("min")));
      $(this).spinner('option', 'max', parseFloat($(this).attr("max")));
    });

  // Algorithm section in tab-1 under 3rd header
     $(function(){
       var container = $("#algo_pane")
       var html =""
     
      $.each(algorithms, function(index, algorithm){

         html   += "<p><input type='checkbox' id='algorithm_checkbox_"+algorithm.id+"' class='algorithm_checkbox'/><label class='algorithm_checkbox_label' for='algorithm_checkbox_"+algorithm.id+"'>"+algorithm.name +"</label></p>"
   });
        container.append(html);
       // create button checkbox
        $( ".algorithm_checkbox" ).button();

     });
  // create the accordion
    $( ".accordion" ).accordion({
      collapsible: true,
      active:false,
      heightStyle: "content",
    });
 }); 

//Check Checkbox selected
 

//Tree creation 
$(function () {
    // 6 create an instance when the DOM is ready
    $('.jstree')
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


// Jquery UI layout 
$(function(){
   $('body').layout({
    applyDefaultStyles: true,
    west__size: "35%",
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


// Jquery UI nested layout
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

// File uplaoding 
    $(function () {
      $('.fileupload').fileupload({
        dataType: 'json',
        done: function (e, data) {
         // console.log(data)
          alert(data.status);
        },
        progressall: function (e, data) {
            console.log(data)
            var progress = parseInt(data.loaded / data.total * 100, 10);
             $(this).parent().next().find('.progress-bar').css(
                'width',
                progress + '%'
            );
        }

      }); 
    });

// Creating Tab
$(function() {
    $( "#tabs" ).tabs();
});
