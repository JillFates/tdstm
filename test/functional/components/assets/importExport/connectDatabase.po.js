function ConnectDatabase () {
	var mysql = require('../../../node_modules/mysql');
	this.connection = mysql.createConnection({
		host:'localhost',
		user:'root',
		password: '',
		database:'tdstm'
	});
};
module.exports = ConnectDatabase;