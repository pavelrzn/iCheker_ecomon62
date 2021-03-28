SELECT comment_id as 'id',
strftime('%Y-%m-%d %H:%M:%S', DATETIME(comment_time, 'unixepoch', 'localtime')) as 'timestamp',
comment_text as 'comment', latitude as 'longitude', longitude as 'latitude', user_id, user_FIO
from ecoMonitor62

-- просто более удобный просмотр комментов