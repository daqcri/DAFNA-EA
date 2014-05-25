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
