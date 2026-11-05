# Cloud Drive Project (30-ый вариант)

## Параметры генератора csv-журнала событий:
```json
{
  "id": 30,
  "block": 5,
  "slug": "cloud_drive",
  "title": "Хмарне сховище",
  "n03Invariant": "forbid_upload_if_exceeds_free_space",
  "entities": {
    "file": { "key": ["file", "kind"] }
  },
  "events": [
    {
      "code": "UPLOAD",
      "entity": { "ref": "file", "op": "create" },
      "fields": [
        { "col": "p1", "name": "file", "type": "token" },
        { "col": "p2", "name": "size_mb", "type": "positive_int", "range": { "min": 1, "max": 1200 } },
        { "col": "p3", "name": "kind", "type": "enum_ref", "allowed": ["DOC", "IMG", "VID", "OTHER"] }
      ]
    },
    {
      "code": "TRASH",
      "entity": { "ref": "file", "op": "remove", "danglingRate": 0.05 },
      "fields": [
        { "col": "p1", "name": "file", "type": "token" },
        { "col": "p2", "name": "size_mb", "type": "positive_int", "range": { "min": 1, "max": 1200 } },
        { "col": "p3", "name": "kind", "type": "enum_ref", "allowed": ["DOC", "IMG", "VID", "OTHER"] }
      ]
    },
    { "code": "EMPTY_TRASH", "fields": [] },
    {
      "code": "PROMO_SPACE",
      "fields": [
        { "col": "p1", "name": "extra_mb", "type": "positive_int", "range": { "min": 512, "max": 5120 } }
      ]
    }
  ],
  "eventWeights": { "UPLOAD": 0.45, "TRASH": 0.3, "EMPTY_TRASH": 0.1, "PROMO_SPACE": 0.15 },
  "constants": { "baseQuotaMb": 15360 }
}
```
Отсюда понятно, что столбец $code$ отвечает за комманду (инструкцию) хранилищу.
В зависимости от инструкции, колонки $p1-p4$ несут в себе полезную нагрузку:
* В случае $code=$ UPLOAD колонка $p1$ отвечает за имя файла (токен), $p2 -$ за размер файла, $p3 -$ за расширение файла (поддерживаются только DOC, IMG, VID, OTHER)
* В случае $code=$ TRASH колонка $p1$ отвечает за имя файла (токен), $p2 -$ за размер файла, $p3 -$ за расширение файла
* В случае $code=$ EMPTY_TRASH корзина со всеми файлами просто очищается (колонок с полезной нагрузкой нет)
* В случае $code=$ PROMO_SPACE колонка $p1$ отвечает за количество доп. места для выделения на диске 