# Net Spectator
Выполняет контроль доступности сетевых хостов.

23/05/2023 (0.2.2)
- клиент и сервер теперь общаются json'ами (добавлена сериализация);
- клиент определяет место на диске, наименование устройства, оставшееся место на диске, разделы;

22/04/2023 (0.2.1)
- миграция BD на h2;
- добавлен web core сервис для обработки команд web сервиса;
- добавлен web сервис возвращающий web страницы с содержимым;
- реализован web-интерфейс отображающий список устройств из базы данных (в т. ч. статусы);


11/12/2022 (0.1 beta)
- blacklist: просмотр содержимого, удаление элементов, разграничение прав доступа;
- connections: добавлена возможность управления подключениями (просмотр, удаление, добавление в черный список);
- исправлена ошибка подключения клиента находящегося в черном списке (происходило зацикливание ожидания ответа от сервера и клиент не завершал свою работу);
- подключил библиотеки: hibernate, sqlite dialect;
- создал и наполнил базу данных, создал ссылку в hibernate.cfg, сконфигурировал объекты;
- добавил класс для группировки объектов;
- добавил сервис управления базами данных;