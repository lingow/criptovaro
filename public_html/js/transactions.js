function get_json(resource) {
    url = "http://localhost/dashboard/data.json";
    var request = makeHttpObject();
	request.open("GET", url, false);
	request.send(null);
	alert(request.responseText);

}

window.onload=get_json("TransactionData");