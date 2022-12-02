var STORMGLASS_API_KEY = $injector.sg_api_key;

function stormGlassWeatherForecast(lat, lng, params, start, end) {
    return $http.query("https://api.stormglass.io/v2/weather/point?lat=${lat}&lng=${lng}&params=${params}&start=${start}&end=${end}", {
        method: "GET",
        timeout: 10000,
        query: {
            lat: lat,
            lng: lng,
            params: params,
            start: start,
            end: end
        },
        headers: {'Authorization': STORMGLASS_API_KEY},
        dataType: "json"
    });
}

var OPENWEATHERMAP_API_KEY = $injector.ow_api_key;

function getGeocoding(q){
return $http.get("http://api.openweathermap.org/geo/1.0/direct?q=${q}&limit=1&appid=${appid}", {
        timeout: 10000,
        query:{
           q: q,
           appid: OPENWEATHERMAP_API_KEY
        },
        dataType: "json",
        headers: {"Content-Type": "application/json"} 
    });
}




