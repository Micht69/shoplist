var openMapDebugMode = false;
var openMaps = [];

function registerMap(mapUtils) {
	openMaps[mapUtils.mapDivId] = mapUtils;
}

function initMap(tabId) {
	if (!openMaps) return;
	
	if (!tabId) {
		// Init de toutes les maps visibles
		$('div[id$=-map]:visible').each(function() {
			mapUtils = openMaps[this.id];
			if (mapUtils) {
				mapUtils.initialize();
			} else {
				mapLog("No map found for ID '"+this.id+"'");
			}
		});
	} else {
		// init des maps de l'onglet clické
		$('#'+tabId).find('div[id$=-map]:visible').each(function() {
			mapUtils = openMaps[this.id];
			if (mapUtils) {
				mapUtils.initialize();
			} else {
				mapLog("No map found for ID '"+this.id+"'");
			}
		});
	}
}

function mapLog(msg) {
	if (openMapDebugMode && window.console && console.log) {
		console.log(msg);
	}
}

function MapUtils(pInputId, pDisabled) {
	mapLog("MapUtils instanciated for '"+pInputId+"' with pDisabled="+pDisabled);
	
	/** Is map initialized **/
	this.initiliazed = false;
	
	/** The map container */
	this.mapDivId = pInputId+'-map';

	/** Is map disabled */
	this.disabled = pDisabled;
	
	/** Points that should be loaded **/
	this.points = new Array();
	this.hasPoints = false;

	/** The OpenLayers map */
	this.map = null;

	/** The address layer */
	this.addressLayer = null;

	/** The Input Id to update geometry */
	this.inputId = pInputId;

	/** The Drag Control */
	this.dragControl = null;

	/** The Click Control */
	this.clickControl = null;

	/** The Select Control */
	this.selectControl = null;
};

/**
 * Initialize
 */
MapUtils.prototype.initialize = function() {
	if (this.initiliazed) return;
	
	// init map
	this.map = new OpenLayers.Map({
        div: this.mapDivId,
        projection: 'EPSG:4326',
        allOverlays: true,
        fractionalZoom: true,
        controls: [
                new OpenLayers.Control.PanZoom(),
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.Attribution()
                ],
        theme: null
    });

	// load basemap
	this.loadBasemapLayers();

	// init address layer
	this.initAddressLayer();
	
	// Load points
	if (this.hasPoints) {
		for (var i=0; i<this.points.length; i++) {
			var point = this.points[i];
			mapLog("MapUtils init point: "+point);
			this.initPoint(point[0], point[1], point[2]);
		}
		this.zoomToExtent();
	}
	
	// Add controls if map enabled
	if (!this.disabled) {
		if (this.hasPoints) {
			this.addClickControl();
		}
		this.addDragControl();
	}
	
	// Record that map is initialized
	this.initiliazed = true;
};

/**
 * Load Basemap Layers (couches de fond de plan)
 */
MapUtils.prototype.loadBasemapLayers = function() {
	/* OpenStreetMap. */
    var osm = new OpenLayers.Layer.OSM();

    var gmapPhysical = new OpenLayers.Layer.Google("Google Physical", {
    	type: google.maps.MapTypeId.TERRAIN,
    	visibility: false,
    	attribution: "Google Physical"
    });

    var gmapStreets = new OpenLayers.Layer.Google("Google Streets", {
    	numZoomLevels: 20,
    	visibility: false,
    	attribution: "Google Streets"
    });

    var gmapHybrid = new OpenLayers.Layer.Google("Google Hybrid", {
    	type: google.maps.MapTypeId.HYBRID,
    	numZoomLevels: 20,
    	visibility: false,
    	attribution: "Google Hybrid"
    });

    var gmapSatellite = new OpenLayers.Layer.Google("Google Satellite", {
    	type: google.maps.MapTypeId.SATELLITE,
    	numZoomLevels: 22,
    	visibility: false,
    	attribution: "Google Satellite"
    });

    // note that first layer must be visible
    this.map.addLayers([osm, gmapPhysical, gmapStreets, gmapHybrid, gmapSatellite]);

 	// Ajout de l'animation
	var animatechecked = true;
	for (var i=this.map.layers.length-1; i>=0; --i) {
		this.map.layers[i].animationEnabled = animatechecked;
    }

	this.map.addControl(new OpenLayers.Control.LayerSwitcher());
	this.map.zoomToMaxExtent();
};

/**
 * Init address Layer
 */
