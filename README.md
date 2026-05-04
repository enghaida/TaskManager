# Smart Task Manager

A Swing-based task manager with weather-aware scheduling, built with Project Reactor.

## Prerequisites

- Java 17+
- Maven

## Configuration

The app fetches live weather data from [OpenWeatherMap](https://openweathermap.org/api).
You must set your API key as an environment variable before running:

```cmd
set OPENWEATHER_API_KEY=your_api_key_here
```

The app will throw an `IllegalStateException` at startup if the variable is missing.

## Running

```bash
mvn compile exec:java -Dexec.mainClass="taskmanager.MainApp"
```
