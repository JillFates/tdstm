
/**----------------------------yahoo.js-------------------------------*/
/* Copyright (c) 2006, Yahoo! Inc. All rights reserved. Code licensed under the BSD License: http://developer.yahoo.net/yui/license.txt   version: 0.10.0   */ 
var YAHOO=window.YAHOO||{};YAHOO.namespace=function(_1){if(!_1||!_1.length){return null;}var _2=_1.split(".");var _3=YAHOO;for(var i=(_2[0]=="YAHOO")?1:0;i<_2.length;++i){_3[_2[i]]=_3[_2[i]]||{};_3=_3[_2[i]];}return _3;};YAHOO.log=function(_5,_6){if(YAHOO.widget.Logger){YAHOO.widget.Logger.log(null,_5,_6);}else{return false;}};YAHOO.namespace("util");YAHOO.namespace("widget");YAHOO.namespace("example");
/**----------------------------animation.js-------------------------*/
/*
Copyright (c) 2006, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 0.10.0
*/
YAHOO.util.Anim=function(el,attributes,duration,method){if(el){this.init(el,attributes,duration,method);}};YAHOO.util.Anim.prototype={doMethod:function(attribute,start,end){return this.method(this.currentFrame,start,end-start,this.totalFrames);},setAttribute:function(attribute,val,unit){YAHOO.util.Dom.setStyle(this.getEl(),attribute,val+unit);},getAttribute:function(attribute){return parseFloat(YAHOO.util.Dom.getStyle(this.getEl(),attribute));},defaultUnit:'px',defaultUnits:{opacity:' '},init:function(el,attributes,duration,method){var isAnimated=false;var startTime=null;var endTime=null;var actualFrames=0;var defaultValues={};el=YAHOO.util.Dom.get(el);this.attributes=attributes||{};this.duration=duration||1;this.method=method||YAHOO.util.Easing.easeNone;this.useSeconds=true;this.currentFrame=0;this.totalFrames=YAHOO.util.AnimMgr.fps;this.getEl=function(){return el;};this.setDefault=function(attribute,val){if(val.constructor!=Array&&(val=='auto'||isNaN(val))){switch(attribute){case'width':val=el.clientWidth||el.offsetWidth;break;case'height':val=el.clientHeight||el.offsetHeight;break;case'left':if(YAHOO.util.Dom.getStyle(el,'position')=='absolute'){val=el.offsetLeft;}else{val=0;}break;case'top':if(YAHOO.util.Dom.getStyle(el,'position')=='absolute'){val=el.offsetTop;}else{val=0;}break;default:val=0;}}defaultValues[attribute]=val;};this.getDefault=function(attribute){return defaultValues[attribute];};this.isAnimated=function(){return isAnimated;};this.getStartTime=function(){return startTime;};this.animate=function(){if(this.isAnimated()){return false;}this.onStart.fire();this._onStart.fire();this.totalFrames=(this.useSeconds)?Math.ceil(YAHOO.util.AnimMgr.fps*this.duration):this.duration;YAHOO.util.AnimMgr.registerElement(this);var attributes=this.attributes;var el=this.getEl();var val;for(var attribute in attributes){val=this.getAttribute(attribute);this.setDefault(attribute,val);}isAnimated=true;actualFrames=0;startTime=new Date();};this.stop=function(){if(!this.isAnimated()){return false;}this.currentFrame=0;endTime=new Date();var data={time:endTime,duration:endTime-startTime,frames:actualFrames,fps:actualFrames/this.duration};isAnimated=false;actualFrames=0;this.onComplete.fire(data);};var onTween=function(){var start;var end=null;var val;var unit;var attributes=this['attributes'];for(var attribute in attributes){unit=attributes[attribute]['unit']||this.defaultUnits[attribute]||this.defaultUnit;if(typeof attributes[attribute]['from']!='undefined'){start=attributes[attribute]['from'];}else{start=this.getDefault(attribute);}if(typeof attributes[attribute]['to']!='undefined'){end=attributes[attribute]['to'];}else if(typeof attributes[attribute]['by']!='undefined'){if(start.constructor==Array){end=[];for(var i=0,len=start.length;i<len;++i){end[i]=start[i]+attributes[attribute]['by'][i];}}else{end=start+attributes[attribute]['by'];}}if(end!==null&&typeof end!='undefined'){val=this.doMethod(attribute,start,end);if((attribute=='width'||attribute=='height'||attribute=='opacity')&&val<0){val=0;}this.setAttribute(attribute,val,unit);}}actualFrames+=1;};this._onStart=new YAHOO.util.CustomEvent('_onStart',this);this.onStart=new YAHOO.util.CustomEvent('start',this);this.onTween=new YAHOO.util.CustomEvent('tween',this);this._onTween=new YAHOO.util.CustomEvent('_tween',this);this.onComplete=new YAHOO.util.CustomEvent('complete',this);this._onTween.subscribe(onTween);}};YAHOO.util.AnimMgr=new function(){var thread=null;var queue=[];var tweenCount=0;this.fps=200;this.delay=1;this.registerElement=function(tween){if(tween.isAnimated()){return false;}queue[queue.length]=tween;tweenCount+=1;this.start();};this.start=function(){if(thread===null){thread=setInterval(this.run,this.delay);}};this.stop=function(tween){if(!tween){clearInterval(thread);for(var i=0,len=queue.length;i<len;++i){if(queue[i].isAnimated()){queue[i].stop();}}queue=[];thread=null;tweenCount=0;}else{tween.stop();tweenCount-=1;if(tweenCount<=0){this.stop();}}};this.run=function(){for(var i=0,len=queue.length;i<len;++i){var tween=queue[i];if(!tween||!tween.isAnimated()){continue;}if(tween.currentFrame<tween.totalFrames||tween.totalFrames===null){tween.currentFrame+=1;if(tween.useSeconds){correctFrame(tween);}tween.onTween.fire();tween._onTween.fire();}else{YAHOO.util.AnimMgr.stop(tween);}}};var correctFrame=function(tween){var frames=tween.totalFrames;var frame=tween.currentFrame;var expected=(tween.currentFrame*tween.duration*1000/tween.totalFrames);var elapsed=(new Date()-tween.getStartTime());var tweak=0;if(elapsed<tween.duration*1000){tweak=Math.round((elapsed/expected-1)*tween.currentFrame);}else{tweak=frames-(frame+1);}if(tweak>0&&isFinite(tweak)){if(tween.currentFrame+tweak>=frames){tweak=frames-(frame+1);}tween.currentFrame+=tweak;}};};YAHOO.util.Bezier=new function(){this.getPosition=function(points,t){var n=points.length;var tmp=[];for(var i=0;i<n;++i){tmp[i]=[points[i][0],points[i][1]];}for(var j=1;j<n;++j){for(i=0;i<n-j;++i){tmp[i][0]=(1-t)*tmp[i][0]+t*tmp[parseInt(i+1,10)][0];tmp[i][1]=(1-t)*tmp[i][1]+t*tmp[parseInt(i+1,10)][1];}}return[tmp[0][0],tmp[0][1]];};};YAHOO.util.Easing=new function(){this.easeNone=function(t,b,c,d){return b+c*(t/=d);};this.easeIn=function(t,b,c,d){return b+c*((t/=d)*t*t);};this.easeOut=function(t,b,c,d){var ts=(t/=d)*t;var tc=ts*t;return b+c*(tc+-3*ts+3*t);};this.easeBoth=function(t,b,c,d){var ts=(t/=d)*t;var tc=ts*t;return b+c*(-2*tc+3*ts);};this.backIn=function(t,b,c,d){var ts=(t/=d)*t;var tc=ts*t;return b+c*(-3.4005*tc*ts+10.2*ts*ts+-6.2*tc+0.4*ts);};this.backOut=function(t,b,c,d){var ts=(t/=d)*t;var tc=ts*t;return b+c*(8.292*tc*ts+-21.88*ts*ts+22.08*tc+-12.69*ts+5.1975*t);};this.backBoth=function(t,b,c,d){var ts=(t/=d)*t;var tc=ts*t;return b+c*(0.402*tc*ts+-2.1525*ts*ts+-3.2*tc+8*ts+-2.05*t);};};YAHOO.util.Motion=function(el,attributes,duration,method){if(el){this.initMotion(el,attributes,duration,method);}};YAHOO.util.Motion.prototype=new YAHOO.util.Anim();YAHOO.util.Motion.prototype.defaultUnits.points='px';YAHOO.util.Motion.prototype.doMethod=function(attribute,start,end){var val=null;if(attribute=='points'){var translatedPoints=this.getTranslatedPoints();var t=this.method(this.currentFrame,0,100,this.totalFrames)/100;if(translatedPoints){val=YAHOO.util.Bezier.getPosition(translatedPoints,t);}}else{val=this.method(this.currentFrame,start,end-start,this.totalFrames);}return val;};YAHOO.util.Motion.prototype.getAttribute=function(attribute){var val=null;if(attribute=='points'){val=[this.getAttribute('left'),this.getAttribute('top')];if(isNaN(val[0])){val[0]=0;}if(isNaN(val[1])){val[1]=0;}}else{val=parseFloat(YAHOO.util.Dom.getStyle(this.getEl(),attribute));}return val;};YAHOO.util.Motion.prototype.setAttribute=function(attribute,val,unit){if(attribute=='points'){YAHOO.util.Dom.setStyle(this.getEl(),'left',val[0]+unit);YAHOO.util.Dom.setStyle(this.getEl(),'top',val[1]+unit);}else{YAHOO.util.Dom.setStyle(this.getEl(),attribute,val+unit);}};YAHOO.util.Motion.prototype.initMotion=function(el,attributes,duration,method){YAHOO.util.Anim.call(this,el,attributes,duration,method);attributes=attributes||{};attributes.points=attributes.points||{};attributes.points.control=attributes.points.control||[];this.attributes=attributes;var start;var end=null;var translatedPoints=null;this.getTranslatedPoints=function(){return translatedPoints;};var translateValues=function(val,self){var pageXY=YAHOO.util.Dom.getXY(self.getEl());val=[val[0]-pageXY[0]+start[0],val[1]-pageXY[1]+start[1]];return val;};var onStart=function(){start=this.getAttribute('points');var attributes=this.attributes;var control=attributes['points']['control']||[];if(control.length>0&&control[0].constructor!=Array){control=[control];}if(YAHOO.util.Dom.getStyle(this.getEl(),'position')=='static'){YAHOO.util.Dom.setStyle(this.getEl(),'position','relative');}if(typeof attributes['points']['from']!='undefined'){YAHOO.util.Dom.setXY(this.getEl(),attributes['points']['from']);start=this.getAttribute('points');}else if((start[0]===0||start[1]===0)){YAHOO.util.Dom.setXY(this.getEl(),YAHOO.util.Dom.getXY(this.getEl()));start=this.getAttribute('points');}var i,len;if(typeof attributes['points']['to']!='undefined'){end=translateValues(attributes['points']['to'],this);for(i=0,len=control.length;i<len;++i){control[i]=translateValues(control[i],this);}}else if(typeof attributes['points']['by']!='undefined'){end=[start[0]+attributes['points']['by'][0],start[1]+attributes['points']['by'][1]];for(i=0,len=control.length;i<len;++i){control[i]=[start[0]+control[i][0],start[1]+control[i][1]];}}if(end){translatedPoints=[start];if(control.length>0){translatedPoints=translatedPoints.concat(control);}translatedPoints[translatedPoints.length]=end;}};this._onStart.subscribe(onStart);};YAHOO.util.Scroll=function(el,attributes,duration,method){if(el){YAHOO.util.Anim.call(this,el,attributes,duration,method);}};YAHOO.util.Scroll.prototype=new YAHOO.util.Anim();YAHOO.util.Scroll.prototype.defaultUnits.scroll=' ';YAHOO.util.Scroll.prototype.doMethod=function(attribute,start,end){var val=null;if(attribute=='scroll'){val=[this.method(this.currentFrame,start[0],end[0]-start[0],this.totalFrames),this.method(this.currentFrame,start[1],end[1]-start[1],this.totalFrames)];}else{val=this.method(this.currentFrame,start,end-start,this.totalFrames);}return val;};YAHOO.util.Scroll.prototype.getAttribute=function(attribute){var val=null;var el=this.getEl();if(attribute=='scroll'){val=[el.scrollLeft,el.scrollTop];}else{val=parseFloat(YAHOO.util.Dom.getStyle(el,attribute));}return val;};YAHOO.util.Scroll.prototype.setAttribute=function(attribute,val,unit){var el=this.getEl();if(attribute=='scroll'){el.scrollLeft=val[0];el.scrollTop=val[1];}else{YAHOO.util.Dom.setStyle(el,attribute,val+unit);}};


