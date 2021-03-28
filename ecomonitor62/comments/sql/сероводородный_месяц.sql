SELECT DATETIME(ROUND(comment_time), 'unixepoch', 'localtime') AS date, latitude as longitude, longitude as latitude, comment_text FROM
    (SELECT * FROM ecoMonitor62
     WHERE comment_text like "%серовод%"
        or comment_text like "%гавн%"
        or comment_text like "%говн%"
        or comment_text like "%каках%"
        or comment_text like "%канали%")
WHERE date > "2020-10-01" and date < "2020-10-31"
-- в октябре 2020 население Рязани наконец-то заметило, что НПЗ ох..ел
-- почти весь месяц вонь во всем городе