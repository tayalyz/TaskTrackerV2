Добавьте новые поля в задачи:
- duration — продолжительность задачи, оценка того, сколько времени она займёт в минутах (число);
- startTime — дата, когда предполагается приступить к выполнению задачи.
- getEndTime() — время завершения задачи, которое рассчитывается исходя из startTime и duration.

Менять сигнатуры методов интерфейса TaskManager не понадобится: при создании или обновлении задач все его методы будут 
принимать и возвращать объект, в который вы добавите два новых поля.

С классом Epic придётся поработать дополнительно. 
Продолжительность эпика — сумма продолжительности всех его подзадач. 
Время начала — дата старта самой ранней подзадачи, а время завершения — время окончания самой поздней из задач. 
Новые поля duration и startTime этого класса будут расчётные — аналогично полю статус. 
Для реализации getEndTime() удобно добавить поле endTime в Epic и рассчитать его вместе с другими полями.

Не забудьте также доработать опцию сохранения состояния в файл: добавьте в сериализацию новые поля.
Добавьте в тесты проверку новых полей.

Выведите список задач в порядке приоритета
Отсортируйте все задачи по приоритету — то есть по startTime. 
Если дата старта не задана, добавьте задачу в конец списка задач, подзадач, отсортированных по startTime. 
Напишите новый метод getPrioritizedTasks, возвращающий список задач и подзадач в заданном порядке.
Предполагается, что пользователь будет часто запрашивать этот список задач и подзадач, 
поэтому подберите подходящую структуру данных для хранения. Сложность получения должна быть уменьшена с O(n log n) до O(n).

Подсказка: как ускорить сортировку
Если сортировать список заново каждый раз, сложность получения будет O(n log n). 
Можно хранить все задачи заранее отсортированными с помощью класса TreeSet

Проверьте пересечения
Предполагается, что пользователь будет выполнять не более одной задачи за раз. Научите трекер проверять, что задачи и подзадачи не пересекаются по времени выполнения. Добавьте валидацию во время создания или изменения задач, подзадач.
Подсказка: как искать пересечения за O(n)
getPrioritizedTasks возвращает отсортированный список задач. 
По нему можно пройтись за O(n) и проверить все задачи на пересечение.