/**-----------------------------dom.js---------------------------------*/
YAHOO.util.Dom=function(){var ua=navigator.userAgent.toLowerCase();var isOpera=(ua.indexOf('opera')!=-1);var isIE=(ua.indexOf('msie')!=-1&&!isOpera);var id_counter=0;return{get:function(el){if(typeof el!='string'&&!(el instanceof Array)){return el;}if(typeof el=='string'){return document.getElementById(el);}else{var collection=[];for(var i=0,len=el.length;i<len;++i){collection[collection.length]=this.get(el[i]);}return collection;}return null;},getStyle:function(el,property){var f=function(el){var value=null;var dv=document.defaultView;if(property=='opacity'&&el.filters){value=1;try{value=el.filters.item('DXImageTransform.Microsoft.Alpha').opacity/100;}catch(e){try{value=el.filters.item('alpha').opacity/100;}catch(e){}}}else if(el.style[property]){value=el.style[property];}else if(el.currentStyle&&el.currentStyle[property]){value=el.currentStyle[property];}else if(dv&&dv.getComputedStyle){var converted='';for(var i=0,len=property.length;i<len;++i){if(property.charAt(i)==property.charAt(i).toUpperCase()){converted=converted+'-'+property.charAt(i).toLowerCase();}else{converted=converted+property.charAt(i);}}if(dv.getComputedStyle(el,'')&&dv.getComputedStyle(el,'').getPropertyValue(converted)){value=dv.getComputedStyle(el,'').getPropertyValue(converted);}}return value;};return this.batch(el,f,this,true);},setStyle:function(el,property,val){var f=function(el){switch(property){case'opacity':if(isIE&&typeof el.style.filter=='string'){el.style.filter='alpha(opacity='+val*100+')';if(!el.currentStyle||!el.currentStyle.hasLayout){el.style.zoom=1;}}else{el.style.opacity=val;el.style['-moz-opacity']=val;el.style['-khtml-opacity']=val;}break;default:el.style[property]=val;}};this.batch(el,f,this,true);},getXY:function(el){var f=function(el){if(el.parentNode===null||this.getStyle(el,'display')=='none'){return false;}var parent=null;var pos=[];var box;if(el.getBoundingClientRect){box=el.getBoundingClientRect();var scrollTop=Math.max(document.documentElement.scrollTop,document.body.scrollTop);var scrollLeft=Math.max(document.documentElement.scrollLeft,document.body.scrollLeft);return[box.left+scrollLeft,box.top+scrollTop];}else if(document.getBoxObjectFor){box=document.getBoxObjectFor(el);var borderLeft=parseInt(this.getStyle(el,'borderLeftWidth'));var borderTop=parseInt(this.getStyle(el,'borderTopWidth'));pos=[box.x-borderLeft,box.y-borderTop];}else{pos=[el.offsetLeft,el.offsetTop];parent=el.offsetParent;if(parent!=el){while(parent){pos[0]+=parent.offsetLeft;pos[1]+=parent.offsetTop;parent=parent.offsetParent;}}if(ua.indexOf('opera')!=-1||(ua.indexOf('safari')!=-1&&this.getStyle(el,'position')=='absolute')){pos[0]-=document.body.offsetLeft;pos[1]-=document.body.offsetTop;}}if(el.parentNode){parent=el.parentNode;}else{parent=null;}while(parent&&parent.tagName!='BODY'&&parent.tagName!='HTML'){pos[0]-=parent.scrollLeft;pos[1]-=parent.scrollTop;if(parent.parentNode){parent=parent.parentNode;}else{parent=null;}}return pos;};return this.batch(el,f,this,true);},getX:function(el){return this.getXY(el)[0];},getY:function(el){return this.getXY(el)[1];},setXY:function(el,pos,noRetry){var f=function(el){var style_pos=this.getStyle(el,'position');if(style_pos=='static'){this.setStyle(el,'position','relative');style_pos='relative';}var pageXY=YAHOO.util.Dom.getXY(el);if(pageXY===false){return false;}var delta=[parseInt(YAHOO.util.Dom.getStyle(el,'left'),10),parseInt(YAHOO.util.Dom.getStyle(el,'top'),10)];if(isNaN(delta[0])){delta[0]=(style_pos=='relative')?0:el.offsetLeft;}if(isNaN(delta[1])){delta[1]=(style_pos=='relative')?0:el.offsetTop;}if(pos[0]!==null){el.style.left=pos[0]-pageXY[0]+delta[0]+'px';}if(pos[1]!==null){el.style.top=pos[1]-pageXY[1]+delta[1]+'px';}var newXY=this.getXY(el);if(!noRetry&&(newXY[0]!=pos[0]||newXY[1]!=pos[1])){var retry=function(){YAHOO.util.Dom.setXY(el,pos,true);};setTimeout(retry,0);}};this.batch(el,f,this,true);},setX:function(el,x){this.setXY(el,[x,null]);},setY:function(el,y){this.setXY(el,[null,y]);},getRegion:function(el){var f=function(el){return new YAHOO.util.Region.getRegion(el);};return this.batch(el,f,this,true);},getClientWidth:function(){return this.getViewportWidth();},getClientHeight:function(){return this.getViewportHeight();},getElementsByClassName:function(className,tag,root){var re=new RegExp('(?:^|\\s+)'+className+'(?:\\s+|$)');var method=function(el){return re.test(el['className']);};return this.getElementsBy(method,tag,root);},hasClass:function(el,className){var f=function(el){var re=new RegExp('(?:^|\\s+)'+className+'(?:\\s+|$)');return re.test(el['className']);};return this.batch(el,f,this,true);},addClass:function(el,className){var f=function(el){if(this.hasClass(el,className)){return;}el['className']=[el['className'],className].join(' ');};this.batch(el,f,this,true);},removeClass:function(el,className){var f=function(el){if(!this.hasClass(el,className)){return;}var re=new RegExp('(?:^|\\s+)'+className+'(?:\\s+|$)','g');var c=el['className'];el['className']=c.replace(re,' ');};this.batch(el,f,this,true);},replaceClass:function(el,oldClassName,newClassName){var f=function(el){this.removeClass(el,oldClassName);this.addClass(el,newClassName);};this.batch(el,f,this,true);},generateId:function(el,prefix){prefix=prefix||'yui-gen';var f=function(el){el=el||{};if(!el.id){el.id=prefix+id_counter++;}return el.id;};return this.batch(el,f,this,true);},isAncestor:function(haystack,needle){haystack=this.get(haystack);if(!haystack||!needle){return false;}var f=function(needle){if(haystack.contains&&ua.indexOf('safari')<0){return haystack.contains(needle);}else if(haystack.compareDocumentPosition){return!!(haystack.compareDocumentPosition(needle)&16);}else{var parent=needle.parentNode;while(parent){if(parent==haystack){return true;}else if(parent.tagName=='HTML'){return false;}parent=parent.parentNode;}return false;}};return this.batch(needle,f,this,true);},inDocument:function(el){var f=function(el){return this.isAncestor(document.documentElement,el);};return this.batch(el,f,this,true);},getElementsBy:function(method,tag,root){tag=tag||'*';root=this.get(root)||document;var nodes=[];var elements=root.getElementsByTagName(tag);if(!elements.length&&(tag=='*'&&root.all)){elements=root.all;}for(var i=0,len=elements.length;i<len;++i){if(method(elements[i])){nodes[nodes.length]=elements[i];}}return nodes;},batch:function(el,method,o,override){el=this.get(el);var scope=(override)?o:window;if(!el||el.tagName||!el.length){return method.call(scope,el,o);}var collection=[];for(var i=0,len=el.length;i<len;++i){collection[collection.length]=method.call(scope,el[i],o);}return collection;},getDocumentHeight:function(){var scrollHeight=-1,windowHeight=-1,bodyHeight=-1;var marginTop=parseInt(this.getStyle(document.body,'marginTop'),10);var marginBottom=parseInt(this.getStyle(document.body,'marginBottom'),10);var mode=document.compatMode;if((mode||isIE)&&!isOpera){switch(mode){case'CSS1Compat':scrollHeight=((window.innerHeight&&window.scrollMaxY)?window.innerHeight+window.scrollMaxY:-1);windowHeight=[document.documentElement.clientHeight,self.innerHeight||-1].sort(function(a,b){return(a-b);})[1];bodyHeight=document.body.offsetHeight+marginTop+marginBottom;break;default:scrollHeight=document.body.scrollHeight;bodyHeight=document.body.clientHeight;}}else{scrollHeight=document.documentElement.scrollHeight;windowHeight=self.innerHeight;bodyHeight=document.documentElement.clientHeight;}var h=[scrollHeight,windowHeight,bodyHeight].sort(function(a,b){return(a-b);});return h[2];},getDocumentWidth:function(){var docWidth=-1,bodyWidth=-1,winWidth=-1;var marginRight=parseInt(this.getStyle(document.body,'marginRight'),10);var marginLeft=parseInt(this.getStyle(document.body,'marginLeft'),10);var mode=document.compatMode;if(mode||isIE){switch(mode){case'CSS1Compat':docWidth=document.documentElement.clientWidth;bodyWidth=document.body.offsetWidth+marginLeft+marginRight;winWidth=self.innerWidth||-1;break;default:bodyWidth=document.body.clientWidth;winWidth=document.body.scrollWidth;break;}}else{docWidth=document.documentElement.clientWidth;bodyWidth=document.body.offsetWidth+marginLeft+marginRight;winWidth=self.innerWidth;}var w=[docWidth,bodyWidth,winWidth].sort(function(a,b){return(a-b);});return w[2];},getViewportHeight:function(){var height=-1;var mode=document.compatMode;if((mode||isIE)&&!isOpera){switch(mode){case'CSS1Compat':height=document.documentElement.clientHeight;break;default:height=document.body.clientHeight;}}else{height=self.innerHeight;}return height;},getViewportWidth:function(){var width=-1;var mode=document.compatMode;if(mode||isIE){switch(mode){case'CSS1Compat':width=document.documentElement.clientWidth;break;default:width=document.body.clientWidth;}}else{width=self.innerWidth;}return width;}};}();YAHOO.util.Region=function(t,r,b,l){this.top=t;this[1]=t;this.right=r;this.bottom=b;this.left=l;this[0]=l;};YAHOO.util.Region.prototype.contains=function(region){return(region.left>=this.left&&region.right<=this.right&&region.top>=this.top&&region.bottom<=this.bottom);};YAHOO.util.Region.prototype.getArea=function(){return((this.bottom-this.top)*(this.right-this.left));};YAHOO.util.Region.prototype.intersect=function(region){var t=Math.max(this.top,region.top);var r=Math.min(this.right,region.right);var b=Math.min(this.bottom,region.bottom);var l=Math.max(this.left,region.left);if(b>=t&&r>=l){return new YAHOO.util.Region(t,r,b,l);}else{return null;}};YAHOO.util.Region.prototype.union=function(region){var t=Math.min(this.top,region.top);var r=Math.max(this.right,region.right);var b=Math.max(this.bottom,region.bottom);var l=Math.min(this.left,region.left);return new YAHOO.util.Region(t,r,b,l);};YAHOO.util.Region.prototype.toString=function(){return("Region {"+"t: "+this.top+", r: "+this.right+", b: "+this.bottom+", l: "+this.left+"}");};YAHOO.util.Region.getRegion=function(el){var p=YAHOO.util.Dom.getXY(el);var t=p[1];var r=p[0]+el.offsetWidth;var b=p[1]+el.offsetHeight;var l=p[0];return new YAHOO.util.Region(t,r,b,l);};YAHOO.util.Point=function(x,y){this.x=x;this.y=y;this.top=y;this[1]=y;this.right=x;this.bottom=y;this.left=x;this[0]=x;};YAHOO.util.Point.prototype=new YAHOO.util.Region();
/**-----------------------------event.js-------------------------*/
/*Copyright (c) 2006, Yahoo! Inc. All rights reserved. Code licensed under the BSD License: http://developer.yahoo.net/yui/license.txt version: 0.10.0 */ 
YAHOO.util.CustomEvent=function(_1,_2){this.type=_1;this.scope=_2||window;this.subscribers=[];if(YAHOO.util.Event){YAHOO.util.Event.regCE(this);}};YAHOO.util.CustomEvent.prototype={subscribe:function(fn,_4,_5){this.subscribers.push(new YAHOO.util.Subscriber(fn,_4,_5));},unsubscribe:function(fn,_6){var _7=false;for(var i=0,len=this.subscribers.length;i<len;++i){var s=this.subscribers[i];if(s&&s.contains(fn,_6)){this._delete(i);_7=true;}}return _7;},fire:function(){for(var i=0,len=this.subscribers.length;i<len;++i){var s=this.subscribers[i];if(s){var _10=(s.override)?s.obj:this.scope;s.fn.call(_10,this.type,arguments,s.obj);}}},unsubscribeAll:function(){for(var i=0,len=this.subscribers.length;i<len;++i){this._delete(i);}},_delete:function(_11){var s=this.subscribers[_11];if(s){delete s.fn;delete s.obj;}delete this.subscribers[_11];}};YAHOO.util.Subscriber=function(fn,obj,_13){this.fn=fn;this.obj=obj||null;this.override=(_13);};YAHOO.util.Subscriber.prototype.contains=function(fn,obj){return (this.fn==fn&&this.obj==obj);};if(!YAHOO.util.Event){YAHOO.util.Event=function(){var _14=false;var _15=[];var _16=[];var _17=[];var _18=[];var _19=[];var _20=[];var _21=0;var _22=[];var _23=[];var _24=0;return {POLL_RETRYS:200,POLL_INTERVAL:50,EL:0,TYPE:1,FN:2,WFN:3,SCOPE:3,ADJ_SCOPE:4,isSafari:(/Safari|Konqueror|KHTML/gi).test(navigator.userAgent),isIE:(!this.isSafari&&!navigator.userAgent.match(/opera/gi)&&navigator.userAgent.match(/msie/gi)),addDelayedListener:function(el,_26,fn,_27,_28){_16[_16.length]=[el,_26,fn,_27,_28];if(_14){_21=this.POLL_RETRYS;this.startTimeout(0);}},startTimeout:function(_29){var i=(_29||_29===0)?_29:this.POLL_INTERVAL;var _30=this;var _31=function(){_30._tryPreloadAttach();};this.timeout=setTimeout(_31,i);},onAvailable:function(_32,_33,_34,_35){_22.push({id:_32,fn:_33,obj:_34,override:_35});_21=this.POLL_RETRYS;this.startTimeout(0);},addListener:function(el,_36,fn,_37,_38){if(!fn||!fn.call){return false;}if(this._isValidCollection(el)){var ok=true;for(var i=0,len=el.length;i<len;++i){ok=(this.on(el[i],_36,fn,_37,_38)&&ok);}return ok;}else{if(typeof el=="string"){var oEl=this.getEl(el);if(_14&&oEl){el=oEl;}else{this.addDelayedListener(el,_36,fn,_37,_38);return true;}}}if(!el){return false;}if("unload"==_36&&_37!==this){_17[_17.length]=[el,_36,fn,_37,_38];return true;}var _41=(_38)?_37:el;var _42=function(e){return fn.call(_41,YAHOO.util.Event.getEvent(e),_37);};var li=[el,_36,fn,_42,_41];var _45=_15.length;_15[_45]=li;if(this.useLegacyEvent(el,_36)){var _46=this.getLegacyIndex(el,_36);if(_46==-1){_46=_19.length;_23[el.id+_36]=_46;_19[_46]=[el,_36,el["on"+_36]];_20[_46]=[];el["on"+_36]=function(e){YAHOO.util.Event.fireLegacyEvent(YAHOO.util.Event.getEvent(e),_46);};}_20[_46].push(_45);}else{if(el.addEventListener){el.addEventListener(_36,_42,false);}else{if(el.attachEvent){el.attachEvent("on"+_36,_42);}}}return true;},fireLegacyEvent:function(e,_47){var ok=true;var le=_20[_47];for(var i=0,len=le.length;i<len;++i){var _49=le[i];if(_49){var li=_15[_49];if(li&&li[this.WFN]){var _50=li[this.ADJ_SCOPE];var ret=li[this.WFN].call(_50,e);ok=(ok&&ret);}else{delete le[i];}}}return ok;},getLegacyIndex:function(el,_52){var key=this.generateId(el)+_52;if(typeof _23[key]=="undefined"){return -1;}else{return _23[key];}},useLegacyEvent:function(el,_54){if(!el.addEventListener&&!el.attachEvent){return true;}else{if(this.isSafari){if("click"==_54||"dblclick"==_54){return true;}}}return false;},removeListener:function(el,_55,fn,_56){if(!fn||!fn.call){return false;}if(typeof el=="string"){el=this.getEl(el);}else{if(this._isValidCollection(el)){var ok=true;for(var i=0,len=el.length;i<len;++i){ok=(this.removeListener(el[i],_55,fn)&&ok);}return ok;}}if("unload"==_55){for(i=0,len=_17.length;i<len;i++){var li=_17[i];if(li&&li[0]==el&&li[1]==_55&&li[2]==fn){delete _17[i];return true;}}return false;}var _57=null;if("undefined"==typeof _56){_56=this._getCacheIndex(el,_55,fn);}if(_56>=0){_57=_15[_56];}if(!el||!_57){return false;}if(el.removeEventListener){el.removeEventListener(_55,_57[this.WFN],false);}else{if(el.detachEvent){el.detachEvent("on"+_55,_57[this.WFN]);}}delete _15[_56][this.WFN];delete _15[_56][this.FN];delete _15[_56];return true;},getTarget:function(ev,_59){var t=ev.target||ev.srcElement;if(_59&&t&&"#text"==t.nodeName){return t.parentNode;}else{return t;}},getPageX:function(ev){var x=ev.pageX;if(!x&&0!==x){x=ev.clientX||0;if(this.isIE){x+=this._getScrollLeft();}}return x;},getPageY:function(ev){var y=ev.pageY;if(!y&&0!==y){y=ev.clientY||0;if(this.isIE){y+=this._getScrollTop();}}return y;},getXY:function(ev){return [this.getPageX(ev),this.getPageY(ev)];},getRelatedTarget:function(ev){var t=ev.relatedTarget;if(!t){if(ev.type=="mouseout"){t=ev.toElement;}else{if(ev.type=="mouseover"){t=ev.fromElement;}}}return t;},getTime:function(ev){if(!ev.time){var t=new Date().getTime();try{ev.time=t;}catch(e){return t;}}return ev.time;},stopEvent:function(ev){this.stopPropagation(ev);this.preventDefault(ev);},stopPropagation:function(ev){if(ev.stopPropagation){ev.stopPropagation();}else{ev.cancelBubble=true;}},preventDefault:function(ev){if(ev.preventDefault){ev.preventDefault();}else{ev.returnValue=false;}},getEvent:function(e){var ev=e||window.event;if(!ev){var c=this.getEvent.caller;while(c){ev=c.arguments[0];if(ev&&Event==ev.constructor){break;}c=c.caller;}}return ev;},getCharCode:function(ev){return ev.charCode||((ev.type=="keypress")?ev.keyCode:0);},_getCacheIndex:function(el,_64,fn){for(var i=0,len=_15.length;i<len;++i){var li=_15[i];if(li&&li[this.FN]==fn&&li[this.EL]==el&&li[this.TYPE]==_64){return i;}}return -1;},generateId:function(el){var id=el.id;if(!id){id="yuievtautoid-"+(_24++);el.id=id;}return id;},_isValidCollection:function(o){return (o&&o.length&&typeof o!="string"&&!o.tagName&&!o.alert&&typeof o[0]!="undefined");},elCache:{},getEl:function(id){return document.getElementById(id);},clearCache:function(){},regCE:function(ce){_18.push(ce);},_load:function(e){_14=true;},_tryPreloadAttach:function(){if(this.locked){return false;}this.locked=true;var _68=!_14;if(!_68){_68=(_21>0);}var _69=[];for(var i=0,len=_16.length;i<len;++i){var d=_16[i];if(d){var el=this.getEl(d[this.EL]);if(el){this.on(el,d[this.TYPE],d[this.FN],d[this.SCOPE],d[this.ADJ_SCOPE]);delete _16[i];}else{_69.push(d);}}}_16=_69;notAvail=[];for(i=0,len=_22.length;i<len;++i){var _71=_22[i];if(_71){el=this.getEl(_71.id);if(el){var _72=(_71.override)?_71.obj:el;_71.fn.call(_72,_71.obj);delete _22[i];}else{notAvail.push(_71);}}}_21=(_69.length===0&&notAvail.length===0)?0:_21-1;if(_68){this.startTimeout();}this.locked=false;},_unload:function(e,me){for(var i=0,len=_17.length;i<len;++i){var l=_17[i];if(l){var _75=(l[this.ADJ_SCOPE])?l[this.SCOPE]:window;l[this.FN].call(_75,this.getEvent(e),l[this.SCOPE]);}}if(_15&&_15.length>0){for(i=0,len=_15.length;i<len;++i){l=_15[i];if(l){this.removeListener(l[this.EL],l[this.TYPE],l[this.FN],i);}}this.clearCache();}for(i=0,len=_18.length;i<len;++i){_18[i].unsubscribeAll();delete _18[i];}for(i=0,len=_19.length;i<len;++i){delete _19[i][0];delete _19[i];}},_getScrollLeft:function(){return this._getScroll()[1];},_getScrollTop:function(){return this._getScroll()[0];},_getScroll:function(){var dd=document.documentElement;db=document.body;if(dd&&dd.scrollTop){return [dd.scrollTop,dd.scrollLeft];}else{if(db){return [db.scrollTop,db.scrollLeft];}else{return [0,0];}}}};}();YAHOO.util.Event.on=YAHOO.util.Event.addListener;if(document&&document.body){YAHOO.util.Event._load();}else{YAHOO.util.Event.on(window,"load",YAHOO.util.Event._load,YAHOO.util.Event,true);}YAHOO.util.Event.on(window,"unload",YAHOO.util.Event._unload,YAHOO.util.Event,true);YAHOO.util.Event._tryPreloadAttach();}
/**------------------------------tabcontent.js---------------------------------*/

