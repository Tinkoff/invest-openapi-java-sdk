[![Release](https://jitpack.io/v/TinkoffCreditSystems/invest-openapi-java-sdk.svg?style=flat-square)](https://jitpack.io/#TinkoffCreditSystems/invest-openapi-java-sdk)

# OpenAPI SDK для Java

Данный проект представляет собой инструментарий на языке Java для работы с OpenAPI Тинькофф Инвестиции, который можно
использовать для создания торговых роботов.

## Начало работы

Для сборки библиотеки понадобится Apache Maven версии не ниже 3, а также JDK версии не ниже 8. Затем в терминале перейдите
в директорию проекта и выполните следующую команду
```bash
mvn clean package
```
Или с помощью docker
```bash
# Linux/Mac версия

docker run -it --rm --name invest-openapi-java-sdk -v "$PWD":/usr/src/invest-openapi-java-sdk -w /usr/src/invest-openapi-java-sdk maven:3.6-jdk-11-slim mvn clean package
```
```PowerShell
# Windows PowerShell версия

docker run -it --rm --name invest-openapi-java-sdk -v "$(pwd):/usr/src/invest-openapi-java-sdk".ToLower() -w /usr/src/invest-openapi-java-sdk maven:3.6-jdk-11-slim mvn clean package
```

Скорее всего вы увидите ошибки, связанные с подпроектом example - он требует JDK версии 11. Однако, сам SDK должен
скомпилироваться.

После успешной сборки в поддиректории `sdk-java8\target` появится jar-файл `openapi-java-sdk-java8-<version>`, который можно подключить к любому
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
mvn javadoc:javadoc
```
Или с помощью docker
```bash
# Linux/Mac версия

docker run -it --rm --name invest-openapi-java-sdk -v "$PWD":/usr/src/invest-openapi-java-sdk -w /usr/src/invest-openapi-java-sdk maven:3.6-jdk-11-slim mvn javadoc:javadoc
```

Проект разделён на 3 части:

- core - содержит интерфейсы всех частей REST API и Streaming API, а также модели данных, которые они используют;
- sdk-java8 - содержит реализацию core-интерфейсов с использованием http-клиента из библиотеки OkHttp;
- example - простой пример использования core-интерфесов, реализованных в sdk-java8 (для компиляции необходим jdk11).

Модели данных core-части подготовлены для работы с JSON при помощи Jackson аннотаций.

Документацию непосредственно по OpenAPI можно найти по [ссылке](https://tinkoffcreditsystems.github.io/invest-openapi/).

Каждая часть может быть подключена в качестве зависимости

```xml
<dependency>
  <groupId>ru.tinkoff.invest</groupId>
  <artifactId>openapi-java-sdk-core</artifactId>
<!--  <artifactId>openapi-java-sdk-java8</artifactId>-->
<!--  <artifactId>openapi-java-sdk-example</artifactId>-->
  <version>0.4.1</version>
</dependency>
```

### А если вкратце?

Для непосредственного взаимодействия с OpenAPI нужно создать подключение.

```java
import org.reactivestreams.Subscriber;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;
import ru.tinkoff.invest.openapi.model.rest.*;
import ru.tinkoff.invest.openapi.model.streaming.*;

import java.util.concurrent.Executors;

class Example {
    public static void main(String[] args) {
      String token = "super_token"; // токен авторизации
      boolean sandboxMode = true;
      OpenApi api = new OkHttpOpenApi(token, sandboxMode);

      if (api.isSandboxMode()) {
        api.getSandboxContext().performRegistration(new SandboxRegisterRequest()).join();
      }

      Subscriber<StreamingEvent> listener = new Subscriber() { /* ваш вариант слушателя */ };
      api.getStreamingContext().subscribe(listener);

      // оформляем подписку на поток "свечей"
      api.getStreamingContext().sendRequest(StreamingRequest.subscribeCandle("<какой-то figi>", CandleInterval.FIVE_MIN));

      // Вся работа происходит через объекты контекста, все запросы асинхронны
      Portfolio portfolio = api.getPortfolioContext.getPortfolio().join(); // получить текущий портфель
    }
}
```

### А пример готового робота есть?

Пример готового робота пока отсутствует, но планируется к добавлению.

## У меня есть вопрос

[Основной репозиторий с документацией](https://github.com/TinkoffCreditSystems/invest-openapi/issues) - в нем вы можете задать вопрос в Issues и получать информацию о релизах в Releases.

Если возникают вопросы по данному SDK, нашёлся баг или есть предложения по улучшению, то можно задать его в Issues.
