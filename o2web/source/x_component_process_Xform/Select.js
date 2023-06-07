MWF.xDesktop.requireApp("process.Xform", "$Input", null, false);
/** @class Select 下拉选择组件。
 * 在8.1之后，支持从数据字典、视图和查询获取可选项。获取过程为异步。
 * @o2cn 下拉选择
 * @example
 * //可以在脚本中获取该组件
 * //方法1：
 * var field = this.form.get("fieldId"); //获取组件对象
 * //方法2
 * var field = this.target; //在组件本身的脚本中获取，比如事件脚本、默认值脚本、校验脚本等等
 *
 * var data = field.getData(); //获取值
 * field.setData("字符串值"); //设置值
 * field.hide(); //隐藏字段
 * var id = field.json.id; //获取字段标识
 * var flag = field.isEmpty(); //字段是否为空
 * @extends MWF.xApplication.process.Xform.$Input
 * @o2category FormComponents
 * @o2range {Process|CMS|Portal}
 * @hideconstructor
 */
MWF.xApplication.process.Xform.Select = MWF.APPSelect =  new Class(
	/** @lends MWF.xApplication.process.Xform.Select# */
	{
	Implements: [Events],
	Extends: MWF.APP$Input,
	iconStyle: "selectIcon",


		/**
		 * 组件加载后触发。如果选项加载为异步，则异步处理完成后触发此事件
		 * @event MWF.xApplication.process.Xform.Select#load
		 * @see {@link https://www.yuque.com/o2oa/ixsnyt/hm5uft#i0zTS|组件事件说明}
		 */

	/**
	 * @ignore
	 * @member {Element} descriptionNode
	 * @memberOf MWF.xApplication.process.Xform.Select#
	 */
    initialize: function(node, json, form, options){
        this.node = $(node);
        this.node.store("module", this);
        this.json = json;
        this.form = form;
        this.field = true;
		this.fieldModuleLoaded = false;
    },
    _loadNode: function(){
        if (this.isReadonly()){
            this._loadNodeRead();
        }else{
            this._loadNodeEdit();
        }
    },
	_loadMergeReadContentNode: function( contentNode, data ){
		this._showValue(contentNode, data.data);
	},
    _loadNodeRead: function(){
        this.node.empty();
		this.node.set({
			"nodeId": this.json.id,
			"MWFType": this.json.type
		});
        var value = this.getValue();
        this._showValue( this.node, value );
    },
	_showValue: function(node, value){
		var optionItems = this.getOptions();
		o2.promiseAll( optionItems ).then(function (opts) {
			if (value){
				if (typeOf(value)!=="array") value = [value];
				var texts = [];
				opts.each(function(item){
					var tmps = item.split("|");
					var t = tmps[0];
					var v = tmps[1] || t;

					if (v){
						if (value.indexOf(v)!=-1){
							texts.push(t);
						}
					}

				});
				node.set("text", texts.join(", "));
			}
		})

		// if( optionItems && typeOf(optionItems.then) === "function" ){
		// 	optionItems.then(function (opt) {
		// 		this.__showValue(node, opt, value)
		// 	}.bind(this));
		// }else{
		// 	this.__showValue(node, optionItems, value)
		// }
	},
	// __showValue: function(node, optionItems, value){
    //     if (value){
    //         if (typeOf(value)!=="array") value = [value];
    //         var texts = [];
    //         optionItems.each(function(item){
    //             var tmps = item.split("|");
    //             var t = tmps[0];
    //             var v = tmps[1] || t;
	//
    //             if (v){
	//
    //                 if (value.indexOf(v)!=-1){
    //                     texts.push(t);
    //                 }
    //             }
	//
    //         });
    //         node.set("text", texts.join(", "));
    //     }
	// },
	_loadDomEvents: function(){
		Object.each(this.json.events, function(e, key){
			if (e.code){
				if (this.options.moduleEvents.indexOf(key)===-1){
					this.node.addEvent(key, function(event){
						return this.form.Macro.fire(e.code, this, event);
					}.bind(this));
				}
			}
		}.bind(this));
	},
    _loadEvents: function(){
        Object.each(this.json.events, function(e, key){
            if (e.code){
                if (this.options.moduleEvents.indexOf(key)!=-1){
                    this.addEvent(key, function(event){
                        return this.form.Macro.fire(e.code, this, event);
                    }.bind(this));
                }else{
                    this.node.addEvent(key, function(event){
                        return this.form.Macro.fire(e.code, this, event);
                    }.bind(this));
                }
            }
        }.bind(this));
    },
	addModuleEvent: function(key, fun){
		if (this.options.moduleEvents.indexOf(key)!==-1){
			this.addEvent(key, function(event){
				return (fun) ? fun(this, event) : null;
			}.bind(this));
		}else{
			this.node.addEvent(key, function(event){
				return (fun) ? fun(this, event) : null;
			}.bind(this));
		}
	},
    _loadStyles: function(){
    	if (this.areaNode){
            if (this.json.styles) if (this.areaNode) this.areaNode.setStyles(this.json.styles);
            if (this.json.inputStyles) this.node.setStyles(this.json.inputStyles);
		}else{
            if (this.json.styles) this.node.setStyles(this.json.styles);
		}
    },
	_resetNodeEdit: function(){
		this.node.empty();
		var select = new Element("select");
		select.set(this.json.properties);
		select.inject(this.node);
	},
    _loadNodeEdit: function(){
		if (!this.json.preprocessing) this._resetNodeEdit();

		var select = this.node.getFirst();
		this.areaNode = this.node;
		this.areaNode.set({
			"id": this.json.id,
			"MWFType": this.json.type
		});
		this.node = select;

		this.node.set({
			"styles": {
				"margin-right": "12px"
			}
		});
		// this.node.set({
		// 	"id": this.json.id,
		// 	"MWFType": this.json.type,
		// 	"styles": {
		// 		"margin-right": "12px"
		// 	}
		// });
		
		this.setOptions();
        this.node.addEvent("change", function(){
			var v = this.getInputData("change");
			this._setBusinessData(v);
            this.validationMode();
            if (this.validation()) {
				//this._setEnvironmentData(v);
				this.fireEvent("change");
			}
        }.bind(this));

	},
	/**
	 * @summary 刷新选择项，如果选择项是脚本，重新计算。
	 * @example
	 * this.form.get('fieldId').resetOption();
	 */
    resetOption: function(){
        this.node.empty();
        this.setOptions();
		this.fireEvent("resetOption")
    },
	/**
	 * @summary 获取选择项。
	 * @return {Array | Promise} 返回选择项数组或Promise，如：<pre><code class='language-js'>[
	 *  "女|female",
	 *  "男|male"
	 * ]</code></pre>
	 * @example
	 * this.form.get('fieldId').getOptions();
	 * @example
	 * //异步
	 * var opt = this.form.get('fieldId').getOptions();
	 * Promise.resolve(opt).then(function(options){
	 *     //options为选择项数组
	 * })
	 */
	 getOptions: function(async, refresh){
	    this.optionsCache = null;
		var opt = this._getOptions(async, refresh);
		if( (opt && typeOf(opt.then) === "function") ){
			var p = Promise.resolve( opt ).then(function(option){
				this.moduleSelectAG = null;
			    this.optionsCache = (option || []);
			    return this.optionsCache;
			}.bind(this));
			this.moduleSelectAG = p;
			return p;
		}else{
		    this.optionsCache = (opt || []);
			return this.optionsCache;
		}
	},
	_getOptions: function(async, refresh){
	    switch (this.json.itemType) {
			case "values":
				return this.json.itemValues;
			case "script":
				return this.form.Macro.exec(((this.json.itemScript) ? this.json.itemScript.code : ""), this);
			default:
				break;
		}

		var opts, defaultOpts = this.getDefaultOptions();
		switch (this.json.itemType) {
			case "dict":
				opts = this.getOptionsWithDict( async, refresh ); break;
			case "view":
				opts = this.getOptionsWithView( async, refresh ); break;
			case "statement":
				opts = this.getOptionsWithStatement( async, refresh ); break;
		}
		return o2.promiseAll( [defaultOpts, opts] ).then(function (arr) {
			return this._contactOption( arr[0], arr[1] );
		}.bind(this));
	},
	_contactOption: function(opt1, opt2){
	 	var optA, optB;
	 	if( !opt1 )opt1 = [];
	 	if( !opt2 )opt2 = [];
		optA = typeOf(opt1) !== "array" ? [opt1]: opt1;
		optB = typeOf(opt2) !== "array" ? [opt2]: opt2;
		optA.each(function (o) {
			if( o )optB.unshift( o );
		});
		return optB;
	},
	getDefaultOptions: function(){
		return this.form.Macro.exec(((this.json.defaultOptionsScript) ? this.json.defaultOptionsScript.code : ""), this);
	},

	/**
	 * @summary 获取整理后的选择项。
	 * @param {Boolean} [refresh] 是否忽略缓存重新计算可选项。
	 * @return {Object} 返回整理后的选择项数组或Promise，如：
	 * <pre><code class='language-js'>{"valueList": ["","female","male"], "textList": ["","女","男"]}
	 * </code></pre>
	 * @example
	 * var optionData = this.form.get('fieldId').getOptionsObj();
	 * @example
	 * //异步
	 * var opt = this.form.get('fieldId').getOptionsObj(true);
	 * Promise.resolve(opt).then(function(optionData){
	 *     //optionData为选择项
	 * })
	 */
	getOptionsObj : function( refresh ){
		debugger;
		var optionItems = (refresh!==true && this.optionsCache) ? this.optionsCache : this.getOptions();
		if( optionItems && typeOf(optionItems.then) === "function" ){
			return Promise.resolve( optionItems ).then(function(optItems){
				return this._getOptionsObj( optItems );
			}.bind(this));
		}else{
			return this._getOptionsObj( optionItems );
		}
	},
	_getOptionsObj: function( optItems ){
		var textList = [];
		var valueList = [];
		optItems.each(function(item){
			var tmps = item.split("|");
			textList.push( tmps[0] );
			valueList.push( tmps[1] || tmps[0] );
		});
		return { textList : textList, valueList : valueList };
	},

	setOptions: function(){
		var optionItems = this.getOptions();
		this._setOptions(optionItems);
	},
	_setOptions: function(optionItems){
		var p = o2.promiseAll(optionItems).then(function(options){
			this.moduleSelectAG = null;
			if (!options) options = [];
			if (o2.typeOf(options)==="array"){
				options.each(function(item){
					var tmps = item.split("|");
					var text = tmps[0];
					var value = tmps[1] || text;

					var option = new Element("option", {
						"value": value,
						"text": text
					}).inject(this.node);
				}.bind(this));
				this.fireEvent("setOptions", [options])
			}
		}.bind(this), function(){
			this.moduleSelectAG = null;
		}.bind(this));
		this.moduleSelectAG = p;
		if (p) p.then(function(){
			this.moduleSelectAG = null;
		}.bind(this), function(){
			this.moduleSelectAG = null;
		}.bind(this));

		// this.moduleSelectAG = o2.AG.all(optionItems).then(function(options){
		// 	this.moduleSelectAG = null;
		// 	if (!options) options = [];
		// 	if (o2.typeOf(options)==="array"){
		// 		options.each(function(item){
		// 			var tmps = item.split("|");
		// 			var text = tmps[0];
		// 			var value = tmps[1] || text;
		//
		// 			var option = new Element("option", {
		// 				"value": value,
		// 				"text": text
		// 			}).inject(this.node);
		// 		}.bind(this));
		// 		this.fireEvent("setOptions", [options])
		// 	}
		// }.bind(this));
		// if (this.moduleSelectAG) this.moduleSelectAG.then(function(){
		// 	this.moduleSelectAG = null;
		// }.bind(this));
	},
	// __setOptions: function(){
	// 	var optionItems = this.getOptions();
    //     if (!optionItems) optionItems = [];
    //     if (o2.typeOf(optionItems)==="array"){
	// 		optionItems.each(function(item){
	// 			var tmps = item.split("|");
	// 			var text = tmps[0];
	// 			var value = tmps[1] || text;
	//
	// 			var option = new Element("option", {
	// 				"value": value,
	// 				"text": text
	// 			}).inject(this.node);
	// 		}.bind(this));
	// 		this.fireEvent("setOptions", [optionItems])
	// 	}
	// },
	addOption: function(text, value){
        var option = new Element("option", {
            "value": value || text,
            "text": text
        }).inject(this.node);
		this.fireEvent("addOption", [text, value])
	},

	_setValue: function(value, m, fireChange){
		var mothed = m || "__setValue";
		if (!!value){
			var p = o2.promiseAll(value).then(function(v){
				if (o2.typeOf(v)=="array") v = v[0];
				if (this.moduleSelectAG){
					this.moduleValueAG = this.moduleSelectAG;
					this.moduleSelectAG.then(function(){
						this[mothed](v, fireChange);
						return v;
					}.bind(this), function(){});
				}else{
					this[mothed](v, fireChange)
				}
				return v;
			}.bind(this), function(){});

			this.moduleValueAG = p;
			if (this.moduleValueAG) this.moduleValueAG.then(function(){
				this.moduleValueAG = null;
			}.bind(this), function(){
				this.moduleValueAG = null;
			}.bind(this));
		}else{
			this[mothed](value, fireChange);
		}


		// this.moduleValueAG = o2.AG.all(value).then(function(v){
		// 	if (o2.typeOf(v)=="array") v = v[0];
		// 	if (this.moduleSelectAG){
		// 		this.moduleValueAG = this.moduleSelectAG;
		// 		this.moduleSelectAG.then(function(){
		// 			this.__setValue(v);
		// 		}.bind(this));
		// 	}else{
		// 		this.__setValue(v)
		// 	}
		// 	return v;
		// }.bind(this));

		// if (value && value.isAG){
		// 	this.moduleValueAG = o2.AG.all(value),then(function(v){
		// 		this._setValue(v);
		// 	}.bind(this));
		// 	// this.moduleValueAG = value;
		// 	// value.addResolve(function(v){
		// 	// 	this._setValue(v);
		// 	// }.bind(this));
		// }else{
		//
		// }
	},
	__setValue: function(value){
		if (!this.isReadonly()) {
			this._setBusinessData(value);

			var ops = this.node.getElements("option");
			for (var i=0; i<ops.length; i++){
				var option = ops[i];
				if (option.value==value){
					option.selected = true;
					//	break;
				}else{
					option.selected = false;
				}
			}
		}
		this.fieldModuleLoaded = true;
		this.moduleValueAG = null;
	},

	// _setValue: function(value){
	// 	if (!this.readonly && !this.json.isReadonly ) {
    //         this._setBusinessData(value);
    //         for (var i=0; i<this.node.options.length; i++){
    //             var option = this.node.options[i];
    //             if (option.value==value){
    //                 option.selected = true;
    //                 //	break;
    //             }else{
    //                 option.selected = false;
    //             }
    //         }
    //     }
	// 	//this.node.set("value", value);
	// },

	/**
	 * @summary 获取选中项的value和text。
	 * @return {Object} 返回选中项的value和text，如：
	 * <pre><code class='language-js'>{"value": ["male"], "text": ["男"]}
	 * {"value": [""], "text": [""]}
	 * </code></pre>
	 * @example
	 * var data = this.form.get('fieldId').getTextData();
	 * var text = data.text[0] //获取选中项的文本
	 */
	getTextData: function(){
		var ops;
		if (this.isReadonly()){
			ops = this.getOptionsObj();
			var data = this._getBusinessData();
			var d = typeOf(data) === "array" ? data : [data];

			return o2.promiseAll( ops ).then(function (opts) {
				debugger;
				var value = [], text = [];
				d.each( function (v) {
					var idx = opts.valueList.indexOf( v );
					value.push( v || "" );
					text.push( idx > -1 ? opts.textList[idx] : (v || "") );
				});
				if (!value.length) value = [""];
				if (!text.length) text = [""];
				return {"value": value, "text": text};
			})

		}else{
			var value = [], text = [];
			ops = this.node.getElements("option");
			ops.each(function(op){
				if (op.selected){
					var v = op.get("value");
					var t = op.get("text");
					value.push(v || "");
					text.push(t || v || "");
				}
			});
			if (!value.length) value = [""];
			if (!text.length) text = [""];
			return {"value": value, "text": text};
		}
	},

	/**
	 * @summary 获取选中项的text。
	 * @return {String} 返回选中项的text
	 * @example
	 * var text = this.form.get('fieldId').getText(); //获取选中项的文本
	 */
	getText: function(){
		var d = this.getTextData();
		if( typeOf(d.then) === "function" ){
			return d.then(function( d1 ){
				var texts = d1.text;
				return (texts && texts.length) ? texts[0] : "";
			})
		}else{
			var texts = d.text;
			return (texts && texts.length) ? texts[0] : "";
		}
	},
    getInputData: function(){
		if( this.isReadonly()){
			return this._getBusinessData();
		}else{
			var ops = this.node.getElements("option");
			var value = [];
			ops.each(function(op){
				if (op.selected){
					var v = op.get("value");
					value.push(v || "");
				}
			});
			if (!value.length) return null;
			return (value.length==1) ? value[0] : value;
		}
	},
    resetData: function(){
        this.setData(this.getValue());
    },

	setData: function(data, fireChange){
		return this._setValue(data, "__setData", fireChange);
		// if (data && data.isAG){
		// 	this.moduleValueAG = o2.AG.all(data).then(function(v){
		// 		if (o2.typeOf(v)=="array") v = v[0];
		// 		this.__setData(v);
		// 	}.bind(this));
		// }else{
		// 	this.__setData(data);
		// }
		// if (data && data.isAG){
		// 	this.moduleValueAG = data;
		// 	data.addResolve(function(v){
		// 		this.setData(v);
		// 	}.bind(this));
		// }else{
		// 	this.__setData(data);
		// 	this.moduleValueAG = null;
		// }
	},

	__setData: function(data, fireChange){
		var old = this.getInputData();
        this._setBusinessData(data);
		if (this.isReadonly()){
			var d = typeOf(data) === "array" ? data : [data];
			var ops = this.getOptionsObj();
			var result = [];
			if( typeOf(ops.then) === "function" ){
                this.moduleSelectAG = Promise.resolve(ops).then(function(){
                    d.each( function (v) {
                        var idx = ops.valueList.indexOf( v );
                        result.push( idx > -1 ? ops.textList[idx] : v);
                    })
                    this.node.set("text", result.join(","));
                }.bind(this))
			}else{
			    d.each( function (v) {
                    var idx = ops.valueList.indexOf( v );
                    result.push( idx > -1 ? ops.textList[idx] : v);
                })
                this.node.set("text", result.join(","));
			}
		}else{
			var ops = this.node.getElements("option");
			ops.each(function(op){
				if (typeOf(data)==="array"){
					if (data.indexOf(op.get("value"))!=-1){
						op.set("selected", true);
					}else{
						op.set("selected", false);
					}
				}else{
					if (data == op.get("value")){
						op.set("selected", true);
					}else{
						op.set("selected", false);
					}
				}
			});
			this.validationMode();
		}
		this.fieldModuleLoaded = true;
		this.fireEvent("setData", [data]);
		if (fireChange && old!==data) this.fireEvent("change");
	},

	getExcelData: function(){
		var value = this.getData();
		var options = this.getOptionsObj();
		return Promise.resolve(options).then(function (opts) {
			var idx = opts.valueList.indexOf( value );
			var text = idx > -1 ? opts.textList[ idx ] : "";
			return text;
		});
	},
	setExcelData: function(d){
		var value = d.replace(/&#10;/g,""); //换行符&#10;
		this.excelData = value;
		var options = this.getOptionsObj();
		this.moduleSelectAG = Promise.resolve(options).then(function (opts) {
			var idx = opts.textList.indexOf( value );
			value = idx > -1 ? opts.valueList[ idx ] : "";
			this.setData(value, true);
			this.moduleSelectAG = null;
		}.bind(this));
	}
	
}); 