function ddtabcontent(tabinterfaceid){
	this.tabinterfaceid=tabinterfaceid //ID of Tab Menu main container
	this.tabs=document.getElementById(tabinterfaceid).getElementsByTagName("a") //Get all tab links within container
	this.enabletabpersistence=true
	this.hottabspositions=[] //Array to store position of tabs that have a "rel" attr defined, relative to all tab links, within container
	this.currentTabIndex=0 //Index of currently selected hot tab (tab with sub content) within hottabspositions[] array
	this.subcontentids=[] //Array to store ids of the sub contents ("rel" attr values)
	this.revcontentids=[] //Array to store ids of arbitrary contents to expand/contact as well ("rev" attr values)
	this.selectedClassTarget="link" //keyword to indicate which target element to assign "selected" CSS class ("linkparent" or "link")
}

ddtabcontent.getCookie=function(Name){ 
	var re=new RegExp(Name+"=[^;]+", "i"); //construct RE to search for target name/value pair
	if (document.cookie.match(re)) //if cookie found
		return document.cookie.match(re)[0].split("=")[1] //return its value
	return ""
}

ddtabcontent.setCookie=function(name, value){
	document.cookie = name+"="+value+";path=/" //cookie value is domain wide (path=/)
}

ddtabcontent.prototype={

	expandit:function(tabid_or_position){ //PUBLIC function to select a tab either by its ID or position(int) within its peers
		this.cancelautorun() //stop auto cycling of tabs (if running)
		var tabref=""
		try{
			if (typeof tabid_or_position=="string" && document.getElementById(tabid_or_position).getAttribute("rel")) //if specified tab contains "rel" attr
				tabref=document.getElementById(tabid_or_position)
			else if (parseInt(tabid_or_position)!=NaN && this.tabs[tabid_or_position].getAttribute("rel")) //if specified tab contains "rel" attr
				tabref=this.tabs[tabid_or_position]
		}
		catch(err){alert("Invalid Tab ID or position entered!")}
		if (tabref!="") //if a valid tab is found based on function parameter
			this.expandtab(tabref) //expand this tab
	},

	cycleit:function(dir, autorun){ //PUBLIC function to move foward or backwards through each hot tab (tabinstance.cycleit('foward/back') )
		if (dir=="next"){
			var currentTabIndex=(this.currentTabIndex<this.hottabspositions.length-1)? this.currentTabIndex+1 : 0
		}
		else if (dir=="prev"){
			var currentTabIndex=(this.currentTabIndex>0)? this.currentTabIndex-1 : this.hottabspositions.length-1
		}
		if (typeof autorun=="undefined") //if cycleit() is being called by user, versus autorun() function
			this.cancelautorun() //stop auto cycling of tabs (if running)
		this.expandtab(this.tabs[this.hottabspositions[currentTabIndex]])
	},

	setpersist:function(bool){ //PUBLIC function to toggle persistence feature
			this.enabletabpersistence=bool
	},

	setselectedClassTarget:function(objstr){ //PUBLIC function to set which target element to assign "selected" CSS class ("linkparent" or "link")
		this.selectedClassTarget=objstr || "link"
	},

	getselectedClassTarget:function(tabref){ //Returns target element to assign "selected" CSS class to
		return (this.selectedClassTarget==("linkparent".toLowerCase()))? tabref.parentNode : tabref
	},

	urlparamselect:function(tabinterfaceid){
		var result=window.location.search.match(new RegExp(tabinterfaceid+"=(\\d+)", "i")) //check for "?tabinterfaceid=2" in URL
		return (result==null)? null : parseInt(RegExp.$1) //returns null or index, where index (int) is the selected tab's index
	},

	expandtab:function(tabref){
		var subcontentid=tabref.getAttribute("rel") //Get id of subcontent to expand
		//Get "rev" attr as a string of IDs in the format ",john,george,trey,etc," to easily search through
		var associatedrevids=(tabref.getAttribute("rev"))? ","+tabref.getAttribute("rev").replace(/\s+/, "")+"," : ""
		this.expandsubcontent(subcontentid)
		this.expandrevcontent(associatedrevids)
		for (var i=0; i<this.tabs.length; i++){ //Loop through all tabs, and assign only the selected tab the CSS class "selected"
			this.getselectedClassTarget(this.tabs[i]).className=(this.tabs[i].getAttribute("rel")==subcontentid)? "selected" : ""
		}
		if (this.enabletabpersistence) //if persistence enabled, save selected tab position(int) relative to its peers
			ddtabcontent.setCookie(this.tabinterfaceid, tabref.tabposition)
		this.setcurrenttabindex(tabref.tabposition) //remember position of selected tab within hottabspositions[] array
	},

	expandsubcontent:function(subcontentid){
		for (var i=0; i<this.subcontentids.length; i++){
			var subcontent=document.getElementById(this.subcontentids[i]) //cache current subcontent obj (in for loop)
			subcontent.style.display=(subcontent.id==subcontentid)? "block" : "none" //"show" or hide sub content based on matching id attr value
		}
	},

	expandrevcontent:function(associatedrevids){
		var allrevids=this.revcontentids
		for (var i=0; i<allrevids.length; i++){ //Loop through rev attributes for all tabs in this tab interface
			//if any values stored within associatedrevids matches one within allrevids, expand that DIV, otherwise, contract it
			document.getElementById(allrevids[i]).style.display=(associatedrevids.indexOf(","+allrevids[i]+",")!=-1)? "block" : "none"
		}
	},

	setcurrenttabindex:function(tabposition){ //store current position of tab (within hottabspositions[] array)
		for (var i=0; i<this.hottabspositions.length; i++){
			if (tabposition==this.hottabspositions[i]){
				this.currentTabIndex=i
				break
			}
		}
	},

	autorun:function(){ //function to auto cycle through and select tabs based on a set interval
		this.cycleit('next', true)
	},

	cancelautorun:function(){
		if (typeof this.autoruntimer!="undefined")
			clearInterval(this.autoruntimer)
	},

	init:function(automodeperiod){
		var persistedtab=ddtabcontent.getCookie(this.tabinterfaceid) //get position of persisted tab (applicable if persistence is enabled)
		var selectedtab=-1 //Currently selected tab index (-1 meaning none)
		var selectedtabfromurl=this.urlparamselect(this.tabinterfaceid) //returns null or index from: 
		this.automodeperiod=automodeperiod || 0
		for (var i=0; i<this.tabs.length; i++){
			this.tabs[i].tabposition=i //remember position of tab relative to its peers
			if (this.tabs[i].getAttribute("rel")){
				var tabinstance=this
				this.hottabspositions[this.hottabspositions.length]=i //store position of "hot" tab ("rel" attr defined) relative to its peers
				this.subcontentids[this.subcontentids.length]=this.tabs[i].getAttribute("rel") //store id of sub content ("rel" attr value)
				this.tabs[i].onclick=function(){
					tabinstance.expandtab(this)
					tabinstance.cancelautorun() //stop auto cycling of tabs (if running)
					return false
				}
				if (this.tabs[i].getAttribute("rev")){ //if "rev" attr defined, store each value within "rev" as an array element
					this.revcontentids=this.revcontentids.concat(this.tabs[i].getAttribute("rev").split(/\s*,\s*/))
				}
				if (selectedtabfromurl==i || this.enabletabpersistence && selectedtab==-1 && parseInt(persistedtab)==i || !this.enabletabpersistence && selectedtab==-1 && this.getselectedClassTarget(this.tabs[i]).className=="selected"){
					selectedtab=i //Selected tab index, if found
				}
			}
		} //END for loop
		if (selectedtab!=-1) //if a valid default selected tab index is found
			this.expandtab(this.tabs[selectedtab]) //expand selected tab (either from URL parameter, persistent feature, or class="selected" class)
		else //if no valid default selected index found
			this.expandtab(this.tabs[this.hottabspositions[0]]) //Just select first tab that contains a "rel" attr
		if (parseInt(this.automodeperiod)>500 && this.hottabspositions.length>1){
			this.autoruntimer=setInterval(function(){tabinstance.autorun()}, this.automodeperiod)
		}
	} 

} 