MapUtils.prototype.initAddressLayer = function() {
	var renderer = OpenLayers.Layer.Vector.prototype.renderers;
	this.addressLayer = new OpenLayers.Layer.Vector("Adresse Layer", {
        renderers: renderer,
        styleMap: new OpenLayers.StyleMap({
            "default": new OpenLayers.Style(OpenLayers.Util.applyDefaults({
                externalGraphic: "../static/img/map/marker-green.png",
                graphicOpacity: 1,
                pointRadius: 15
            }, OpenLayers.Feature.Vector.style["default"])),
            "select": new OpenLayers.Style({
                externalGraphic: "../static/img/map/marker-red.png"
            })
        })
    });

	this.map.addLayers([this.addressLayer]);
};

/**
 * Add point to address Layer 
 */
MapUtils.prototype.addPoint = function (longitude, latitude, attributes) {
	var point = new Array(longitude, latitude, attributes);
	mapLog("MapUtils add point:" + point);
	this.points.push(point);
	this.hasPoints = true;
};
MapUtils.prototype.initPoint = function (longitude, latitude, attributes) {
	var foundPosition = new OpenLayers.LonLat(longitude, latitude).transform(
			new OpenLayers.Projection("EPSG:4326"),
			this.map.getProjectionObject()
	);

	var point = new OpenLayers.Geometry.Point(foundPosition.lon, foundPosition.lat);
	var pointFeature = new OpenLayers.Feature.Vector(point, attributes, null);
	this.addressLayer.addFeatures(pointFeature);
};

/**
 * Zoom to address Layer
 */
MapUtils.prototype.zoomToExtent = function () {
	if (!this.hasPoints) return;
	
	if (this.addressLayer.getDataExtent()) {
		this.map.zoomToExtent(this.addressLayer.getDataExtent(), true);

		// add a good zoom
		var zoom = Math.round(this.map.zoom) - 1;
		this.map.zoomTo(zoom);
	}
};

/**
 * Add Drag Control to address Layer
 */
MapUtils.prototype.addDragControl = function() {
	if (this.dragControl) return;
	
	var that = this;
	this.dragControl = new OpenLayers.Control.DragFeature(
		this.addressLayer,
		{
			autoActivate: true,
		    onComplete: function (feature, pixel) {
		    	var point = feature.geometry.getVertices()[0];
		        var newPoint = new OpenLayers.Geometry.Point(point.x, point.y);
		        var foundPosition = newPoint.transform(
		        		that.map.getProjectionObject(),
		        	new OpenLayers.Projection("EPSG:4326")
		    	);
		        var str="POINT (" + foundPosition.y + " " + foundPosition.x + ")";
		    	if (document.getElementById(that.inputId)) {
		    		document.getElementById(that.inputId).value = str;
		    	}
		    }
		}
	);

	this.map.addControl(this.dragControl);
	this.dragControl.activate();
};

/**
 * Add Select Control to address Layer
 */
MapUtils.prototype.addSelectControl = function() {
	if (this.selectControl) return;
	
	this.selectControl = new OpenLayers.Control.SelectFeature(
		this.addressLayer,
		{
			clickout: true,
			toggle: false,
			multiple: false,
			hover: false,
			toggleKey: "ctrlKey", // ctrl key removes from selection
			multipleKey: "shiftKey", // shift key adds to selection
			box: true
		}
	);

	this.map.addControl(this.selectControl);
	this.selectControl.activate();
};

/**
 * Get Selection Array 
 */
MapUtils.prototype.getSelection = function() {
	if (this.selectControl.layers[0]) {
		return this.selectControl.layers[0].selectedFeatures;	
	}
	return new Array();
};

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
            options, this.defaultHandlerOptions
        );
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
        );
        this.handler = new OpenLayers.Handler.Click(
            this, {
                'click': this.onClick,
                'dblclick': this.onDblclick
            }, this.handlerOptions
        );
    },

    onClick: function(evt) {
    	var mapUtils = this.handlerOptions.mapUtils;
        //var msg = "click " + evt.xy;
        //alert(msg);
        var lonlat = mapUtils.map.getLonLatFromPixel(evt.xy);

        var newPoint = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
        var foundPosition = newPoint.transform(
        	mapUtils.map.getProjectionObject(),
        	new OpenLayers.Projection("EPSG:4326")
		);
        // save to input hidden
        var str="POINT (" + foundPosition.y + " " + foundPosition.x + ")";
        if (document.getElementById(mapUtils.inputId)) {
    		document.getElementById(mapUtils.inputId).value = str;
    	}
        // add point to addressLayer
        var mapPoint = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
		var pointFeature = new OpenLayers.Feature.Vector(mapPoint, null, null);
		mapUtils.addressLayer.addFeatures(pointFeature);

		mapUtils.clickControl.deactivate();
    }
});

/**
 * Add Click Control to address Layer
 */
MapUtils.prototype.addClickControl = function() {
	if (this.clickControl) return;
	
	var that = this;

	this.clickControl = new OpenLayers.Control.Click({
		mapUtils: that
	});
	this.map.addControl(this.clickControl);
	this.clickControl.activate();
};
