patterns:
    $undecided = (* не (уверен/знаю/определился) *)
    $agree = ([ну] [конечно|всё|все|вроде|пожалуй|возможно] (да|даа|lf|ага|агась|точно|угу|верно|ок|ok|окей|окай|okay|оке|именно|подтвержд*|йес) [да|конечно|конешно|канешна|всё|все|вроде|пожалуй|возможно] | (конечно|конешно|канешна|а то [нет]|очень|[ты] прав*|абсолютно|обязательно|непременно|а как же|[я] подтверждаю|[совершенно|абсолютно] точно|пожалуй|запросто|норм|(почему/что/че) [бы] [и] нет|хочу|было [бы] (неплохо|не плохо)|[я] [очень] (хочу|хо чу|ладно|хорошо)|ладно|можно|валяй*|договорились|[я] [совершенно|абсолютно] [с (тобой/вами)] согла*|не могу [с (тобой/вами)] не согласиться|вполне|в полной мере|естественно|разумеется|(еще|ещё) как|[я] не (против|возражаю|сомневаюсь)|я только за|безусловн*|[это] так [и есть]|все (так|верно)|(совершенно|абсолютно) верно) | (давай|давайте|логично|могу|было дело|бывало|бывает) | именно)
    $disagree = ((нет|неат|ниат|неа|ноуп|ноу|найн) [нет] [спасибо] | [@No|конечно|конешно|канешна] (@No|не сейчас/ни капли/отнюдь/нискол*/да ладно/[я] не (хоч*|хо чу|надо|могу|очень|думаю|нравится|стоит|буду|считаю|согла*|подтв*)|ненадо|нельзя|нехочу|ненавижу|невозможно|никогда|никуда|ни за что|нисколько|никак*|никто|ниразу|[я] против|вряд ли|сомневаюсь|неправильно|неверно|невсегда|[это] {не так}|отказываюсь) [(конечно|конешно|канешна|спасибо)] | (да ну|не|нее|ничего))
    $dontKnow = (пропустить)