/**--------------------------------textscroll.js------------------------------------*/

ypSimpleScroll.prototype.scrollNorth = function() { this.startScroll(90) }
ypSimpleScroll.prototype.scrollSouth = function() { this.startScroll(270)  }
ypSimpleScroll.prototype.scrollWest  = function() { this.startScroll(180) }
ypSimpleScroll.prototype.scrollEast  = function() { this.startScroll(0)   }



ypSimpleScroll.prototype.startScroll = function(deg, speed) {
  if(this.load) {
    if (this.aniTimer) window.clearTimeout(this.aniTimer);
    this.overrideScrollAngle(deg);

    this.speed    = speed ? speed : this.origSpeed ;
    this.lastTime = (new Date()).getTime() - this.y.minRes ;
    this.aniTimer = window.setTimeout(this.gRef + ".scroll()", this.y.minRes) ;
  }
}


ypSimpleScroll.prototype.endScroll = function() {
  if (this.loaded)
  {
    window.clearTimeout(this.aniTimer)
    this.aniTimer = 0;
    this.speed = this.origSpeed
  }
}


ypSimpleScroll.prototype.overrideScrollAngle = function(deg) {
  if (this.loaded)
  {
    deg = deg % 360
    if (deg % 90 == 0) {
      var cos = deg == 0  ? 1 : deg == 180 ? -1 : 0
      var sin = deg == 90 ? -1 : deg == 270 ? 1 : 0
    } else {
      var angle = deg * Math.PI / 180
      var cos   = Math.cos(angle)
      var sin   = Math.sin(angle)
      sin = -sin
    }
    this.fx = cos / (Math.abs(cos) + Math.abs(sin))
    this.fy = sin / (Math.abs(cos) + Math.abs(sin))
    this.stopH = deg == 90 || deg == 270 ? this.scrollLeft : deg < 90 || deg > 270 ? this.scrollW : 0
    this.stopV = deg == 0 || deg == 180 ? this.scrollTop : deg < 180 ? 0 : this.scrollH
  }
}


