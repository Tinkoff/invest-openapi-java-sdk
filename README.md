# OpenAPI SDK для Java

Данный проект представляет собой инструментарий на языке Java для работы с OpenAPI Тинькофф Инвестиции, который можно
использовать для создания торговых роботов.

## Начало работы

Для сборки библиотеки понадобится Gradle версии не ниже 5, а также JDK версии не ниже 8. Затем в терминале перейдите
в директорию проекта и выполните следующую команду
```bash
gradlew build
```
Или с помощью docker
```
docker run --rm -u gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:jdk8 gradle build
```

Скорее всего вы увидите ошибки, связанные с подпроектом example - он требует JDK версии 11. Однако, сам SDK должен
скомпилироваться.

После успешной сборки в поддиректории `sdk-java8/build/libs` появится jar-файл, который можно подключить к любому
другому Java-проекту (или Java-совместимому, например, на таких языках, как Kotlin и Scala).

### Где взять токен аутентификации?

В разделе инвестиций вашего [личного кабинета tinkoff](https://www.tinkoff.ru/invest/). Далее:


* Перейдите в настройки
* Проверьте, что функция "Подтверждение сделок кодом" отключена
* Выпустите токен для торговли на бирже и режима "песочницы" (sandbox)
* Скопируйте токен и сохраните, токен отображается только один раз, просмотреть его позже не получится, тем не менее вы
  можете выпускать неограниченное количество токенов

## Документация

Для проекта можно сгенерировать javadoc-документацию с помощью команды
```bash
gradlew javadoc
```
Или с помощью docker
```
docker run --rm -u gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gradle:jdk8 gradle javadoc
```

Проект разделён на 3 части:

- core - содержит интерфейсы всех частей REST API и Streaming API, а также модели данных, которые они используют;
- sdk-java8 - содержит реализацию core-интерфейсов с использованием http-клиента из библиотеки OkHttp;
- example - простой пример использования core-интерфесов, реализованных в sdk-java8 (для компиляции необходим jdk11).

Единственной зависимостью в core-части явлется библиотека Jackson для работы с JSON.

Документацию непосредственно по OpenAPI можно найти по [ссылке](https://tinkoffcreditsystems.github.io/invest-openapi/).

### А если вкратце?

Для непосредственного взаимодействия с OpenAPI нужно создать подключение.

```java
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

var token = "super_token"; // токен авторизации
var sandboxMode = true;
var factory = new OkHttpOpenApiFactory(parameters.ssoToken, logger);
OpenApi api;

if (sandboxMode) {
    api = factory.createSandboxOpenApiClient(
            se -> logger.info("Из Streaming API пришло событие"),
            ex -> logger.severe("Что-то произошло со Streaming API")
    );
    // ОБЯЗАТЕЛЬНО нужно выполнить регистрацию в "песочнице"
    ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
} else {
    api = factory.createOpenApiClient(
            se -> logger.info("Из Streaming API пришло событие"),
            ex -> logger.severe("Что-то произошло со Streaming API")
    );
}

// Вся работа происходит через объекты контекста, все запросы асинхронны
var portfolio = api.portfolioContext.getPortfolio().join(); // получить текущий портфель
```

### А пример готового робота есть?

Пример готового робота пока отсутствует, но планируется к добавлению.

## У меня есть вопрос

[Основной репозиторий с документацией](https://github.com/TinkoffCreditSystems/invest-openapi/issues) - в нем вы можете задать вопрос в Issues и получать информацию о релизах в Releases.

Если возникают вопросы по данному SDK, нашёлся баг или есть предложения по улучшению, то можно задать его в Issues, либо писать на почту openapi_invest@tinkoff.ru
