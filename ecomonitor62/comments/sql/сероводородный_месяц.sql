SELECT DATETIME(ROUND(commentTime), 'unixepoch', 'localtime') AS date, latitude as longitude, longitude as latitude, commentText FROM
	(SELECT * FROM ecoMonitor62 
	WHERE commentText like "%серовод%" 
		or commentText like "%гавн%" 
		or commentText like "%говн%" 
		or commentText like "%каках%" 
		or commentText like "%канали%")
WHERE date > "2020-10-01" and date < "2020-10-31"
-- в октябре 2020 население Рязани наконец-то заметило, что НПЗ ох..ел
-- почти весь месяц вонь во всем городе