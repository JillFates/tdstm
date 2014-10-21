'use strict';
var Menu = require('../menu/menu.po.js');
var Rooms = require('./rooms.po.js');
describe('Rooms page', function(){
  it('should go to Rooms View after select Data Centers - List Rooms',function(){
    var menu = new Menu();
    menu.goToDataCentars('listRooms');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/room/list?viewType=list');
  });

  it('should have "Room List" Title', function(){
    var RoomsPage = new Rooms();
    expect(RoomsPage.titleh.getText()).toEqual('Room List');
  });
});//Rooms page