function encode(data)
{
    var str = String.fromCharCode.apply(null,data);
    return btoa(str).replace(/.{76}(?=.)/g,'$&\n');
}

function refreshData(source,dest,interval){
    $.getJSON(source,function(data){
        tab = $(dest);
        tab.empty();
        if (data.length==0){
            tab.append("No data available");
        } else {
            first = data[0];
            $header = $('<thead>');
            $header.appendTo(tab);
            $row = $('<tr>');
            $row.appendTo($header);
            $th = $('<th>');
            $th.text("#");
            $th.appendTo($row);
            if (data[0].hasOwnProperty("length")){
                    data[0]._lenght_ = data[0].length;
                    delete data[0].length;
                }
            $.each(data[0],function(k,v){
                $th = $('<th>');
                $th.text(k);
                $th.appendTo($row);
            });
            $tb = $('<tbody>');
            $tb.appendTo(tab);
            var i = 1;
            $.each(data,function(){
                var $tr = $('<tr>');
                $tr.append($('<td>').text(i));
                i++;
                if (this.hasOwnProperty("length")){
                    this._lenght_ = this.length;
                    delete this.length;
                }
                $.each(this,function(k,v){
                    if( Object.prototype.toString.call( v ) === '[object Array]' ) {
                        $tr.append($('<td>').text(encode(v)));
                    } else {
                        $tr.append($('<td>').text(v));
                    }
                });
                $tr.appendTo($tb);
            });
        }
    });
    setTimeout("refreshData(\""+source+"\",\""+dest+"\","+interval+")",interval);
}