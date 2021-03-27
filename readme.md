##Полезные ссылки

Роза ветров Рязани
https://ilyutkin.ru/

Основные загрязнители Рязани
https://www.google.com/maps/d/viewer?mid=1Gaza4uOpfGbj5cvI0kKgR51kkbbOwTCA&shorturl=1&ll=54.578613925883595%2C39.83104175061036&z=12

Экомонитор Рязани с жалобами
https://ecomonitor62.ru/

Самое удобное приложение 
https://play.google.com/store/apps/details?id=com.mrgames13.jimdo.feinstaubapp

Ещё один хороший сервис
https://russia.maps.sensor.community/#13/54.6122/39.8004

#Как сделать датчик для замеров воздуха
##Запчасти

1 321,19 руб.  19%OFF | Датчик высокой точности Nova PM SDS011, лазерный датчик качества воздуха PM2.5, датчик супер-пыли, цифровой выход s SDS, 1 шт.
https://a.aliexpress.com/_u7kJRt

97,12 руб.  19%OFF | ESP8266 ESP32 CP2102 CH340 NodeMcu V3 V2 D1 MINI Lua беспроводной WIFI модуль Разъем макетная плата CP2102 ESP-12E Micro USB
https://a.aliexpress.com/_v28qlh

46,25 руб. | Высокоточный модуль датчика атмосферного давления BMP280 BME280 с высотомером, 3,3 В, 5 В
https://a.aliexpress.com/_vekO6L


##Инструкция (некоторые моменты там не очень хорошо описаны, по моему мнению) 
https://docs.google.com/document/d/1cDL0KtBHc0Q2Dq_zfSVBFS6UNHIDVBUb3gaUc5YoP2w/edit#heading=h.gv0m2i78oj83


#Как можно применить базу с комментами 
###Тепловая карта
Можно создать тепловую карту жалоб. Например сделать селект по интересующему запаху (например %пластик%) в определенном диапазоне дат

`SELECT latitude as longitude, longitude as latitude FROM
(SELECT DATETIME(ROUND(commentTime), 'unixepoch', 'localtime') AS date, longitude, latitude FROM ecoMonitor62
WHERE commentText like "%пластик%"
WHERE date between "2020-10-01" and "2020-10-11"`

Полученный список координат без заголовков сохранить в csv `54.61417816316;39.7204259055767`
файл загрузить на сайт http://newscad.com/heat-maps
Регулировками радиуса добиться наилучшего отображения (лучше увеличить радиус и прозрачность)

###Статистики
Можно выявлять в каком районе какие выбросы наиболее часто, или редко встречаются

###да всё, на что хватит фантазии
можно вычислить какой запах чувствует лучше всего конкретный пользователь сайта =)