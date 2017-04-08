function clean(indata) {
    return indata.innerHTML.replace(/[^\-?0-9.]/g, '');
}

function returnPercent(part, total) {
    var temp = total != 0 ? part / total * 100 : 0;
    temp = temp.toFixed(1);
    return temp + '&nbsp;%';
}

function addStats(rating) {
    rating.rows[0].insertAdjacentHTML('beforeEnd', '<th data-sort-method="number" class="s hidden">Трудяга</th>' +
        '<th data-sort-method="number" class="s hidden">Грабитель</th>' +
        '<th data-sort-method="number" class="s hidden">Уничтожитель</th>' +
        '<th data-sort-method="number" class="s hidden">Собиратель</th>' +
        '<th data-sort-method="number" class="s hidden">Жмот</th>');

    for (var i = 1; i < rating.rows.length; i++) {
        var exp = clean(rating.rows[i].cells[3]);
        var gold = clean(rating.rows[i].cells[4]);
        var robbery = clean(rating.rows[i].cells[7]);
        var destruction = clean(rating.rows[i].cells[8]);
        var treasure = clean(rating.rows[i].cells[9]);
        var work = exp - robbery - destruction - treasure;

        rating.rows[i].insertAdjacentHTML('beforeEnd', '<td class="r s hidden"></td>' +
            '<td class="r s hidden"></td>' +
            '<td class="r s hidden"></td>' +
            '<td class="r s hidden"></td>' +
            '<td class="r s hidden"></td>');

        rating.rows[i].cells[10].innerHTML = returnPercent(work, exp);
        rating.rows[i].cells[11].innerHTML = returnPercent(robbery, exp);
        rating.rows[i].cells[12].innerHTML = returnPercent(destruction, exp);
        rating.rows[i].cells[13].innerHTML = returnPercent(treasure, exp);
        rating.rows[i].cells[14].innerHTML = returnPercent(gold, exp);
    }
}

function showStats() {
    var class_s = document.getElementsByClassName('s');

    for (var i = 0; i < class_s.length; i++) {
        class_s[i].classList.remove('hidden');
    }

    document.getElementById('open-stats').classList.add('hidden');
    document.getElementById('close-stats').classList.remove('hidden');
}

function closeStats() {
    var class_s = document.getElementsByClassName('s');

    for (var i = 0; i < class_s.length; i++) {
        class_s[i].classList.add('hidden');
    }

    document.getElementById('open-stats').classList.remove('hidden');
    document.getElementById('close-stats').classList.add('hidden');
}
