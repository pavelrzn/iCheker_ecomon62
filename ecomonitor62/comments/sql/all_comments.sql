SELECT commentId as 'id', 
strftime('%Y-%m-%d %H:%M:%S', DATETIME(commentTime, 'unixepoch', 'localtime')) as 'timestamp', 
commentText as 'comment', latitude as 'longitude', longitude as 'latitude', userID as 'user_id', userFIO as 'user_fio'
from ecoMonitor62

-- просто более удобный просмотр комментов