ypSimpleScroll.prototype.overrideScrollSpeed = function(speed) {
  if (this.loaded) this.speed = speed
}


ypSimpleScroll.prototype.scrollTo = function(stopH, stopV, aniLen) {
  if (this.loaded)
  {
    if (stopH != this.scrollLeft || stopV != this.scrollTop) {
      if (this.aniTimer) window.clearTimeout(this.aniTimer)
      this.lastTime = (new Date()).getTime()
      var dx = Math.abs(stopH - this.scrollLeft)
      var dy = Math.abs(stopV - this.scrollTop)
      var d = Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2))
      this.fx = (stopH - this.scrollLeft) / (dx + dy)
      this.fy = (stopV - this.scrollTop) / (dx + dy)
      this.stopH = stopH
      this.stopV = stopV
      this.speed = d / aniLen * 1000
      window.setTimeout(this.gRef + ".scroll()", this.y.minRes)
    }
  }
}


ypSimpleScroll.prototype.jumpTo = function(nx, ny) { 
  if (this.loaded)
  {
    nx = Math.min(Math.max(nx, 0), this.scrollW)
    ny = Math.min(Math.max(ny, 0), this.scrollH)
    this.scrollLeft = nx
    this.scrollTop = ny
    if (this.y.ns4)this.content.moveTo(-nx, -ny)
    else {
      this.content.style.left = -nx + "px"
      this.content.style.top = -ny + "px"
    }
  }
}


