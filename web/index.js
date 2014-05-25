$(function() {
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
    west__size: "27%",
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
     south__size: "50%",
    south__minSize: "25%",
    south__maxSize: "75%",
    center__onresize: function() {
      setup_datatable()
    },
});
});