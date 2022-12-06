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

function cheсker(numberOfPoeple, numberOfChildrens, Budget, startDate, tripDuration, hotelStars, name, phone, comment){
    if (numberOfPoeple) {
        if (numberOfChildrens) {
            if (Budget) {
                if (startDate) {
                    if (tripDuration) {
                        if (hotelStars) {
                            if (name) {
                                if (phone) {
                                    if (comment) {
                                        return $reactions.transition("/named/arrangeTour/comment");
                                        } else {
                                            return $reactions.transition("/named/arrangeTour/comment");
                                            };
                                    } else {
                                        return $reactions.transition("/named/arrangeTour/phone");
                                        };
                                } else {
                                    return $reactions.transition("/named/arrangeTour/username");
                                };
                            } else {
                                return $reactions.transition("/named/arrangeTour/hotelStars");
                                };
                        } else {
                            return $reactions.transition("/named/arrangeTour/tripDuration");
                            };
                    } else {
                        return $reactions.transition("/named/arrangeTour/startDate");
                        };
                } else {
                    return $reactions.transition("/named/arrangeTour/Budget");
                    };
            } else {
                return $reactions.transition("/named/arrangeTour/numberOfChildrens");
                };
        } else {
            return $reactions.transition("/named/arrangeTour/numberOfPeople");
            };

}