ypSimpleScroll.minRes = 10
ypSimpleScroll.ie  = document.all ? 1 : 0
ypSimpleScroll.ns4 = document.layers ? 1 : 0
ypSimpleScroll.dom = document.getElementById ? 1 : 0
ypSimpleScroll.mac = navigator.platform == "MacPPC"
ypSimpleScroll.mo5 = document.getElementById && !document.all ? 1 : 0

// recursively calls itself to animate
ypSimpleScroll.prototype.scroll = function() {
  this.aniTimer = window.setTimeout(this.gRef + ".scroll()", this.y.minRes)
  var nt = (new Date()).getTime()
  var d = Math.round((nt - this.lastTime) / 1000 * this.speed)
  if (d > 0)
  {
    var nx = d * this.fx + this.scrollLeft
    var ny = d * this.fy + this.scrollTop

    var xOut = (nx >= this.scrollLeft && nx >= this.stopH) || (nx <= this.scrollLeft && nx <= this.stopH)
    var yOut = (ny >= this.scrollTop && ny >= this.stopV)  || (ny <= this.scrollTop && ny <= this.stopV)
    //window.status = nx + " " + this.fx + " " + this.scrollLeft + " " + this.stopH + " | " + ny + " " + this.fy + " " + this.scrollTop + " " + this.stopV + " : " + d
    if (nt - this.lastTime != 0 &&
      ((this.fx == 0 && this.fy == 0) ||
       (this.fy == 0 && xOut) ||
       (this.fx == 0 && yOut) ||
       (this.fx != 0 && this.fy != 0 && xOut && yOut)))
    {

      this.jumpTo(this.stopH, this.stopV)
      this.endScroll()
    }
    else {
      this.jumpTo(nx, ny)
      this.lastTime = nt
    }
  }
}

