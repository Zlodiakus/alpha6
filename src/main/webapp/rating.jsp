<%@ page language="java" contentType="text/html; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.Rating"%>
<%
String token=Rating.getFullRate();
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" href="bootstrap.min.css">
    <script src="tablesort.min.js"></script>
    <script src="tablesort.number.js"></script>
    <script src="rating.js"></script>
    <title>Рейтинг</title>
    <style>
        body {
            margin: 15px;
        }

        td.r {
            text-align: right;
        }
        th::-moz-selection { background:transparent; }
        th::selection      { background:transparent; }
        th { cursor:pointer; }
        table th:after {
            content:'';
            float:right;
            margin-top:7px;
            border-width:0 4px 4px;
            border-style:solid;
            border-color:#404040 transparent;
            visibility:hidden;
        }
        table th:hover:after {
            visibility:visible;
        }
        table th.sort-up:after,
        table th.sort-down:after,
        table th.sort-down:hover:after {
            visibility:visible;
            opacity:0.4;
        }
        table th.sort-up:after {
            border-bottom:none;
            border-width:4px 4px 0;
        }
        .table>thead>tr>th {
            vertical-align: top;
        }
    </style>
</head>
<body>
    <div class="col-lg-10">
        <table class="table table-striped table-bordered table-hover" id="rating">
            <thead>
            <tr>
                <th>№</th>
                <th>Имя</th>
                <th data-sort-method='number'>Уровень</th>
                <th data-sort-method='number'>Опыт</th>
                <th data-sort-method='number'>Золото</th>
                <th data-sort-method='number'>Корованов</th>
                <th data-sort-method='number'>Доход<br>в час</th>
                <th data-sort-method='number'>Награблено</th>
                <th data-sort-method='number'>Уничтожение<br>засад</th>
                <th data-sort-method='number'>Найдено в <br>сундуках</th>
            </tr>
            </thead>
            <tbody>
<%=token%>
            </tbody>
        </table>
        <p class="pull-right">
            <a href="#" id="open-stats" onclick="showStats(); return false;">Статистика</a>
            <a href="#" id="close-stats" class="hidden" onclick="closeStats(); return false;">Без статистики</a>
        </p>
    </div>

    <script>
        addStats(document.getElementById('rating'));
        new Tablesort(document.getElementById('rating'), { descending: true });
    </script>

</body>
</html>
