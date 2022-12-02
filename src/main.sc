require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: functions.js
require: patterns.sc
theme: /

    state: Start
        q!: $regex</start>
        script: $jsapi.startSession()
        
        if: $client.name
            go!: /named
        else:
            go!: /unnamed

    state: unnamed
        a: Здравствуйте! Я чат-бот туристической компании Just Tour. Могу рассказать о погоде по всему миру, а также помочь оформить тур.
        go!: whatsName
   
        state: whatsName
            a: Как могу к вам обращаться?
                
            state: getName
                intent: /Name
                script: $client.name = $parseTree._NAME
                go!: /niceToMeet

            state: LocalCatchAll 
                event: noMatch 
                script: $client.name = $parseTree.text
                a: Хмм, я не знаю такого имени. Записать вас как {{$client.name}}?
                buttons: 
                    "Да" -> /niceToMeet
                    "Нет" -> /unnamed/whatsName
  
    state: niceToMeet
        a: Приятно познакомиться, {{$client.name}}. Буду рад вам помочь!
        buttons:
            "Узнать погоду" -> /named/checkWeather  
            "Оформить тур" -> /named/arrangeTour  

    state: named
        a: Привет, {{$client.name}}! Я чат-бот туристической компании Just Tour. Могу рассказать о погоде по всему миру, а также помочь оформить тур.
        buttons:
            "Узнать погоду" -> ./checkWeather
            "Оформить тур" -> ./arrangeTour 

        state: checkWeather
            intent!: /Weather
            script: $session.geo = $parseTree._GEO
                    $session.date = $parseTree._DATE
            
            if: $session.geo && $session.date
                go!: getGeocode
            
            elseif: $session.geo
                a: На какую дату вы хотите узнать погоду?
                go: getDate    
            
            elseif: $session.date
                a: Где вы хотите узнать погоду?
                go: getGeo
            
            else: 
                a: Где вы хотите узнать погоду?
                go: getBoth

            state: getGeo
                intent: /Geo
                script: $session.geo = $parseTree._GEO
                go!: /named/checkWeather/getGeocode

            state: getDate
                intent: /Date
                script: $session.date = $parseTree._DATE
                go!: /named/checkWeather/getGeocode

            state: getBoth
                intent: /Geo
                script: $session.geo = $parseTree._GEO
                a: На какую дату вы хотите узнать погоду?
                go: /named/checkWeather/getDate

            state: getGeocode
                script:
                    var q = $caila.inflect($session.geo, ["nomn"]); // форматируем город в правильный падеж
                    getGeocoding(q).then(function (result) { // Получаем геолокацию города
                        $session.lon = result[0].lon;
                        $session.lat = result[0].lat;
                    });
                go!: getForecast

                state: getForecast
                    script:
                        $session.userDate = $session.date.day + "." + $session.date.month + "." + $session.date.year // Сохраняем дату в понятном клиенту формате
                        
                        stormGlassWeatherForecast($session.lat, $session.lon, "airTemperature", $session.date.value, $session.date.value).then(function (res) { // В тз не указали точные значения климата, так что выбрали значения температуры на свой взгляд
                            if (res && res.hours[0].airTemperature.sg) {
                                $session.temp = Math.round(res.hours[0].airTemperature.sg);
                                if (Math.round($session.temp) >= 30) {
                                    $reactions.answer($session.userDate + " " + "температура в" + " " + capitalize($caila.inflect($session.geo, ["loct"])) + " " + $session.temp + "°C" + " " + "Вы уверены что хотите посетить страну с таким жарким климатом?");
                                    $reactions.buttons([{text: "Да", transition: "/named/arrangeTour"}, {text: "Нет", transition: "/named/checkWeather"}]);
                                    } else if (Math.round($session.temp) <= -10) {
                                        $reactions.answer($session.userDate + " " + "температура в" + " " + capitalize($caila.inflect($session.geo, ["loct"])) + " " + $session.temp + "°C" + " " + "Вы уверены что хотите посетить страну с таким холодным климатом?");
                                        $reactions.buttons([{text: "Да", transition: "/named/arrangeTour"}, {text: "Нет", transition: "/named/checkWeather"}]);
                                        } else {
                                            $reactions.answer($session.userDate + " " + "температура в" + " " + capitalize($caila.inflect($session.geo, ["loct"])) + " " + $session.temp + "°C" + " " + "Вы уверены что хотите посетить страну с умеренным климатом?");
                                            $reactions.buttons([{text: "Да", transition: "/named/arrangeTour"}, {text: "Нет", transition: "/named/checkWeather"}]);
                                        };
                                } else {
                                    $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                                    }
                        }).catch(function (err) {
                            $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                            });

                    state: agree
                        q: $agree
                        go!: /named/arrangeTour

                    state: disagree
                        q: $disagree
                        go!: /named/checkWeather

        state: arrangeTour
            intent!: /Tour
            script:
                if ($session.geo) { // Проверяем указывал ли клиент город
                    if ($session.numberOfPoeple || $session.numberOfChildrens || $session.Budget || $session.startDate || $session.tripDuration || $session.hotelStars) { // Проверяем начинал ли клиент заполнять анкету
                        $reactions.answer("Вижу, что Вы уже начинали оформлять тур, Продолжим?");
                        $reactions.buttons([{text: "Продолжить", transition: "/named/arrangeTour/checker"}, {text: "Нет", transition: "/named/checkWeather"}]);
                    } else {
                        $reactions.answer("Отлично!" + " " + $client.name + ", " + "тогда хочу предложить Вам оформить тур в" + " " + capitalize($caila.inflect($session.geo, ["accs"])) + " , начнем заполнение заявки?");
                        $reactions.buttons([{text: "Начнём!", transition: "/named/arrangeTour/checker"}, {text: "Нет", transition: "/named/checkWeather"}]);
                        };
                } else { 
                    $reactions.answer("Куда вы бы хотели отправиться?");
                    };

            state: city
                intent: /Geo 
                script: $session.geo = $parseTree._GEO
                go!: /named/arrangeTour/checker
   
            state: undecided
                q: $undecided
                script: $session.geo = "Не определился"
                go!: /named/arrangeTour/checker
                
            state: localCatchAll || fromState = "/named/arrangeTour", onlyThisState = true, noContext = true
                event: noMatch
                a: Мы не сможем оформить тур, если вы не подскажите нам, куда хотите отправиться. Пожалуйста напишите город или страну
    
            state: checker
                script: // Проверяем какие данные уже заполнял клиент
                    if ($session.numberOfPoeple) {
                        if ($session.numberOfChildrens) {
                            if ($session.Budget) {
                                if ($session.startDate) {
                                    if ($session.tripDuration) {
                                        if ($session.hotelStars) {
                                            if ($client.name) {
                                                if ($client.phone) {
                                                    if ($session.comment) {
                                                        $reactions.transition("/named/arrangeTour/comment");
                                                        } else {
                                                            $reactions.transition("/named/arrangeTour/comment");
                                                            };
                                                    } else {
                                                        $reactions.transition("/named/arrangeTour/phone");
                                                        };
                                                } else {
                                                $reactions.transition("/named/arrangeTour/username");
                                                    };
                                            } else {
                                                $reactions.transition("/named/arrangeTour/hotelStars");
                                                };
                                        } else {
                                            $reactions.transition("/named/arrangeTour/tripDuration");
                                            };
                                    } else {
                                        $reactions.transition("/named/arrangeTour/startDate");
                                        };
                                } else {
                                    $reactions.transition("/named/arrangeTour/Budget");
                                    };
                            } else {
                                $reactions.transition("/named/arrangeTour/numberOfChildrens");
                                };
                        } else {
                            $reactions.transition("/named/arrangeTour/numberOfPeople");
                            };

            state: numberOfPeople
                a: На сколько человек оформляем тур? 
                buttons:
                    "Пропустить" -> ./dontKnow

                state: getNumberOfPeople
                    intent: /Number/ofPeople
                    script: $session.numberOfPoeple = $parseTree.text
                    go!: /named/arrangeTour/numberOfChildrens
 
                state: dontKnow
                    q: $dontKnow
                    script: $session.numberOfPoeple = "Не выбрано"
                    go!: /named/arrangeTour/numberOfChildrens

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: мы не сможем помочь вам с оформлением тура, если не будем знать сколько человек поедет. Пожалуйста напишите количество людей в поездке цифрой

            state: numberOfChildrens
                a: Будут ли среди них дети и сколько? Дети считаются до 12 лет.
                buttons:
                    "без детей"
                    "Пропустить" -> ./dontKnow

                state: getNumberOfChildrens
                    intent: /Number/ofChildrens
                    script: $session.numberOfChildrens = $parseTree.text
                    go!: /named/arrangeTour/Budget

                state: dontKnow
                    q: $dontKnow
                    script: $session.numberOfChildrens = "Не выбрано"
                    go!: /named/arrangeTour/Budget

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Мы не сможем корректно рассчитать стоимость, если не будем знать, сколько поедет детей. Пожалуйста напишите ответ цифрой

            state: Budget
                a: Какой у Вас бюджет поездки?
                buttons:
                    "Пропустить" -> ./dontKnow

                state: getBudget
                    intent: /Number/Budget
                    script: $session.Budget = $parseTree.text    
                    go!: /named/arrangeTour/startDate

                state: dontKnow
                    q: $dontKnow
                    script: $session.Budget = "Не выбрано"
                    go!: /named/arrangeTour/startDate

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Не зная ваш бюджет на поездку нам будет очень сложно помочь вам с оформлением тура. Пожалуйста напишите, какую сумму вы закладываете на путешествие

            state: startDate
                a: Дата начала поездки?
                buttons:
                    "Пропустить" -> ./dontKnow
 
                state: getStartDate
                    intent: /Date
                    script: $session.startDate = $parseTree._DATE
                    go!: /named/arrangeTour/tripDuration 

                state: dontKnow
                    q: $dontKnow
                    script: $session.startDate = "Не выбрано"
                    go!: /named/arrangeTour/tripDuration 

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Стоимость тура сильно отличается в зависимости от даты начала поездки. Пожалуйста напишите удобную вам дату отправления
 
            state: tripDuration
                a: Длительность поездки?
                buttons:
                    "Пропустить" -> ./dontKnow

                state: getTripDuration
                    intent: /Number/tripDuration
                    script: $session.tripDuration = $parseTree.text
                    go!: /named/arrangeTour/hotelStars

                state: dontKnow
                    q: $dontKnow
                    script: $session.tripDuration = "Не выбрано"
                    go!: /named/arrangeTour/hotelStars

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Для расчёта стоимости необходимо знать длительность поездки. Пожалуйста напишите, сколько дней вы планируете заложить на путешествие
 
            state: hotelStars
                a: Желаемая звёздность отеля?
                buttons:
                    "1"
                    "2"
                    "3"
                    "4"
                    "5"
                    "Пропустить" -> ./dontKnow

                state: getHotelStars
                    intent: /Number/hotelStars
                    script: 
                        $session.hotelStars = $parseTree.text;
                        var starQuant = $parseTree._NUMBER; // Проверяем не превышет ли количество звёзд возможные значения
                        if (starQuant >= 1 && starQuant <= 5) { 
                            $reactions.transition("/named/arrangeTour/username");
                            } else {
                                $reactions.answer("Существуют только отели со звёздностью от 1 до 5");
                                $reactions.buttons([{text: "1"}, {text: "2",}, {text: "3"}, {text: "4",}, {text: "5",}, {text: "Пропустить", transition: "/named/arrangeTour/username"}])
                                $reactions.transition( {value: "/named/arrangeTour/hotelStars/getHotelStars", deferred: true} );
                                };

                state: dontKnow
                    q: $dontKnow
                    script: $session.numberOfPoeple = "Не выбрано"
                    go!: /named/arrangeTour/username

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Пожалуйста напишите желаемую звёздность отеля от 1 до 5

            state: username
                a: Контатное лицо — {{$client.name}}, верно?
                buttons:
                    "Да"
                    "Нет" -> ./whatsName

                state: whatsName
                    q: $disagree
                    a: На чьё имя оформляем заявку?

                    state: getName
                        intent: /Name
                        script: $session.name = $parseTree._NAME
                        go!: /named/arrangeTour/phone

                    state: localCatchAll || noContext = true
                        event: noMatch
                        script: $session.name = $parseTree.text
                        a: Хмм, я не знаю имени {{$client.name}}. Вы уверены в правильности написания?
                        buttons: 
                            "Да" -> /named/arrangeTour/phone
                            "Нет" -> /named/arrangeTour/username/whatsName

                state: agree
                    q: $agree
                    script: $session.name = $client.name 
                    go!: /named/arrangeTour/phone

            state: phone
                a: Пожалуйста оставьте контактный номер телефона
                buttons:
                    {text: "Отправить номер телефона", request_contact: true}

                state: getPhoneEvent
                    event: telegramSendContact
                    script: $client.phone = $request.rawRequest.message.contact.phone_number;
                    go!: /named/arrangeTour/comment

                state: getPhone
                    intent: /Phone
                    script: $client.phone = $parseTree._Phone
                    go!: /named/arrangeTour/comment

                state: localCatchAll || noContext = true
                    event: noMatch
                    a: Это не похоже на номер телефона, пожалуйста проверьте правильность введённого номера 

            state: comment
                a: Оставьте ваш комментарий в свободной форме
                buttons:
                    "Пропустить" -> ./dontKnow
                    
                state: getComment
                    q: * || fromState = "/named/arrangeTour/comment", onlyThisState = True
                    script: $session.comment = $parseTree.text
                    go!: /named/arrangeTour/sendEmail

                state: dontKnow
                    q: $dontKnow
                    script: $session.comment = "Не выбрано"
                    go!: /named/arrangeTour/sendEmail

            state: sendEmail
                script: 
                    init: $mail.debug(true);
                    var result = $mail.send({        // Отправляем письмо на почту
                    from: "alltechjaicp@mail.ru",
                        hiddenCopy: [],
                        to: ["alltechjaicp@mail.ru"],
                        subject: "Новая заявка на подбор тура",
                        content: "Город — " + $session.geo + '\n<br>' + "Количество человек — " + $session.numberOfPoeple + '\n<br>' + "Количество детей — " + $session.numberOfChildrens + '\n<br>' + "Бюджет — " + $session.Budget + '\n<br>' + "Дата начала поездки — " + $session.startDate + '\n<br>' + "Длительность поездки — " + $session.tripDuration + '\n<br>' + "Звёздность отеля — " + $session.hotelStars + '\n<br>' + "Имя — " + $client.name + '\n<br>' + "Телефон — " + $client.phone + '\n<br>' + "Комментарий — " + $session.comment,
                        smtpHost: "smtp.mail.ru",
                        smtpPort: "465",
                        user: "alltechjaicp@mail.ru",
                        password: "1cpds61v7WiDbHd5Kx7X"
                    });
                    
                    if (result.status == "OK") {
                        $reactions.answer("Отправил вашу заявку менеджеру, он перезвонит вам как только с ней ознакомится! Будем ждать вас снова, не пропустите звонок!");
                        $jsapi.stopSession();
                        $reactions.transition("/howCanHelp");
                    } else {
                        $reactions.answer("К сожалению, не удалось отправить заявку менеджеру. Пожалуйста, обратитесь за подбором тура по телефону 8(812)000-00-00. Хорошего дня!");
                        $jsapi.stopSession();
                    }; 

    state: howCanHelp
        q!: $regex</endSession>
        script: $jsapi.startSession();
        a: Могу вам еще чем-то помочь?
        buttons:
            "Узнать погоду" -> /named/checkWeather
            "Оформить тур" -> /named/arrangeTour 
    
        state: yes
            q: $agree
            a: Могу рассказать о погоде по всему миру, а также помочь оформить тур.
            buttons:
                "Узнать погоду" -> /named/checkWeather
                "Оформить тур" -> /named/arrangeTour
    
    state: CatchAll || noContext = true
        event!: noMatch 
        a: К сожалению, не смог понять, что вы написали, попробуйте переформулировать.
    

    state: nameRegret
        q!: сбрось имя 
        script: // функция для удобства тестирования
            $client.name = null