// constructor
function ypSimpleScroll(id, left, top, width, height, speed, contentWidth, initLeft, initTop)
{
  var y = this.y = ypSimpleScroll
  if (!initLeft)     initLeft     = 0
  if (!initTop)      initTop      = 0
  if (!contentWidth) contentWidth = width

  // fix for netscape4 first-page bug.
  if (document.layers && !y.ns4) history.go(0)

  if (y.ie || y.ns4 || y.dom) {
    this.loaded    = false
    this.id      = id
    this.origSpeed  = speed
    this.aniTimer  = false
    this.op      = ""
    this.lastTime  = 0
    this.clipH    = height
    this.clipW    = width
    this.scrollTop  = initTop
    this.scrollLeft  = initLeft

    // global reference to this object
    this.gRef = "ypSimpleScroll_"+id
    eval(this.gRef+"=this")

    var d = document
    d.write('<style type="text/css">')
    d.write('#' + this.id + 'Container { left:' + left + 'px; top:' + top + 'px; width:' + width + 'px; height:' + height + 'px; clip:rect(0 ' + width + ' ' + height + ' 0); overflow:hidden; }')
    d.write('#' + this.id + 'Container, #' + this.id + 'Content { position:absolute; }')
    d.write('#' + this.id + 'Content { left:' + (-initLeft) + 'px; top:' + (-initTop) + 'px; width:' + contentWidth + 'px; }')
    d.write('</style>')
  }
}

// once the nceesary objects are present in the DOM, this gathers some info about them.
ypSimpleScroll.prototype.load = function() {
  var d, lyrId1, lyrId2

  d = document
  lyrId1 = this.id + "Container"
  lyrId2 = this.id + "Content"

  this.container  = this.y.dom ? d.getElementById(lyrId1) : this.y.ie ? d.all[lyrId1] : d.layers[lyrId1]
  this.content  = obj2 = this.y.ns4 ? this.container.layers[lyrId2] : this.y.ie ? d.all[lyrId2] : d.getElementById(lyrId2)
  this.docH    = Math.max(this.y.ns4 ? this.content.document.height : this.content.offsetHeight, this.clipH)
  this.docW    = Math.max(this.y.ns4 ? this.content.document.width  : this.content.offsetWidth,  this.clipW)
  this.scrollH  = this.docH - this.clipH
  this.scrollW  = this.docW - this.clipW
  this.loaded    = true
  this.scrollLeft = Math.max(Math.min(this.scrollLeft, this.scrollW),0)
  this.scrollTop  = Math.max(Math.min(this.scrollTop, this.scrollH),0)
  this.jumpTo(this.scrollLeft, this.scrollTop)
}

var test = new ypSimpleScroll("myScroll", 20, 50, 150, 250, 100)

